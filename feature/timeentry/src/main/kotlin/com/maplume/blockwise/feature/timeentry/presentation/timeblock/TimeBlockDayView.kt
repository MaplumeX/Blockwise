package com.maplume.blockwise.feature.timeentry.presentation.timeblock

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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.core.domain.model.TimeEntry
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@Composable
fun TimeBlockDayView(
    date: LocalDate,
    entries: List<TimeEntry>,
    onEntryClick: (TimeEntry) -> Unit,
    onEntryLongClick: (TimeEntry) -> Unit,
    onEmptySlotClick: (LocalTime) -> Unit,
    modifier: Modifier = Modifier,
    hourHeight: Dp = 60.dp
) {
    val scrollState = rememberScrollState()
    val timeAxisWidth = 48.dp
    val fiveMinuteStep = 5

    // Calculate positioned entries with overlap handling
    val positionedEntries = remember(entries) {
        calculatePositionedEntries(entries)
    }

    val gridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
    val subGridColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.2f)

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
                    }
                    .pointerInput(Unit) {
                        detectTapGestures { offset ->
                            val minutesPerPx = (24f * 60f) / size.height
                            val minutesFloat = offset.y * minutesPerPx
                            val snapped = ((minutesFloat / fiveMinuteStep).toInt() * fiveMinuteStep)
                                .coerceIn(0, (24 * 60) - fiveMinuteStep)

                            val h = snapped / 60
                            val m = snapped % 60
                            onEmptySlotClick(LocalTime(h, m))
                        }
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
