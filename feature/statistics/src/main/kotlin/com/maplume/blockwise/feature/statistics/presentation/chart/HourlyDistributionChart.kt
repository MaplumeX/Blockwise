package com.maplume.blockwise.feature.statistics.presentation.chart

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.core.domain.model.HourlyPattern

/**
 * Hourly distribution chart showing time usage across 24 hours.
 *
 * @param data List of hourly patterns (should contain 24 items, 0-23).
 * @param modifier Modifier for the chart.
 * @param height Height of the chart.
 * @param barSpacing Spacing between bars.
 */
@Composable
fun HourlyDistributionChart(
    data: List<HourlyPattern>,
    modifier: Modifier = Modifier,
    height: Dp = ChartDimensions.DefaultHeight,
    barSpacing: Dp = 2.dp
) {
    if (data.isEmpty() || data.all { it.totalMinutes == 0 }) {
        EmptyChartPlaceholder(
            modifier = modifier,
            height = height,
            message = "暂无时段数据"
        )
        return
    }

    var animationProgress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 300),
        label = "hourly_animation"
    )

    LaunchedEffect(data) {
        animationProgress = 1f
    }

    val maxMinutes = data.maxOfOrNull { it.totalMinutes } ?: 1

    Column(modifier = modifier.fillMaxWidth()) {
        // Chart
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height)
                .padding(horizontal = 8.dp)
        ) {
            Canvas(modifier = Modifier.matchParentSize()) {
                val barWidth = (size.width - (barSpacing.toPx() * 23)) / 24
                val chartHeight = size.height - 24.dp.toPx() // Leave space for labels

                data.forEachIndexed { index, pattern ->
                    val barHeight = if (maxMinutes > 0) {
                        (pattern.totalMinutes.toFloat() / maxMinutes) * chartHeight * animatedProgress
                    } else {
                        0f
                    }

                    val x = index * (barWidth + barSpacing.toPx())
                    val y = chartHeight - barHeight

                    val color = ChartColors.getHourColor(pattern.hour)

                    drawRoundRect(
                        color = color,
                        topLeft = Offset(x, y),
                        size = Size(barWidth, barHeight),
                        cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // Hour labels
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            listOf("0", "6", "12", "18", "24").forEach { label ->
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Legend
        HourlyLegend()
    }
}

@Composable
private fun HourlyLegend() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendDot(color = ChartColors.Morning, label = "早晨")
        Spacer(modifier = Modifier.width(12.dp))
        LegendDot(color = ChartColors.Afternoon, label = "下午")
        Spacer(modifier = Modifier.width(12.dp))
        LegendDot(color = ChartColors.Evening, label = "晚间")
        Spacer(modifier = Modifier.width(12.dp))
        LegendDot(color = ChartColors.Night, label = "夜间")
    }
}

@Composable
private fun LegendDot(
    color: Color,
    label: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.height(8.dp).width(8.dp)) {
            drawCircle(color = color)
        }
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * Compact hourly distribution for summary cards.
 *
 * @param data List of hourly patterns.
 * @param modifier Modifier for the chart.
 * @param height Height of the chart.
 */
@Composable
fun CompactHourlyChart(
    data: List<HourlyPattern>,
    modifier: Modifier = Modifier,
    height: Dp = 60.dp
) {
    if (data.isEmpty() || data.all { it.totalMinutes == 0 }) {
        return
    }

    val maxMinutes = data.maxOfOrNull { it.totalMinutes } ?: 1

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    ) {
        val barWidth = size.width / 24
        val chartHeight = size.height

        data.forEach { pattern ->
            val barHeight = if (maxMinutes > 0) {
                (pattern.totalMinutes.toFloat() / maxMinutes) * chartHeight
            } else {
                0f
            }

            val x = pattern.hour * barWidth
            val y = chartHeight - barHeight

            val color = ChartColors.getHourColor(pattern.hour)

            drawRect(
                color = color.copy(alpha = 0.7f),
                topLeft = Offset(x, y),
                size = Size(barWidth - 1.dp.toPx(), barHeight)
            )
        }
    }
}
