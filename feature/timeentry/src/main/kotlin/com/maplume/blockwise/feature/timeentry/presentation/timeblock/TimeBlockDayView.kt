package com.maplume.blockwise.feature.timeentry.presentation.timeblock

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.core.domain.model.TimeEntry
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.roundToInt

@Composable
fun TimeBlockDayView(
    date: LocalDate,
    entries: List<TimeEntry>,
    selectedEntryId: Long?,
    onEntryClick: (TimeEntry) -> Unit,
    onEntryLongClick: (TimeEntry) -> Unit,
    onEmptySlotClick: (LocalTime) -> Unit,
    onEmptyRangeCreate: (startTime: LocalTime, endTime: LocalTime) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    hourHeight: Dp = 60.dp
) {
    val scrollState = rememberScrollState()
    val timeAxisWidth = 48.dp

    val fiveMinuteStep = 5
    val minutesPerHour = 60
    val hoursPerDay = 24
    val columnsPerHour = minutesPerHour / fiveMinuteStep
    val minutesPerDay = hoursPerDay * minutesPerHour

    fun snapMinutesToNearestGrid(minutes: Int, gridMinutes: Int): Int {
        return ((minutes + gridMinutes / 2) / gridMinutes) * gridMinutes
    }

    fun floorToGrid(minutes: Int, gridMinutes: Int): Int {
        return (minutes / gridMinutes) * gridMinutes
    }

    fun ceilToGrid(minutes: Int, gridMinutes: Int): Int {
        return ((minutes + gridMinutes - 1) / gridMinutes) * gridMinutes
    }

    fun minutesToLocalTime(minutes: Int): LocalTime {
        if (minutes >= minutesPerDay) return LocalTime(0, 0)
        val clamped = minutes.coerceAtLeast(0)
        val hours = clamped / 60
        val mins = clamped % 60
        return LocalTime(hours, mins)
    }

    fun offsetToSnappedMinutes(offset: Offset, gridWidthPx: Float, hourHeightPx: Float): Int {
        val hourIndex = (offset.y / hourHeightPx).toInt().coerceIn(0, hoursPerDay - 1)
        val minuteInHour = ((offset.x / gridWidthPx) * minutesPerHour.toFloat())
            .roundToInt()
            .coerceIn(0, minutesPerHour)
        val rawMinutes = hourIndex * minutesPerHour + minuteInHour
        return snapMinutesToNearestGrid(rawMinutes, fiveMinuteStep)
            .coerceIn(0, minutesPerDay)
    }

    var rangeSelection by remember { mutableStateOf<Pair<Int, Int>?>(null) }
    var isRangeSelectionActive by remember { mutableStateOf(false) }

    val positionedEntries = remember(entries) {
        calculatePositionedEntries(entries)
    }

    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val subGridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)
    val rangeSelectionColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.14f)
    val selectedEntryOverlayColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.18f)

    Box(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
        ) {
            TimeAxis(
                hourHeight = hourHeight,
                modifier = Modifier.width(timeAxisWidth)
            )

            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .height(hourHeight * hoursPerDay)
                    .drawBehind {
                        val hourPx = hourHeight.toPx()
                        val width = size.width
                        val cellWidth = width / columnsPerHour

                        for (h in 0 until hoursPerDay) {
                            val y = h * hourPx
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, size.height),
                            end = Offset(width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )

                        for (c in 1 until columnsPerHour) {
                            val x = c * cellWidth
                            drawLine(
                                color = subGridColor,
                                start = Offset(x, 0f),
                                end = Offset(x, size.height),
                                strokeWidth = 0.5.dp.toPx()
                            )
                        }

                        val selection = rangeSelection
                        if (selection != null) {
                            val (a, b) = selection
                            val start = minOf(a, b)
                            val end = maxOf(a, b)

                            if (end > start) {
                                val startHour = (start / minutesPerHour).coerceIn(0, hoursPerDay - 1)
                                val endHour = ((end - 1) / minutesPerHour).coerceIn(0, hoursPerDay - 1)

                                for (h in startHour..endHour) {
                                    val segStart = maxOf(start, h * minutesPerHour)
                                    val segEnd = minOf(end, (h + 1) * minutesPerHour)
                                    if (segEnd > segStart) {
                                        val startInHour = segStart - h * minutesPerHour
                                        val endInHour = segEnd - h * minutesPerHour

                                        val snappedStartInHour = floorToGrid(startInHour, fiveMinuteStep).coerceIn(0, minutesPerHour)
                                        val snappedEndInHour = ceilToGrid(endInHour, fiveMinuteStep).coerceIn(0, minutesPerHour)

                                        if (snappedEndInHour > snappedStartInHour) {
                                            val x = (snappedStartInHour / fiveMinuteStep) * cellWidth
                                            val w = ((snappedEndInHour - snappedStartInHour) / fiveMinuteStep) * cellWidth
                                            val y = h * hourPx
                                            drawRect(
                                                color = rangeSelectionColor,
                                                topLeft = Offset(x, y),
                                                size = Size(w, hourPx)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                    .pointerInput(positionedEntries, hourHeight) {
                        fun isPointerOnEntry(offset: Offset): Boolean {
                            val hourPx = hourHeight.toPx()
                            val widthPx = size.width
                            if (hourPx <= 0f || widthPx <= 0f) return false

                            val hourIndex = (offset.y / hourPx).toInt().coerceIn(0, hoursPerDay - 1)

                            positionedEntries.forEach { positioned ->
                                val entry = positioned.entry
                                val startMinutes = getMinutesOfDay(entry.startTime)
                                val endMinutes = getMinutesOfDay(entry.endTime)
                                if (endMinutes <= startMinutes) return@forEach

                                val startHour = startMinutes / minutesPerHour
                                val endHour = (endMinutes - 1) / minutesPerHour
                                if (hourIndex !in startHour..endHour) return@forEach

                                val lanes = positioned.totalColumns.coerceAtLeast(1)
                                val laneHeightPx = hourPx / lanes
                                val laneTopPx = hourIndex * hourPx + positioned.columnIndex * laneHeightPx
                                val laneBottomPx = laneTopPx + laneHeightPx
                                if (offset.y < laneTopPx || offset.y > laneBottomPx) return@forEach

                                val segStart = maxOf(startMinutes, hourIndex * minutesPerHour)
                                val segEnd = minOf(endMinutes, (hourIndex + 1) * minutesPerHour)
                                if (segEnd <= segStart) return@forEach

                                val startInHour = segStart - hourIndex * minutesPerHour
                                val endInHour = segEnd - hourIndex * minutesPerHour

                                val snappedStartInHour = floorToGrid(startInHour, fiveMinuteStep).coerceIn(0, minutesPerHour)
                                val snappedEndInHour = ceilToGrid(endInHour, fiveMinuteStep).coerceIn(0, minutesPerHour)
                                if (snappedEndInHour <= snappedStartInHour) return@forEach

                        val cellWidthPx = widthPx / columnsPerHour.toFloat()
                        val xStart = (snappedStartInHour / fiveMinuteStep) * cellWidthPx
                        val xEnd = (snappedEndInHour / fiveMinuteStep) * cellWidthPx

                        if (offset.x in xStart..xEnd) {
                                    return true
                                }
                            }

                            return false
                        }

                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                if (isPointerOnEntry(offset)) {
                                    isRangeSelectionActive = false
                                    rangeSelection = null
                                    return@detectDragGesturesAfterLongPress
                                }

                                isRangeSelectionActive = true
                                val startMinutes = offsetToSnappedMinutes(offset, size.width.toFloat(), hourHeight.toPx())
                                rangeSelection = startMinutes to startMinutes
                            },
                            onDrag = { change, _ ->
                                if (!isRangeSelectionActive) return@detectDragGesturesAfterLongPress
                                change.consume()
                                val current = rangeSelection ?: return@detectDragGesturesAfterLongPress
                                val (start, _) = current
                                val endMinutes = offsetToSnappedMinutes(change.position, size.width.toFloat(), hourHeight.toPx())
                                rangeSelection = start to endMinutes
                            },
                            onDragEnd = {
                                if (!isRangeSelectionActive) return@detectDragGesturesAfterLongPress
                                isRangeSelectionActive = false

                                val selection = rangeSelection
                                rangeSelection = null
                                if (selection != null) {
                                    val (a, b) = selection
                                    val start = minOf(a, b)
                                    val end = maxOf(a, b)
                                    if (end > start) {
                                        onEmptyRangeCreate(
                                            minutesToLocalTime(start),
                                            minutesToLocalTime(end)
                                        )
                                    }
                                }
                            },
                            onDragCancel = {
                                isRangeSelectionActive = false
                                rangeSelection = null
                            }
                        )
                    }
            ) {
                val gridWidth = maxWidth
                val cellWidth = gridWidth / columnsPerHour.toFloat()

                positionedEntries.forEach { positioned ->
                    val entry = positioned.entry
                    val startMinutes = getMinutesOfDay(entry.startTime)
                    val endMinutes = getMinutesOfDay(entry.endTime)
                    if (endMinutes <= startMinutes) return@forEach

                    val startHour = startMinutes / minutesPerHour
                    val endHour = (endMinutes - 1) / minutesPerHour

                    val lanes = positioned.totalColumns.coerceAtLeast(1)
                    val laneHeight = hourHeight / lanes.toFloat()
                    val laneTopOffset = laneHeight * positioned.columnIndex.toFloat()

                    val isSelected = selectedEntryId == entry.id

                    for (h in startHour..endHour) {
                        val segStart = maxOf(startMinutes, h * minutesPerHour)
                        val segEnd = minOf(endMinutes, (h + 1) * minutesPerHour)
                        if (segEnd <= segStart) continue

                        val startInHour = segStart - h * minutesPerHour
                        val endInHour = segEnd - h * minutesPerHour

                        val snappedStartInHour = floorToGrid(startInHour, fiveMinuteStep).coerceIn(0, minutesPerHour)
                        val snappedEndInHour = ceilToGrid(endInHour, fiveMinuteStep).coerceIn(0, minutesPerHour)
                        if (snappedEndInHour <= snappedStartInHour) continue

                        val xOffset = cellWidth * (snappedStartInHour.toFloat() / fiveMinuteStep)
                        val segmentWidth = cellWidth * ((snappedEndInHour - snappedStartInHour).toFloat() / fiveMinuteStep)
                        val minSegmentWidth = cellWidth

                        val yOffset = (hourHeight * h.toFloat()) + laneTopOffset
                        val segmentHeight = (laneHeight - 2.dp).coerceAtLeast(8.dp)

                        val isFirstSlice = segStart == startMinutes
                        val isLastSlice = segEnd == endMinutes

                        val shape = RoundedCornerShape(
                            topStart = if (isFirstSlice) 4.dp else 0.dp,
                            bottomStart = if (isFirstSlice) 4.dp else 0.dp,
                            topEnd = if (isLastSlice) 4.dp else 0.dp,
                            bottomEnd = if (isLastSlice) 4.dp else 0.dp
                        )

                        val showDetails = (snappedEndInHour - snappedStartInHour) >= 10

                        TimeBlock(
                            entry = entry,
                            onClick = { onEntryClick(entry) },
                            onLongClick = { onEntryLongClick(entry) },
                            showDetails = showDetails,
                            shape = shape,
                            modifier = Modifier
                                .offset(x = xOffset, y = yOffset)
                                .width((segmentWidth.coerceAtLeast(minSegmentWidth) - 2.dp).coerceAtLeast(1.dp))
                                .height(segmentHeight)
                                .drawBehind {
                                    if (isSelected) {
                                        drawRect(color = selectedEntryOverlayColor)
                                    }
                                }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TimeAxis(
    hourHeight: Dp,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        (0..23).forEach { hour ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(hourHeight),
                contentAlignment = Alignment.TopEnd
            ) {
                Text(
                    text = String.format("%02d:00", hour),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 8.dp, top = 2.dp)
                )
            }
        }
    }
}

data class PositionedEntry(
    val entry: TimeEntry,
    val columnIndex: Int,
    val totalColumns: Int
)

private fun calculatePositionedEntries(entries: List<TimeEntry>): List<PositionedEntry> {
    if (entries.isEmpty()) return emptyList()

    val sortedEntries = entries.sortedBy { it.startTime }
    val result = mutableListOf<PositionedEntry>()

    val groups = mutableListOf<MutableList<TimeEntry>>()

    sortedEntries.forEach { entry ->
        val overlappingGroup = groups.find { group ->
            group.any { existing -> entriesOverlap(existing, entry) }
        }

        if (overlappingGroup != null) {
            overlappingGroup.add(entry)
        } else {
            groups.add(mutableListOf(entry))
        }
    }

    groups.forEach { group ->
        if (group.size == 1) {
            result.add(PositionedEntry(group[0], 0, 1))
        } else {
            val sortedGroup = group.sortedBy { it.startTime }
            val columns = mutableListOf<MutableList<TimeEntry>>()

            sortedGroup.forEach { entry ->
                val columnIndex = columns.indexOfFirst { column ->
                    column.none { existing -> entriesOverlap(existing, entry) }
                }

                if (columnIndex >= 0) {
                    columns[columnIndex].add(entry)
                    result.add(PositionedEntry(entry, columnIndex, 0))
                } else {
                    columns.add(mutableListOf(entry))
                    result.add(PositionedEntry(entry, columns.size - 1, 0))
                }
            }

            val totalColumns = columns.size
            group.forEach { entry ->
                val index = result.indexOfFirst { it.entry.id == entry.id }
                if (index >= 0) {
                    result[index] = result[index].copy(totalColumns = totalColumns)
                }
            }
        }
    }

    return result
}

private fun entriesOverlap(a: TimeEntry, b: TimeEntry): Boolean {
    return a.startTime < b.endTime && b.startTime < a.endTime
}

private fun getMinutesOfDay(instant: kotlinx.datetime.Instant): Int {
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return local.hour * 60 + local.minute
}
