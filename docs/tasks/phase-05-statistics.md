---
文档类型: 阶段任务文档
阶段编号: 05
阶段名称: 统计分析模块
版本: v1.0
创建日期: 2026-01-15
预计工期: 5-6天
前置条件: 阶段三、阶段四完成
---

# 阶段五：统计分析模块

## 1. 阶段目标

### 1.1 核心目标

实现 Blockwise 应用的统计分析功能，包括数据聚合计算、图表可视化展示、多维度统计视图，为用户提供直观的时间使用分析。

### 1.2 交付成果

- 统计数据聚合 Use Cases
- 饼图/柱状图/折线图组件
- 日/周/月/年统计视图
- 活动类型分布统计
- 标签维度统计
- 时段分布分析
- 趋势对比分析

### 1.3 功能优先级

| 功能 | 优先级 | 说明 |
|------|--------|------|
| 数据聚合 | P0 | 统计计算基础 |
| 饼图展示 | P0 | 分布可视化 |
| 柱状图展示 | P0 | 趋势可视化 |
| 日/周/月视图 | P0 | 多周期统计 |
| 时段分布 | P1 | 使用模式分析 |
| 趋势对比 | P1 | 同比环比分析 |

---

## 2. 任务列表

### 2.1 统计数据聚合

#### T5.1.1 实现按活动类型统计 Use Case

| 属性 | 值 |
|------|-----|
| **任务ID** | T5.1.1 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T3.4.10 |

**任务描述**：
实现按活动类型聚合统计的业务逻辑。

**关键代码**：
```kotlin
// GetStatsByActivityTypeUseCase.kt
package com.blockwise.feature.statistics.domain.usecase

import com.blockwise.core.domain.model.CategoryStatistics
import com.blockwise.core.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import javax.inject.Inject

class GetStatsByActivityTypeUseCase @Inject constructor(
    private val repository: StatisticsRepository
) {
    operator fun invoke(
        startTime: Instant,
        endTime: Instant
    ): Flow<List<CategoryStatistics>> {
        return repository.getStatsByActivityType(startTime, endTime)
            .map { stats ->
                val total = stats.sumOf { it.totalMinutes }
                stats.map { stat ->
                    stat.copy(
                        percentage = if (total > 0) {
                            (stat.totalMinutes * 100f / total)
                        } else 0f
                    )
                }.sortedByDescending { it.totalMinutes }
            }
    }
}
```

**验收标准**：
- [ ] 按时间范围聚合正确
- [ ] 百分比计算正确
- [ ] 按时长降序排列
- [ ] 空数据处理正确

---

#### T5.1.2 实现按标签统计 Use Case

| 属性 | 值 |
|------|-----|
| **任务ID** | T5.1.2 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T3.4.10 |

**任务描述**：
实现按标签聚合统计的业务逻辑。

**验收标准**：
- [ ] 按时间范围聚合正确
- [ ] 多标签记录正确计算
- [ ] 百分比计算正确

---

#### T5.1.3 实现每日趋势统计 Use Case

| 属性 | 值 |
|------|-----|
| **任务ID** | T5.1.3 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T3.4.10 |

**任务描述**：
实现按日聚合的趋势统计。

**关键代码**：
```kotlin
// GetDailyTrendsUseCase.kt
class GetDailyTrendsUseCase @Inject constructor(
    private val repository: StatisticsRepository
) {
    operator fun invoke(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<DailyTrend>> {
        val tz = TimeZone.currentSystemDefault()
        val startTime = startDate.atStartOfDayIn(tz)
        val endTime = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(tz)

        return repository.getDailyTrends(startTime, endTime)
            .map { trends ->
                // Fill missing days with zero
                val trendMap = trends.associateBy { it.date }
                val result = mutableListOf<DailyTrend>()
                var current = startDate
                while (current <= endDate) {
                    result.add(
                        trendMap[current] ?: DailyTrend(
                            date = current,
                            totalMinutes = 0,
                            entryCount = 0
                        )
                    )
                    current = current.plus(1, DateTimeUnit.DAY)
                }
                result
            }
    }
}
```

**验收标准**：
- [ ] 按日聚合正确
- [ ] 缺失日期填充零值
- [ ] 日期范围正确

---

#### T5.1.4 实现时段分布统计 Use Case

| 属性 | 值 |
|------|-----|
| **任务ID** | T5.1.4 |
| **优先级** | P1 |
| **预计耗时** | 1.5h |
| **依赖任务** | T3.4.10 |

**任务描述**：
实现24小时时段分布统计。

**验收标准**：
- [ ] 24小时分布正确
- [ ] 跨时段记录正确分配
- [ ] 返回完整24小时数据

---

#### T5.1.5 实现统计摘要 Use Case

| 属性 | 值 |
|------|-----|
| **任务ID** | T5.1.5 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T3.4.10 |

**任务描述**：
实现统计摘要计算，包括总时长、记录数、同比变化。

**关键代码**：
```kotlin
// GetStatisticsSummaryUseCase.kt
class GetStatisticsSummaryUseCase @Inject constructor(
    private val repository: StatisticsRepository
) {
    suspend operator fun invoke(
        startTime: Instant,
        endTime: Instant,
        comparePreviousPeriod: Boolean = true
    ): StatisticsSummary {
        val current = repository.getTotalStats(startTime, endTime)

        val previous = if (comparePreviousPeriod) {
            val duration = endTime - startTime
            val previousStart = startTime - duration
            val previousEnd = startTime
            repository.getTotalStats(previousStart, previousEnd)
        } else null

        return StatisticsSummary(
            totalMinutes = current.totalMinutes,
            entryCount = current.entryCount,
            previousPeriodMinutes = previous?.totalMinutes
        )
    }
}
```

**验收标准**：
- [ ] 总时长计算正确
- [ ] 记录数统计正确
- [ ] 同比计算正确

---

#### T5.1.6 实现周期统计 Use Case

| 属性 | 值 |
|------|-----|
| **任务ID** | T5.1.6 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T5.1.1 - T5.1.5 |

**任务描述**：
整合各维度统计，提供完整的周期统计数据。

**关键代码**：
```kotlin
// GetPeriodStatisticsUseCase.kt
class GetPeriodStatisticsUseCase @Inject constructor(
    private val getStatsByActivityType: GetStatsByActivityTypeUseCase,
    private val getStatsByTag: GetStatsByTagUseCase,
    private val getDailyTrends: GetDailyTrendsUseCase,
    private val getHourlyDistribution: GetHourlyDistributionUseCase,
    private val getSummary: GetStatisticsSummaryUseCase
) {
    suspend operator fun invoke(
        period: StatisticsPeriod
    ): PeriodStatistics {
        val (startTime, endTime) = period.toTimeRange()
        val (startDate, endDate) = period.toDateRange()

        return PeriodStatistics(
            summary = getSummary(startTime, endTime),
            byActivity = getStatsByActivityType(startTime, endTime).first(),
            byTag = getStatsByTag(startTime, endTime).first(),
            dailyTrends = getDailyTrends(startDate, endDate).first(),
            hourlyPattern = getHourlyDistribution(startTime, endTime).first()
        )
    }
}

// StatisticsPeriod.kt
sealed class StatisticsPeriod {
    data class Day(val date: LocalDate) : StatisticsPeriod()
    data class Week(val weekStart: LocalDate) : StatisticsPeriod()
    data class Month(val year: Int, val month: Int) : StatisticsPeriod()
    data class Year(val year: Int) : StatisticsPeriod()
    data class Custom(val startDate: LocalDate, val endDate: LocalDate) : StatisticsPeriod()

    fun toTimeRange(): Pair<Instant, Instant> {
        val tz = TimeZone.currentSystemDefault()
        return when (this) {
            is Day -> {
                val start = date.atStartOfDayIn(tz)
                val end = date.plus(1, DateTimeUnit.DAY).atStartOfDayIn(tz)
                start to end
            }
            is Week -> {
                val start = weekStart.atStartOfDayIn(tz)
                val end = weekStart.plus(7, DateTimeUnit.DAY).atStartOfDayIn(tz)
                start to end
            }
            is Month -> {
                val start = LocalDate(year, month, 1).atStartOfDayIn(tz)
                val end = start.plus(1, DateTimeUnit.MONTH)
                start to end
            }
            is Year -> {
                val start = LocalDate(year, 1, 1).atStartOfDayIn(tz)
                val end = LocalDate(year + 1, 1, 1).atStartOfDayIn(tz)
                start to end
            }
            is Custom -> {
                val start = startDate.atStartOfDayIn(tz)
                val end = endDate.plus(1, DateTimeUnit.DAY).atStartOfDayIn(tz)
                start to end
            }
        }
    }
}
```

**验收标准**：
- [ ] 日/周/月/年周期支持
- [ ] 自定义周期支持
- [ ] 各维度数据整合正确

---

### 2.2 图表组件

#### T5.2.1 集成 Vico 图表库

| 属性 | 值 |
|------|-----|
| **任务ID** | T5.2.1 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T1.3.1 |

**任务描述**：
集成 Vico 图表库，配置基础依赖。

**执行步骤**：
1. 添加 Vico 依赖到 feature:statistics 模块
2. 配置图表主题适配 Material3
3. 创建图表工具类

**关键代码**：
```kotlin
// build.gradle.kts (feature:statistics)
dependencies {
    implementation(libs.vico.compose)
    implementation(libs.vico.compose.m3)
}

// ChartTheme.kt
package com.blockwise.feature.statistics.ui.chart

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.patrykandpatrick.vico.compose.m3.style.m3ChartStyle
import com.patrykandpatrick.vico.compose.style.ChartStyle

@Composable
fun blockwiseChartStyle(): ChartStyle {
    return m3ChartStyle(
        axisLabelColor = MaterialTheme.colorScheme.onSurfaceVariant,
        axisGuidelineColor = MaterialTheme.colorScheme.outlineVariant,
        axisLineColor = MaterialTheme.colorScheme.outline
    )
}
```

**验收标准**：
- [ ] Vico 依赖正确添加
- [ ] 图表主题适配 Material3
- [ ] 编译无错误

---

#### T5.2.2 实现饼图组件

| 属性 | 值 |
|------|-----|
| **任务ID** | T5.2.2 |
| **优先级** | P0 |
| **预计耗时** | 2.5h |
| **依赖任务** | T5.2.1 |

**任务描述**：
实现活动类型分布饼图组件。

**关键代码**：
```kotlin
// PieChart.kt
package com.blockwise.feature.statistics.ui.chart

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.blockwise.core.domain.model.CategoryStatistics

@Composable
fun BlockwisePieChart(
    data: List<CategoryStatistics>,
    modifier: Modifier = Modifier,
    showLegend: Boolean = true,
    centerContent: @Composable (() -> Unit)? = null
) {
    if (data.isEmpty()) {
        EmptyChartPlaceholder(modifier)
        return
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Pie Chart
        Box(
            modifier = Modifier
                .weight(1f)
                .aspectRatio(1f),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                val total = data.sumOf { it.totalMinutes }
                var startAngle = -90f

                data.forEach { item ->
                    val sweepAngle = (item.totalMinutes.toFloat() / total) * 360f
                    drawArc(
                        color = item.color,
                        startAngle = startAngle,
                        sweepAngle = sweepAngle,
                        useCenter = false,
                        style = Stroke(width = 40.dp.toPx()),
                        size = Size(size.width - 40.dp.toPx(), size.height - 40.dp.toPx()),
                        topLeft = Offset(20.dp.toPx(), 20.dp.toPx())
                    )
                    startAngle += sweepAngle
                }
            }

            centerContent?.invoke()
        }

        // Legend
        if (showLegend) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 16.dp)
            ) {
                data.take(5).forEach { item ->
                    PieLegendItem(
                        color = item.color,
                        label = item.name,
                        value = item.formattedDuration,
                        percentage = item.percentage
                    )
                }
                if (data.size > 5) {
                    Text(
                        text = "还有 ${data.size - 5} 项...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun PieLegendItem(
    color: Color,
    label: String,
    value: String,
    percentage: Float
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "$value (${percentage.toInt()}%)",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
```

**验收标准**：
- [ ] 饼图正确显示分布
- [ ] 颜色与活动类型对应
- [ ] 图例显示正确
- [ ] 空数据显示占位符

---

#### T5.2.3 实现柱状图组件

| 属性 | 值 |
|------|-----|
| **任务ID** | T5.2.3 |
| **优先级** | P0 |
| **预计耗时** | 2.5h |
| **依赖任务** | T5.2.1 |

**任务描述**：
实现每日时长柱状图组件。

**关键代码**：
```kotlin
// BarChart.kt
package com.blockwise.feature.statistics.ui.chart

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.blockwise.core.domain.model.DailyTrend
import com.patrykandpatrick.vico.compose.axis.horizontal.rememberBottomAxis
import com.patrykandpatrick.vico.compose.axis.vertical.rememberStartAxis
import com.patrykandpatrick.vico.compose.chart.Chart
import com.patrykandpatrick.vico.compose.chart.column.columnChart
import com.patrykandpatrick.vico.compose.style.ProvideChartStyle
import com.patrykandpatrick.vico.core.axis.AxisPosition
import com.patrykandpatrick.vico.core.axis.formatter.AxisValueFormatter
import com.patrykandpatrick.vico.core.entry.entryModelOf
import kotlinx.datetime.LocalDate

@Composable
fun BlockwiseBarChart(
    data: List<DailyTrend>,
    modifier: Modifier = Modifier,
    showHours: Boolean = true
) {
    if (data.isEmpty()) {
        EmptyChartPlaceholder(modifier)
        return
    }

    val chartEntryModel = remember(data) {
        entryModelOf(*data.mapIndexed { index, trend ->
            index.toFloat() to (trend.totalMinutes / 60f)
        }.toTypedArray())
    }

    val dateFormatter = remember(data) {
        AxisValueFormatter<AxisPosition.Horizontal.Bottom> { value, _ ->
            data.getOrNull(value.toInt())?.date?.let { date ->
                "${date.monthNumber}/${date.dayOfMonth}"
            } ?: ""
        }
    }

    val hoursFormatter = remember {
        AxisValueFormatter<AxisPosition.Vertical.Start> { value, _ ->
            "${value.toInt()}h"
        }
    }

    ProvideChartStyle(blockwiseChartStyle()) {
        Chart(
            chart = columnChart(),
            model = chartEntryModel,
            modifier = modifier,
            startAxis = rememberStartAxis(
                valueFormatter = hoursFormatter,
                itemPlacer = remember { AxisItemPlacer.Vertical.default(maxItemCount = 5) }
            ),
            bottomAxis = rememberBottomAxis(
                valueFormatter = dateFormatter,
                guideline = null
            )
        )
    }
}
```

**验收标准**：
- [ ] 柱状图正确显示每日数据
- [ ] X轴日期标签正确
- [ ] Y轴小时数标签正确
- [ ] 空数据显示占位符

---

#### T5.2.4 实现折线图组件

| 属性 | 值 |
|------|-----|
| **任务ID** | T5.2.4 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T5.2.1 |

**任务描述**：
实现趋势折线图组件。

**验收标准**：
- [ ] 折线图正确显示趋势
- [ ] 支持多条折线（对比）
- [ ] 数据点可交互
- [ ] 平滑曲线效果

---

#### T5.2.5 实现时段分布图组件

| 属性 | 值 |
|------|-----|
| **任务ID** | T5.2.5 |
| **优先级** | P1 |
| **预计耗时** | 2h |
| **依赖任务** | T5.2.1 |

**任务描述**：
实现24小时时段分布热力图/柱状图。

**关键代码**：
```kotlin
// HourlyDistributionChart.kt
@Composable
fun HourlyDistributionChart(
    data: List<HourlyPattern>,
    modifier: Modifier = Modifier
) {
    if (data.isEmpty()) {
        EmptyChartPlaceholder(modifier)
        return
    }

    val maxMinutes = data.maxOfOrNull { it.totalMinutes } ?: 1

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        data.forEach { hourData ->
            val heightFraction = hourData.totalMinutes.toFloat() / maxMinutes

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(0.6f)
                        .fillMaxHeight(heightFraction.coerceAtLeast(0.02f))
                        .background(
                            color = getHourColor(hourData.hour),
                            shape = RoundedCornerShape(topStart = 2.dp, topEnd = 2.dp)
                        )
                )

                if (hourData.hour % 6 == 0) {
                    Text(
                        text = "${hourData.hour}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

private fun getHourColor(hour: Int): Color {
    return when (hour) {
        in 6..11 -> Color(0xFF4CAF50)   // Morning - Green
        in 12..17 -> Color(0xFFFFC107)  // Afternoon - Yellow
        in 18..21 -> Color(0xFFFF9800)  // Evening - Orange
        else -> Color(0xFF9E9E9E)        // Night - Grey
    }
}
```

**验收标准**：
- [ ] 24小时分布正确显示
- [ ] 时段颜色区分
- [ ] 高度比例正确

---

### 2.3 统计UI界面

#### T5.3.1 实现统计主页面 ViewModel

| 属性 | 值 |
|------|-----|
| **任务ID** | T5.3.1 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T5.1.6 |

**关键代码**：
```kotlin
// StatisticsViewModel.kt
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val getPeriodStatistics: GetPeriodStatisticsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    private val _selectedPeriod = MutableStateFlow<StatisticsPeriod>(
        StatisticsPeriod.Week(LocalDate.now().startOfWeek())
    )

    init {
        loadStatistics()
    }

    private fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val stats = getPeriodStatistics(_selectedPeriod.value)
                _uiState.update { it.copy(
                    statistics = stats,
                    isLoading = false
                )}
            } catch (e: Exception) {
                _uiState.update { it.copy(
                    error = e.message,
                    isLoading = false
                )}
            }
        }
    }

    fun selectPeriod(period: StatisticsPeriod) {
        _selectedPeriod.value = period
        loadStatistics()
    }

    fun navigateToPreviousPeriod() {
        _selectedPeriod.value = _selectedPeriod.value.previous()
        loadStatistics()
    }

    fun navigateToNextPeriod() {
        _selectedPeriod.value = _selectedPeriod.value.next()
        loadStatistics()
    }
}

data class StatisticsUiState(
    val statistics: PeriodStatistics? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)
```

**验收标准**：
- [ ] 周期切换正常
- [ ] 数据加载正确
- [ ] 加载状态管理

---

#### T5.3.2 实现统计主页面界面

| 属性 | 值 |
|------|-----|
| **任务ID** | T5.3.2 |
| **优先级** | P0 |
| **预计耗时** | 3h |
| **依赖任务** | T5.3.1, T5.2.2, T5.2.3 |

**验收标准**：
- [ ] 周期选择器显示
- [ ] 摘要卡片显示
- [ ] 饼图显示正确
- [ ] 柱状图显示正确

---

#### T5.3.3 实现周期选择器组件

| 属性 | 值 |
|------|-----|
| **任务ID** | T5.3.3 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T2.1.3 |

**关键代码**：
```kotlin
// PeriodSelector.kt
@Composable
fun PeriodSelector(
    currentPeriod: StatisticsPeriod,
    onPeriodTypeChange: (PeriodType) -> Unit,
    onNavigatePrevious: () -> Unit,
    onNavigateNext: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        // Period type tabs
        TabRow(
            selectedTabIndex = currentPeriod.type.ordinal
        ) {
            PeriodType.entries.forEach { type ->
                Tab(
                    selected = currentPeriod.type == type,
                    onClick = { onPeriodTypeChange(type) },
                    text = { Text(type.label) }
                )
            }
        }

        // Period navigation
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onNavigatePrevious) {
                Icon(Icons.Default.ChevronLeft, "上一周期")
            }

            Text(
                text = currentPeriod.displayLabel,
                style = MaterialTheme.typography.titleMedium
            )

            IconButton(
                onClick = onNavigateNext,
                enabled = !currentPeriod.isCurrentPeriod
            ) {
                Icon(Icons.Default.ChevronRight, "下一周期")
            }
        }
    }
}

enum class PeriodType(val label: String) {
    DAY("日"),
    WEEK("周"),
    MONTH("月"),
    YEAR("年")
}
```

**验收标准**：
- [ ] Tab 切换正常
- [ ] 前后导航正常
- [ ] 当前周期禁用"下一个"

---

#### T5.3.4 实现统计摘要卡片

| 属性 | 值 |
|------|-----|
| **任务ID** | T5.3.4 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T2.2.2 |

**验收标准**：
- [ ] 总时长显示
- [ ] 记录数显示
- [ ] 同比变化显示（箭头+百分比）

---

#### T5.3.5 实现活动类型详情页面

| 属性 | 值 |
|------|-----|
| **任务ID** | T5.3.5 |
| **优先级** | P1 |
| **预计耗时** | 2h |
| **依赖任务** | T5.3.2 |

**验收标准**：
- [ ] 单活动类型统计
- [ ] 趋势图表
- [ ] 相关记录列表

---

#### T5.3.6 实现标签详情页面

| 属性 | 值 |
|------|-----|
| **任务ID** | T5.3.6 |
| **优先级** | P1 |
| **预计耗时** | 2h |
| **依赖任务** | T5.3.2 |

**验收标准**：
- [ ] 单标签统计
- [ ] 趋势图表
- [ ] 目标进度（如有关联目标）

---

## 3. 依赖关系图

```
阶段三完成 (Repository)
        │
        └── T3.4.10 StatisticsRepository
                │
                ├── T5.1.1 按活动类型统计 ──┐
                ├── T5.1.2 按标签统计 ──────┤
                ├── T5.1.3 每日趋势统计 ────┼── T5.1.6 周期统计整合
                ├── T5.1.4 时段分布统计 ────┤
                └── T5.1.5 统计摘要 ────────┘
                                            │
        T1.3.1 Compose ─── T5.2.1 Vico集成  │
                                │           │
                                ├── T5.2.2 饼图 ────────┐
                                ├── T5.2.3 柱状图 ──────┤
                                ├── T5.2.4 折线图 ──────┼── T5.3.2 统计主页面
                                └── T5.2.5 时段分布图 ──┤
                                                        │
                        T5.3.1 ViewModel ───────────────┤
                        T5.3.3 周期选择器 ──────────────┤
                        T5.3.4 摘要卡片 ────────────────┘
                                                        │
                                        ┌───────────────┴───────────────┐
                                        │                               │
                                T5.3.5 活动类型详情          T5.3.6 标签详情
```

---

## 4. 验收标准清单

### 4.1 数据聚合验收

| 验收项 | 要求 |
|--------|------|
| 活动类型统计 | 按时间范围正确聚合，百分比计算正确 |
| 标签统计 | 多标签记录正确计算，不重复统计 |
| 每日趋势 | 缺失日期填充零值，日期范围正确 |
| 时段分布 | 24小时完整数据，跨时段正确分配 |
| 统计摘要 | 总时长、记录数、同比变化正确 |

### 4.2 图表组件验收

| 组件 | 验收标准 |
|------|----------|
| 饼图 | 分布正确、颜色对应、图例显示、空数据处理 |
| 柱状图 | 数据正确、坐标轴标签、空数据处理 |
| 折线图 | 趋势正确、多线支持、平滑曲线 |
| 时段分布图 | 24小时显示、时段颜色区分 |

### 4.3 统计UI验收

| 验收项 | 要求 |
|--------|------|
| 周期选择 | 日/周/月/年切换正常 |
| 周期导航 | 前后切换正常，当前周期禁用"下一个" |
| 摘要卡片 | 总时长、记录数、变化趋势显示 |
| 图表加载 | 加载状态显示，<3秒加载完成 |
| 详情页面 | 单维度统计、趋势图表、相关数据 |

---

## 5. 风险与注意事项

### 5.1 潜在风险

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 大数据量图表卡顿 | 用户体验差 | 数据采样、虚拟化渲染 |
| 时区计算错误 | 统计数据不准 | 统一使用系统时区 |
| 图表库兼容性 | 编译/运行错误 | 锁定 Vico 版本 |

### 5.2 注意事项

1. **数据采样**：超过30天的趋势数据考虑按周聚合
2. **缓存策略**：统计结果可缓存，减少重复计算
3. **空数据处理**：所有图表需要空数据占位符
4. **动画性能**：图表动画不超过300ms

---

*文档版本: v1.0*
*阶段状态: 待开始*
