package com.maplume.blockwise.feature.timeentry.presentation.timeline

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Date group header for the timeline.
 * Shows the date and total duration for that day.
 */
@Composable
fun DateGroupHeader(
    date: LocalDate,
    totalMinutes: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Date display
        Text(
            text = formatDate(date),
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )

        // Total duration badge
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.primaryContainer)
                .padding(horizontal = 10.dp, vertical = 4.dp)
        ) {
            Text(
                text = formatTotalDuration(totalMinutes),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

/**
 * Sticky date group header for LazyColumn.
 */
@Composable
fun StickyDateGroupHeader(
    date: LocalDate,
    totalMinutes: Int,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
    ) {
        DateGroupHeader(
            date = date,
            totalMinutes = totalMinutes,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
    }
}

/**
 * Format date for display.
 */
private fun formatDate(date: LocalDate): String {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    val yesterday = LocalDate(today.year, today.month, today.dayOfMonth - 1)

    return when (date) {
        today -> "今天"
        yesterday -> "昨天"
        else -> {
            val dayOfWeek = when (date.dayOfWeek.ordinal) {
                0 -> "周一"
                1 -> "周二"
                2 -> "周三"
                3 -> "周四"
                4 -> "周五"
                5 -> "周六"
                6 -> "周日"
                else -> ""
            }
            "${date.monthNumber}月${date.dayOfMonth}日 $dayOfWeek"
        }
    }
}

/**
 * Format total duration for display.
 */
private fun formatTotalDuration(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60

    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
}

// =============================================================================
// Previews
// =============================================================================

@Preview(showBackground = true)
@Composable
private fun DateGroupHeaderTodayPreview() {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    BlockwiseTheme {
        DateGroupHeader(
            date = today,
            totalMinutes = 480 // 8 hours
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DateGroupHeaderOtherDayPreview() {
    BlockwiseTheme {
        DateGroupHeader(
            date = LocalDate(2024, 1, 15),
            totalMinutes = 360 // 6 hours
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun StickyDateGroupHeaderPreview() {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    BlockwiseTheme {
        StickyDateGroupHeader(
            date = today,
            totalMinutes = 240 // 4 hours
        )
    }
}
