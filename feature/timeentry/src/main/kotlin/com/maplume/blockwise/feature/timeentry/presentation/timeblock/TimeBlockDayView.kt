package com.maplume.blockwise.feature.timeentry.presentation.timeblock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.core.domain.model.TimeEntry
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Day view showing 24-hour timeline with time blocks.
 */
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

    // Calculate positioned entries with overlap handling
    val positionedEntries = remember(entries) {
        calculatePositionedEntries(entries)
    }

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
            ) {
                val blockAreaWidth = maxWidth

                // Hour grid lines and clickable areas
                Column(modifier = Modifier.fillMaxSize()) {
                    (0..23).forEach { hour ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(hourHeight)
                                .clickable {
                                    onEmptySlotClick(LocalTime(hour, 0))
                                }
                        ) {
                            HorizontalDivider(
                                modifier = Modifier.align(Alignment.TopStart),
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                            )
                        }
                    }
                }

                // Time blocks
                positionedEntries.forEach { positioned ->
                    val entry = positioned.entry
                    val startMinutes = getMinutesOfDay(entry.startTime)
                    val endMinutes = getMinutesOfDay(entry.endTime)

                    val topOffset = (startMinutes * hourHeight.value / 60).dp
                    val blockHeight = ((endMinutes - startMinutes) * hourHeight.value / 60).dp
                        .coerceAtLeast(20.dp)

                    // Calculate width based on overlap
                    val columnWidth = blockAreaWidth / positioned.totalColumns
                    val leftOffset = columnWidth * positioned.columnIndex

                    TimeBlock(
                        entry = entry,
                        onClick = { onEntryClick(entry) },
                        onLongClick = { onEntryLongClick(entry) },
                        showDetails = blockHeight >= 40.dp,
                        modifier = Modifier
                            .offset(x = leftOffset, y = topOffset)
                            .width(columnWidth - 2.dp)
                            .height(blockHeight)
                    )
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
