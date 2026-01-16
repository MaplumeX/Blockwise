package com.maplume.blockwise.feature.statistics.presentation.chart

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.core.domain.model.CategoryStatistics

/**
 * Pie chart component for displaying category distribution.
 *
 * @param data List of category statistics to display.
 * @param modifier Modifier for the chart.
 * @param chartSize Size of the pie chart.
 * @param strokeWidth Width of the pie chart stroke.
 * @param maxLegendItems Maximum number of legend items to show.
 * @param centerContent Optional composable content for the center of the chart.
 */
@Composable
fun BlockwisePieChart(
    data: List<CategoryStatistics>,
    modifier: Modifier = Modifier,
    chartSize: Dp = 160.dp,
    strokeWidth: Dp = 24.dp,
    maxLegendItems: Int = 5,
    centerContent: @Composable (() -> Unit)? = null
) {
    if (data.isEmpty()) {
        EmptyChartPlaceholder(
            modifier = modifier,
            message = "暂无活动数据"
        )
        return
    }

    var animationProgress by remember { mutableFloatStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = tween(durationMillis = 300),
        label = "pie_animation"
    )

    LaunchedEffect(data) {
        animationProgress = 1f
    }

    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pie Chart
        Box(
            modifier = Modifier.size(chartSize),
            contentAlignment = Alignment.Center
        ) {
            PieChartCanvas(
                data = data,
                animationProgress = animatedProgress,
                strokeWidth = strokeWidth
            )
            centerContent?.invoke()
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Legend
        PieChartLegend(
            data = data,
            maxItems = maxLegendItems
        )
    }
}

@Composable
private fun PieChartCanvas(
    data: List<CategoryStatistics>,
    animationProgress: Float,
    strokeWidth: Dp
) {
    val colors = remember(data) {
        data.mapIndexed { index, stat ->
            try {
                Color(android.graphics.Color.parseColor(stat.colorHex))
            } catch (e: Exception) {
                ChartColors.palette[index % ChartColors.palette.size]
            }
        }
    }

    Canvas(modifier = Modifier.size(160.dp)) {
        val stroke = Stroke(
            width = strokeWidth.toPx(),
            cap = StrokeCap.Butt
        )

        var startAngle = -90f
        data.forEachIndexed { index, stat ->
            val sweepAngle = (stat.percentage / 100f) * 360f * animationProgress
            drawArc(
                color = colors[index],
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                style = stroke
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
private fun PieChartLegend(
    data: List<CategoryStatistics>,
    maxItems: Int
) {
    val displayItems = data.take(maxItems)
    val remainingCount = data.size - maxItems

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        displayItems.forEachIndexed { index, stat ->
            LegendItem(
                color = try {
                    Color(android.graphics.Color.parseColor(stat.colorHex))
                } catch (e: Exception) {
                    ChartColors.palette[index % ChartColors.palette.size]
                },
                name = stat.name,
                value = stat.formattedDuration,
                percentage = stat.percentage
            )
        }

        if (remainingCount > 0) {
            Text(
                text = "还有 $remainingCount 项...",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 24.dp)
            )
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    name: String,
    value: String,
    percentage: Float
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = name,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = String.format("%.1f%%", percentage),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
