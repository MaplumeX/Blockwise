package com.maplume.blockwise.feature.statistics.presentation.chart

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.BarChart
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Placeholder displayed when chart has no data.
 *
 * @param modifier Modifier for the placeholder.
 * @param height Height of the placeholder area.
 * @param message Message to display.
 */
@Composable
fun EmptyChartPlaceholder(
    modifier: Modifier = Modifier,
    height: Dp = ChartDimensions.DefaultHeight,
    message: String = "暂无数据"
) {
    Box(
        modifier = modifier
            .height(height)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.BarChart,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                textAlign = TextAlign.Center
            )
        }
    }
}

/**
 * Loading placeholder for charts.
 *
 * @param modifier Modifier for the placeholder.
 * @param height Height of the placeholder area.
 */
@Composable
fun LoadingChartPlaceholder(
    modifier: Modifier = Modifier,
    height: Dp = ChartDimensions.DefaultHeight
) {
    Box(
        modifier = modifier
            .height(height)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "加载中...",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
