package com.maplume.blockwise.feature.timeentry.presentation.timeblock

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.gestures.detectTapGestures
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
    val minutesPerDay = 24 * 60

    fun snapMinutesToNearestGrid(minutes: Int, gridMinutes: Int): Int {
        return ((minutes + gridMinutes / 2) / gridMinutes) * gridMinutes
    }

    fun minutesToLocalTime(minutes: Int): LocalTime {
        if (minutes >= minutesPerDay) return LocalTime(0, 0)
        val clamped = minutes.coerceAtLeast(0)
        val hours = clamped / 60
        val mins = clamped % 60
        return LocalTime(hours, mins)
    }

    fun yToSnappedMinutes(yPx: Float, heightPx: Float): Int {
        val minutesPerPx = minutesPerDay.toFloat() / heightPx
        val rawMinutes = (yPx * minutesPerPx).roundToInt()
        return snapMinutesToNearestGrid(rawMinutes, fiveMinuteStep)
            .coerceIn(0, minutesPerDay)
    }

    fun minutesToY(minutes: Int, heightPx: Float): Float {
        return (minutes.toFloat() / minutesPerDay) * heightPx
    }

    var rangeSelection by remember { mutableStateOf<Pair<Int, Int>?>(null) }

    // Calculate positioned entries with overlap handling
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
            // Time axis
            TimeAxis(
                hourHeight = hourHeight,
                modifier = Modifier.width(timeAxisWidth)
            )

            // Time blocks area
            BoxWithConstraints(
                modifier = Modifier
                    .weight(1f)
                    .height(hourHeight * 24)
                    .drawBehind {
                        val hourPx = hourHeight.toPx()
                        val width = size.width

                        for (h in 0 until 24) {
                            val y = h * hourPx
                            drawLine(
                                color = gridColor,
                                start = Offset(0f, y),
                                end = Offset(width, y),
                                strokeWidth = 1.dp.toPx()
                            )

                            val fiveMinPx = hourPx / 12
                            for (m in 1 until 12) {
                                val subY = y + m * fiveMinPx
                                drawLine(
                                    color = subGridColor,
                                    start = Offset(0f, subY),
                                    end = Offset(width, subY),
                                    strokeWidth = 0.5.dp.toPx()
                                )
                            }
                        }
                        drawLine(
                            color = gridColor,
                            start = Offset(0f, size.height),
                            end = Offset(width, size.height),
                            strokeWidth = 1.dp.toPx()
                        )

                        val selection = rangeSelection
                        if (selection != null) {
                            val (a, b) = selection
                            val start = minOf(a, b)
                            val end = maxOf(a, b)

                            if (end > start) {
                                val topPx = minutesToY(start, size.height)
                                val bottomPx = minutesToY(end, size.height)

                                drawRect(
                                    color = rangeSelectionColor,
                                    topLeft = Offset(0f, topPx),
                                    size = Size(width, bottomPx - topPx)
                                )
                            }
                        }
                    }
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = { offset ->
                                val minutes = yToSnappedMinutes(offset.y, size.height.toFloat())
                                val snapped = minutes.coerceIn(0, minutesPerDay - fiveMinuteStep)
                                onEmptySlotClick(minutesToLocalTime(snapped))
                            }
                        )
                    }
                    .pointerInput(Unit) {
                        detectDragGesturesAfterLongPress(
                            onDragStart = { offset ->
                                val startMinutes = yToSnappedMinutes(offset.y, size.height.toFloat())
                                rangeSelection = startMinutes to startMinutes
                            },
                            onDrag = { change, _ ->
                                change.consume()
                                val current = rangeSelection
                                if (current != null) {
                                    val (start, _) = current
                                    val endMinutes = yToSnappedMinutes(change.position.y, size.height.toFloat())
                                    rangeSelection = start to endMinutes
                                }
                            },
                            onDragEnd = {
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
                                rangeSelection = null
                            }
                        )
                    }
            ) {
                val blockAreaWidth = maxWidth

                positionedEntries.forEach { positioned ->
                    val entry = positioned.entry
                    val startMinutes = getMinutesOfDay(entry.startTime)
                    val endMinutes = getMinutesOfDay(entry.endTime)

                    val startHour = startMinutes / 60
                    val endHour = (endMinutes - 1) / 60

                    for (h in startHour..endHour) {
                        val segStart = maxOf(startMinutes, h * 60)
                        val segEnd = minOf(endMinutes, (h + 1) * 60)

                        if (segEnd > segStart) {
                            val topOffset = (segStart * hourHeight.value / 60).dp
                            val segmentHeight = ((segEnd - segStart) * hourHeight.value / 60).dp
                                .coerceAtLeast((hourHeight.value / 60f * fiveMinuteStep).dp)

                            val isFirst = (segStart == startMinutes)
                            val isLast = (segEnd == endMinutes)

                            val shape = RoundedCornerShape(
                                topStart = if (isFirst) 4.dp else 0.dp,
                                topEnd = if (isFirst) 4.dp else 0.dp,
                                bottomStart = if (isLast) 4.dp else 0.dp,
                                bottomEnd = if (isLast) 4.dp else 0.dp
                            )

                            // Calculate width based on overlap
                            val columnWidth = blockAreaWidth / positioned.totalColumns
                            val leftOffset = columnWidth * positioned.columnIndex

                            val isSelected = selectedEntryId == entry.id

                            TimeBlock(
                                entry = entry,
                                onClick = { onEntryClick(entry) },
                                onLongClick = { onEntryLongClick(entry) },
                                showDetails = segmentHeight >= 20.dp,
                                shape = shape,
                                modifier = Modifier
                                    .offset(x = leftOffset, y = topOffset)
                                    .width(columnWidth - 2.dp)
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
}

/**
 * Time axis showing hours.
 */
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

/**
 * Data class for positioned entry with overlap information.
 */
data class PositionedEntry(
    val entry: TimeEntry,
    val columnIndex: Int,
    val totalColumns: Int
)

/**
 * Calculate positioned entries with overlap handling.
 * Entries that overlap are placed in adjacent columns.
 */
private fun calculatePositionedEntries(entries: List<TimeEntry>): List<PositionedEntry> {
    if (entries.isEmpty()) return emptyList()

    val sortedEntries = entries.sortedBy { it.startTime }
    val result = mutableListOf<PositionedEntry>()

    // Group overlapping entries
    val groups = mutableListOf<MutableList<TimeEntry>>()

    sortedEntries.forEach { entry ->
        // Find a group that this entry overlaps with
        val overlappingGroup = groups.find { group ->
            group.any { existing -> entriesOverlap(existing, entry) }
        }

        if (overlappingGroup != null) {
            overlappingGroup.add(entry)
        } else {
            groups.add(mutableListOf(entry))
        }
    }

    // Assign columns within each group
    groups.forEach { group ->
        if (group.size == 1) {
            result.add(PositionedEntry(group[0], 0, 1))
        } else {
            // Sort by start time within group
            val sortedGroup = group.sortedBy { it.startTime }
            val columns = mutableListOf<MutableList<TimeEntry>>()

            sortedGroup.forEach { entry ->
                // Find first column where this entry doesn't overlap
                val columnIndex = columns.indexOfFirst { column ->
                    column.none { existing -> entriesOverlap(existing, entry) }
                }

                if (columnIndex >= 0) {
                    columns[columnIndex].add(entry)
                    result.add(PositionedEntry(entry, columnIndex, 0)) // totalColumns updated later
                } else {
                    columns.add(mutableListOf(entry))
                    result.add(PositionedEntry(entry, columns.size - 1, 0))
                }
            }

            // Update totalColumns for all entries in this group
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

/**
 * Check if two entries overlap in time.
 */
private fun entriesOverlap(a: TimeEntry, b: TimeEntry): Boolean {
    return a.startTime < b.endTime && b.startTime < a.endTime
}

/**
 * Get minutes of day from an Instant.
 */
private fun getMinutesOfDay(instant: kotlinx.datetime.Instant): Int {
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return local.hour * 60 + local.minute
}
