package com.maplume.blockwise.feature.statistics.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maplume.blockwise.core.designsystem.component.BlockwiseEmptyState
import com.maplume.blockwise.core.designsystem.component.BlockwiseErrorState
import com.maplume.blockwise.core.designsystem.component.LoadingIndicator
import com.maplume.blockwise.feature.statistics.presentation.chart.BlockwiseBarChart
import com.maplume.blockwise.feature.statistics.presentation.chart.BlockwisePieChart
import com.maplume.blockwise.feature.statistics.presentation.chart.HourlyDistributionChart
import com.maplume.blockwise.feature.statistics.presentation.component.PeriodNavigationBar
import com.maplume.blockwise.feature.statistics.presentation.component.PeriodSelector
import com.maplume.blockwise.feature.statistics.presentation.component.StatisticsSummaryCard

/**
 * Statistics screen displaying time usage analytics.
 *
 * @param onNavigateToActivityDetail Callback to navigate to activity type detail.
 * @param onNavigateToTagDetail Callback to navigate to tag detail.
 * @param viewModel ViewModel for the statistics screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    onNavigateToActivityDetail: (Long) -> Unit = {},
    onNavigateToTagDetail: (Long) -> Unit = {},
    viewModel: StatisticsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "统计分析",
                        fontWeight = FontWeight.Bold
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Period type selector
            PeriodSelector(
                selectedType = uiState.periodType,
                periodLabel = uiState.periodLabel,
                isCurrentPeriod = uiState.isCurrentPeriod,
                onTypeSelected = { viewModel.onEvent(StatisticsEvent.SelectPeriodType(it)) },
                onPreviousClick = { viewModel.onEvent(StatisticsEvent.NavigateToPrevious) },
                onNextClick = { viewModel.onEvent(StatisticsEvent.NavigateToNext) }
            )

            // Period navigation
            PeriodNavigationBar(
                periodLabel = uiState.periodLabel,
                isCurrentPeriod = uiState.isCurrentPeriod,
                onPreviousClick = { viewModel.onEvent(StatisticsEvent.NavigateToPrevious) },
                onNextClick = { viewModel.onEvent(StatisticsEvent.NavigateToNext) }
            )

            // Content
            when {
                uiState.isLoading -> {
                    LoadingContent()
                }
                uiState.error != null -> {
                    ErrorContent(
                        message = uiState.error!!,
                        onRetry = { viewModel.onEvent(StatisticsEvent.Refresh) }
                    )
                }
                uiState.statistics != null -> {
                    StatisticsContent(
                        uiState = uiState,
                        onActivityClick = onNavigateToActivityDetail,
                        onTagClick = onNavigateToTagDetail
                    )
                }
                else -> {
                    EmptyStatisticsContent()
                }
            }
        }
    }
}

@Composable
private fun LoadingContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        LoadingIndicator()
    }
}

@Composable
private fun ErrorContent(
    message: String,
    onRetry: () -> Unit
) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        BlockwiseErrorState(
            title = "加载失败",
            description = message,
            onRetry = onRetry
        )
    }
}

@Composable
private fun EmptyStatisticsContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        BlockwiseEmptyState(
            title = "暂无统计数据",
            description = "开始记录时间后，这里将显示你的时间分析",
            icon = Icons.Outlined.Analytics
        )
    }
}

@Composable
private fun StatisticsContent(
    uiState: StatisticsUiState,
    onActivityClick: (Long) -> Unit,
    onTagClick: (Long) -> Unit
) {
    val statistics = uiState.statistics ?: return

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Summary card
        item {
            StatisticsSummaryCard(summary = statistics.summary)
        }

        // Activity type distribution (Pie chart)
        item {
            ChartCard(title = "活动类型分布") {
                BlockwisePieChart(
                    data = statistics.byActivity,
                    centerContent = {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = statistics.summary.formattedTotal,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "总时长",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                )
            }
        }

        // Daily trends (Bar chart)
        item {
            ChartCard(title = "每日趋势") {
                BlockwiseBarChart(
                    data = statistics.dailyTrends,
                    height = 180.dp
                )
            }
        }

        // Hourly distribution
        item {
            ChartCard(title = "时段分布") {
                HourlyDistributionChart(
                    data = statistics.hourlyPattern,
                    height = 160.dp
                )
            }
        }

        // Tag distribution (if available)
        if (statistics.byTag.isNotEmpty()) {
            item {
                ChartCard(title = "标签分布") {
                    BlockwisePieChart(
                        data = statistics.byTag,
                        maxLegendItems = 5
                    )
                }
            }
        }

        // Bottom spacing
        item {
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

/**
 * Card wrapper for chart sections.
 */
@Composable
private fun ChartCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}
