package com.maplume.blockwise.feature.timeentry.presentation.timeblock

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.core.domain.model.TimeEntry
import kotlinx.datetime.TimeZone
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun TimeBlock(
    entry: TimeEntry,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier,
    showDetails: Boolean = true,
    shape: Shape = RoundedCornerShape(4.dp)
) {
    val backgroundColor = remember(entry.activity.colorHex) {
        parseColorHex(entry.activity.colorHex)
    }

    val contentColor = remember(backgroundColor) {
        if (isColorDark(backgroundColor)) Color.White else Color.Black
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor.copy(alpha = 0.85f))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 6.dp, vertical = 4.dp)
    ) {
        Column {
            // Activity name
            Text(
                text = entry.activity.name,
                style = MaterialTheme.typography.labelSmall,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            // Time range (if space allows)
            if (showDetails) {
                val timeRange = formatTimeRange(entry)
                Text(
                    text = timeRange,
                    style = MaterialTheme.typography.labelSmall,
                    color = contentColor.copy(alpha = 0.8f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CompactTimeBlock(
    entry: TimeEntry,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = remember(entry.activity.colorHex) {
        parseColorHex(entry.activity.colorHex)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(2.dp))
            .background(backgroundColor.copy(alpha = 0.85f))
            .combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
            .padding(horizontal = 4.dp, vertical = 2.dp)
    ) {
        val contentColor = if (isColorDark(backgroundColor)) Color.White else Color.Black
        Text(
            text = entry.activity.name,
            style = MaterialTheme.typography.labelSmall,
            color = contentColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

private fun formatTimeRange(entry: TimeEntry): String {
    val tz = TimeZone.currentSystemDefault()
    val startLocal = entry.startTime.toLocalDateTime(tz)
    val endLocal = entry.endTime.toLocalDateTime(tz)

    val isCrossDay = startLocal.date != endLocal.date

    val startTimeStr = String.format("%02d:%02d", startLocal.hour, startLocal.minute)
    val startStr = if (isCrossDay) {
        "${startLocal.date.monthNumber}/${startLocal.date.dayOfMonth} $startTimeStr"
    } else {
        startTimeStr
    }

    val endTimeStr = if (
        endLocal.hour == 0 &&
            endLocal.minute == 0 &&
            endLocal.date == LocalDate.fromEpochDays(startLocal.date.toEpochDays() + 1)
    ) {
        "24:00"
    } else {
        String.format("%02d:%02d", endLocal.hour, endLocal.minute)
    }

    val endStr = if (isCrossDay) {
        "${endLocal.date.monthNumber}/${endLocal.date.dayOfMonth} $endTimeStr"
    } else {
        endTimeStr
    }

    return "$startStr-$endStr"
}

internal fun parseColorHex(colorHex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color(0xFF4CAF50)
    }
}

internal fun isColorDark(color: Color): Boolean {
    val luminance = 0.299 * color.red + 0.587 * color.green + 0.114 * color.blue
    return luminance < 0.5
}
