package com.maplume.blockwise.feature.statistics.presentation.chart

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.maplume.blockwise.core.domain.model.DailyTrend
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Line chart component for displaying trends over time.
 *
 * @param data List of daily trends to display.
 * @param modifier Modifier for the chart.
 * @param height Height of the chart.
 * @param showPoints Whether to show data points on the line.
 */
@Composable
fun BlockwiseLineChart(
    data: List<DailyTrend>,
    modifier: Modifier = Modifier,
    height: Dp = ChartDimensions.DefaultHeight,
    @Suppress("UNUSED_PARAMETER") showPoints: Boolean = true
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

    LaunchedEffect(data) {
        withContext(Dispatchers.Default) {
            modelProducer.runTransaction {
                lineSeries {
                    series(data.map { it.totalMinutes / 60f }) // Convert to hours
                }
            }
        }
    }

    val lineLayer = rememberLineCartesianLayer(
        lineProvider = LineCartesianLayer.LineProvider.series(
            LineCartesianLayer.Line(
                fill = LineCartesianLayer.LineFill.single(fill(primaryColor))
            )
        )
    )

    CartesianChartHost(
        chart = rememberCartesianChart(lineLayer),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    )
}

/**
 * Multi-line chart for comparing multiple data series.
 *
 * @param dataSeries Map of series name to data values.
 * @param modifier Modifier for the chart.
 * @param height Height of the chart.
 */
@Composable
fun MultiLineChart(
    dataSeries: Map<String, List<Float>>,
    modifier: Modifier = Modifier,
    height: Dp = ChartDimensions.DefaultHeight
) {
    if (dataSeries.isEmpty() || dataSeries.values.all { it.isEmpty() }) {
        EmptyChartPlaceholder(
            modifier = modifier,
            height = height,
            message = "暂无对比数据"
        )
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(dataSeries) {
        withContext(Dispatchers.Default) {
            modelProducer.runTransaction {
                lineSeries {
                    dataSeries.values.forEach { values ->
                        series(values)
                    }
                }
            }
        }
    }

    val lines = dataSeries.keys.mapIndexed { index, _ ->
        val color = ChartColors.palette[index % ChartColors.palette.size]
        LineCartesianLayer.Line(
            fill = LineCartesianLayer.LineFill.single(fill(color))
        )
    }

    val lineLayer = rememberLineCartesianLayer(
        lineProvider = LineCartesianLayer.LineProvider.series(lines)
    )

    CartesianChartHost(
        chart = rememberCartesianChart(lineLayer),
        modelProducer = modelProducer,
        modifier = modifier
            .fillMaxWidth()
            .height(height)
    )
}
