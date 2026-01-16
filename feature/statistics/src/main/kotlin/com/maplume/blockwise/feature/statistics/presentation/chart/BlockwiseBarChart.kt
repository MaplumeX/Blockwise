package com.maplume.blockwise.feature.statistics.presentation.chart

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.core.domain.model.DailyTrend
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberColumnCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.component.rememberLineComponent
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.columnSeries
import com.patrykandpatrick.vico.core.cartesian.layer.ColumnCartesianLayer
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Bar chart component for displaying daily trends.
 *
 * @param data List of daily trends to display.
 * @param modifier Modifier for the chart.
 * @param height Height of the chart.
 */
@Composable
fun BlockwiseBarChart(
    data: List<DailyTrend>,
    modifier: Modifier = Modifier,
    height: Dp = ChartDimensions.DefaultHeight
) {
    if (data.isEmpty() || data.all { it.totalMinutes == 0 }) {
        EmptyChartPlaceholder(
            modifier = modifier,
            height = height,
            message = "暂无趋势数据"
        )
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    val modelProducer = remember { CartesianChartModelProducer() }

    androidx.compose.runtime.LaunchedEffect(data) {
        withContext(Dispatchers.Default) {
            modelProducer.runTransaction {
                columnSeries {
                    series(data.map { it.totalMinutes / 60f }) // Convert to hours
                }
            }
        }
    }

    val columnLayer = rememberColumnCartesianLayer(
        columnProvider = ColumnCartesianLayer.ColumnProvider.series(
            rememberLineComponent(
                fill = fill(primaryColor),
                thickness = ChartDimensions.BarWidth,
                shape = CorneredShape.rounded(allPercent = 40)
            )
        )
    )

    CartesianChartHost(
        chart = rememberCartesianChart(columnLayer),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    )
}

/**
 * Simplified bar chart for preview or small displays.
 *
 * @param values List of values to display.
 * @param modifier Modifier for the chart.
 * @param height Height of the chart.
 */
@Composable
fun SimpleBarChart(
    values: List<Float>,
    modifier: Modifier = Modifier,
    height: Dp = 120.dp
) {
    if (values.isEmpty() || values.all { it == 0f }) {
        EmptyChartPlaceholder(
            modifier = modifier,
            height = height,
            message = "暂无数据"
        )
        return
    }

    val primaryColor = MaterialTheme.colorScheme.primary

    val modelProducer = remember { CartesianChartModelProducer() }

    androidx.compose.runtime.LaunchedEffect(values) {
        withContext(Dispatchers.Default) {
            modelProducer.runTransaction {
                columnSeries {
                    series(values)
                }
            }
        }
    }

    val columnLayer = rememberColumnCartesianLayer(
        columnProvider = ColumnCartesianLayer.ColumnProvider.series(
            rememberLineComponent(
                fill = fill(primaryColor),
                thickness = ChartDimensions.BarWidth,
                shape = CorneredShape.rounded(allPercent = 40)
            )
        )
    )

    CartesianChartHost(
        chart = rememberCartesianChart(columnLayer),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    )
}
