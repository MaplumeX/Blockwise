package com.maplume.blockwise.feature.timeentry.presentation.timeline

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun UntrackedGapItem(
    startTime: Instant,
    endTime: Instant,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val axisWidth = 32.dp
    val lineWidth = 2.dp
    val lineColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .testTag("untrackedGapItem-${startTime.toEpochMilliseconds()}-${endTime.toEpochMilliseconds()}")
    ) {
        Canvas(modifier = Modifier.width(axisWidth).height(72.dp)) {
            val axisCenterX = axisWidth.toPx() / 2
            drawLine(
                color = lineColor,
                start = Offset(axisCenterX, 0f),
                end = Offset(axisCenterX, size.height),
                strokeWidth = lineWidth.toPx()
            )
        }

        Box(
            modifier = Modifier
                .padding(start = 0.dp)
                .padding(bottom = 8.dp)
                .fillMaxWidth()
                .testTag("untrackedGapCard-${startTime.toEpochMilliseconds()}-${endTime.toEpochMilliseconds()}")
                .clip(RoundedCornerShape(12.dp))
                .drawDashedRoundRectBorder(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f),
                    cornerRadius = 12.dp,
                    strokeWidth = 1.dp,
                    dashLength = 8.dp,
                    gapLength = 6.dp
                )
                .combinedClickable(onClick = onClick)
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.0f))
                .padding(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "未追踪时间段",
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Text(
                        text = "点击创建",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }

                TimeRangeChip(startTime = startTime, endTime = endTime)
            }
        }
    }
}

@Composable
private fun TimeRangeChip(
    startTime: Instant,
    endTime: Instant
) {
    Surface(
        shape = RoundedCornerShape(4.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    ) {
        Text(
            text = formatGapTimeRange(startTime, endTime),
            style = MaterialTheme.typography.bodySmall.copy(fontFamily = FontFamily.Monospace),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
        )
    }
}

private fun formatGapTimeRange(startTime: Instant, endTime: Instant): String {
    val tz = TimeZone.currentSystemDefault()
    val startLocal = startTime.toLocalDateTime(tz)
    val endLocal = endTime.toLocalDateTime(tz)

    val startStr = String.format("%02d:%02d", startLocal.hour, startLocal.minute)

    val endStr = if (
        endLocal.hour == 0 &&
            endLocal.minute == 0 &&
            endLocal.date.toEpochDays() == startLocal.date.toEpochDays() + 1
    ) {
        "24:00"
    } else {
        String.format("%02d:%02d", endLocal.hour, endLocal.minute)
    }

    return "$startStr - $endStr"
}

private fun Modifier.drawDashedRoundRectBorder(
    color: androidx.compose.ui.graphics.Color,
    cornerRadius: androidx.compose.ui.unit.Dp,
    strokeWidth: androidx.compose.ui.unit.Dp,
    dashLength: androidx.compose.ui.unit.Dp,
    gapLength: androidx.compose.ui.unit.Dp
): Modifier {
    return drawBehind {
        drawRoundRect(
            color = color,
            cornerRadius = CornerRadius(cornerRadius.toPx()),
            style = Stroke(
                width = strokeWidth.toPx(),
                pathEffect = PathEffect.dashPathEffect(
                    floatArrayOf(dashLength.toPx(), gapLength.toPx()),
                    0f
                )
            )
        )
    }
}
