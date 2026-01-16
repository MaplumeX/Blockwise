package com.maplume.blockwise.feature.timeentry.presentation.timeblock

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.core.domain.model.TimeEntry
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import java.time.format.TextStyle
import java.util.Locale

/**
 * Week view showing 7 days with time blocks.
 */
@Composable
fun TimeBlockWeekView(
    weekStart: LocalDate,
    entriesByDay: Map<LocalDate, List<TimeEntry>>,
    onEntryClick: (TimeEntry) -> Unit,
    onEntryLongClick: (TimeEntry) -> Unit,
    onEmptySlotClick: (LocalDate, LocalTime) -> Unit,
    onDayHeaderClick: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    hourHeight: Dp = 48.dp
) {
    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()
    val timeAxisWidth = 40.dp
    val dayColumnWidth = 100.dp

    val today = remember {
        Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    Column(modifier = modifier) {
        // Day headers (fixed)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            // Empty space for time axis
            Spacer(modifier = Modifier.width(timeAxisWidth))

            // Day headers
            Row(
                modifier = Modifier
                    .horizontalScroll(horizontalScrollState)
            ) {
                (0..6).forEach { dayOffset ->
                    val date = weekStart.plus(dayOffset, DateTimeUnit.DAY)
                    val isToday = date == today
                    val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY ||
                        date.dayOfWeek == DayOfWeek.SUNDAY

                    DayHeader(
                        date = date,
                        isToday = isToday,
                        isWeekend = isWeekend,
                        onClick = { onDayHeaderClick(date) },
                        modifier = Modifier.width(dayColumnWidth)
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)

        // Scrollable content
        Row(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(verticalScrollState)
        ) {
            // Time axis (fixed horizontally)
            WeekTimeAxis(
                hourHeight = hourHeight,
                modifier = Modifier.width(timeAxisWidth)
            )

            // Day columns (scrollable horizontally)
            Row(
                modifier = Modifier
                    .horizontalScroll(horizontalScrollState)
            ) {
                (0..6).forEach { dayOffset ->
                    val date = weekStart.plus(dayOffset, DateTimeUnit.DAY)
                    val entries = entriesByDay[date] ?: emptyList()
                    val isWeekend = date.dayOfWeek == DayOfWeek.SATURDAY ||
                        date.dayOfWeek == DayOfWeek.SUNDAY

                    WeekDayColumn(
                        date = date,
                        entries = entries,
                        isWeekend = isWeekend,
                        hourHeight = hourHeight,
                        onEntryClick = onEntryClick,
                        onEntryLongClick = onEntryLongClick,
                        onEmptySlotClick = { time -> onEmptySlotClick(date, time) },
                        modifier = Modifier.width(dayColumnWidth)
                    )

                    if (dayOffset < 6) {
                        VerticalDivider(
                            modifier = Modifier.height(hourHeight * 24),
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Day header for week view.
 */
@Composable
private fun DayHeader(
    date: LocalDate,
    isToday: Boolean,
    isWeekend: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayName = remember(date) {
        date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    }

    Column(
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = dayName,
            style = MaterialTheme.typography.labelSmall,
            color = when {
                isToday -> MaterialTheme.colorScheme.primary
                isWeekend -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                else -> MaterialTheme.colorScheme.onSurfaceVariant
            }
        )

        Spacer(modifier = Modifier.height(2.dp))

        Box(
            modifier = Modifier
                .then(
                    if (isToday) {
                        Modifier.background(
                            MaterialTheme.colorScheme.primary,
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                    } else {
                        Modifier
                    }
                )
                .padding(horizontal = 8.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "${date.dayOfMonth}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    isToday -> MaterialTheme.colorScheme.onPrimary
                    isWeekend -> MaterialTheme.colorScheme.error.copy(alpha = 0.7f)
                    else -> MaterialTheme.colorScheme.onSurface
                }
            )
        }
    }
}

/**
 * Time axis for week view (more compact).
 */
@Composable
private fun WeekTimeAxis(
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
                    text = String.format("%02d", hour),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(end = 4.dp, top = 2.dp)
                )
            }
        }
    }
}

/**
 * Single day column in week view.
 */
@Composable
private fun WeekDayColumn(
    date: LocalDate,
    entries: List<TimeEntry>,
    isWeekend: Boolean,
    hourHeight: Dp,
    onEntryClick: (TimeEntry) -> Unit,
    onEntryLongClick: (TimeEntry) -> Unit,
    onEmptySlotClick: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    val positionedEntries = remember(entries) {
        calculatePositionedEntriesForWeek(entries)
    }

    BoxWithConstraints(
        modifier = modifier
            .height(hourHeight * 24)
            .background(
                if (isWeekend) {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                } else {
                    MaterialTheme.colorScheme.surface
                }
            )
    ) {
        val columnWidth = maxWidth

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
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
                    )
                }
            }
        }

        // Time blocks
        positionedEntries.forEach { positioned ->
            val entry = positioned.entry
            val startMinutes = getMinutesOfDayFromInstant(entry.startTime)
            val endMinutes = getMinutesOfDayFromInstant(entry.endTime)

            val topOffset = (startMinutes * hourHeight.value / 60).dp
            val blockHeight = ((endMinutes - startMinutes) * hourHeight.value / 60).dp
                .coerceAtLeast(16.dp)

            // Calculate width based on overlap
            val blockWidth = columnWidth / positioned.totalColumns
            val leftOffset = blockWidth * positioned.columnIndex

            CompactTimeBlock(
                entry = entry,
                onClick = { onEntryClick(entry) },
                onLongClick = { onEntryLongClick(entry) },
                modifier = Modifier
                    .offset(x = leftOffset, y = topOffset)
                    .width(blockWidth - 1.dp)
                    .height(blockHeight)
            )
        }
    }
}

/**
 * Calculate positioned entries for week view (simpler overlap handling).
 */
private fun calculatePositionedEntriesForWeek(entries: List<TimeEntry>): List<PositionedEntry> {
    if (entries.isEmpty()) return emptyList()

    val sortedEntries = entries.sortedBy { it.startTime }
    val result = mutableListOf<PositionedEntry>()
    val columns = mutableListOf<MutableList<TimeEntry>>()

    sortedEntries.forEach { entry ->
        // Find first column where this entry doesn't overlap
        val columnIndex = columns.indexOfFirst { column ->
            column.none { existing -> entriesOverlapCheck(existing, entry) }
        }

        if (columnIndex >= 0) {
            columns[columnIndex].add(entry)
        } else {
            columns.add(mutableListOf(entry))
        }
    }

    val totalColumns = columns.size.coerceAtLeast(1)

    columns.forEachIndexed { colIndex, column ->
        column.forEach { entry ->
            result.add(PositionedEntry(entry, colIndex, totalColumns))
        }
    }

    return result
}

/**
 * Check if two entries overlap in time.
 */
private fun entriesOverlapCheck(a: TimeEntry, b: TimeEntry): Boolean {
    return a.startTime < b.endTime && b.startTime < a.endTime
}

/**
 * Get minutes of day from an Instant.
 */
private fun getMinutesOfDayFromInstant(instant: kotlinx.datetime.Instant): Int {
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return local.hour * 60 + local.minute
}
