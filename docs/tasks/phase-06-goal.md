---
文档类型: 阶段任务文档
阶段编号: 06
阶段名称: 目标管理模块
版本: v1.0
创建日期: 2026-01-15
预计工期: 4-5天
前置条件: 阶段三、阶段五完成
---

# 阶段六：目标管理模块

## 1. 阶段目标

### 1.1 核心目标

实现 Blockwise 应用的目标管理功能，包括目标创建、进度追踪、完成提醒，帮助用户设定和达成时间使用目标。

### 1.2 交付成果

- 目标 CRUD Use Cases
- 目标进度计算逻辑
- 目标列表界面
- 目标创建/编辑界面
- 目标进度卡片组件
- 目标完成通知

### 1.3 功能优先级

| 功能 | 优先级 | 说明 |
|------|--------|------|
| 目标创建 | P0 | 核心功能 |
| 进度计算 | P0 | 核心功能 |
| 目标列表 | P0 | 展示界面 |
| 进度卡片 | P0 | 可视化展示 |
| 完成通知 | P1 | 用户激励 |
| 目标归档 | P2 | 历史管理 |

---

## 2. 任务列表

### 2.1 目标业务逻辑

#### T6.1.1 实现创建目标 Use Case

| 属性 | 值 |
|------|-----|
| **任务ID** | T6.1.1 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T3.4.9 |

**任务描述**：
实现创建目标的业务逻辑，包括验证和持久化。

**关键代码**：
```kotlin
// CreateGoalUseCase.kt
package com.blockwise.feature.goal.domain.usecase

import com.blockwise.core.domain.model.Goal
import com.blockwise.core.domain.model.GoalPeriod
import com.blockwise.core.domain.model.GoalType
import com.blockwise.core.domain.repository.GoalRepository
import kotlinx.datetime.LocalDate
import javax.inject.Inject

data class GoalInput(
    val tagId: Long,
    val targetMinutes: Int,
    val goalType: GoalType,
    val period: GoalPeriod,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)

class CreateGoalUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    suspend operator fun invoke(input: GoalInput): Result<Long> {
        // Validation
        if (input.targetMinutes <= 0) {
            return Result.failure(IllegalArgumentException("目标时长必须大于0"))
        }

        if (input.period == GoalPeriod.CUSTOM) {
            if (input.startDate == null || input.endDate == null) {
                return Result.failure(IllegalArgumentException("自定义周期需要设置起止日期"))
            }
            if (input.startDate >= input.endDate) {
                return Result.failure(IllegalArgumentException("结束日期必须晚于开始日期"))
            }
        }

        // Check if active goal exists for this tag
        val existingGoal = repository.getActiveByTagId(input.tagId)
        if (existingGoal != null) {
            return Result.failure(IllegalArgumentException("该标签已有活动目标"))
        }

        return try {
            val id = repository.create(input)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**验收标准**：
- [ ] 目标时长验证
- [ ] 自定义周期日期验证
- [ ] 同标签目标唯一性检查
- [ ] 创建成功返回 ID

---

#### T6.1.2 实现更新目标 Use Case

| 属性 | 值 |
|------|-----|
| **任务ID** | T6.1.2 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T3.4.9 |

**验收标准**：
- [ ] 更新现有目标
- [ ] 验证逻辑复用
- [ ] 不能修改已完成目标

---

#### T6.1.3 实现删除/归档目标 Use Case

| 属性 | 值 |
|------|-----|
| **任务ID** | T6.1.3 |
| **优先级** | P0 |
| **预计耗时** | 0.5h |
| **依赖任务** | T3.4.9 |

**验收标准**：
- [ ] 软删除（归档）
- [ ] 硬删除选项
- [ ] 归档后不计入活动目标

---

#### T6.1.4 实现查询目标 Use Case

| 属性 | 值 |
|------|-----|
| **任务ID** | T6.1.4 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T3.4.9 |

**关键代码**：
```kotlin
// GetGoalsUseCase.kt
class GetGoalsUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    fun getActiveGoals(): Flow<List<Goal>> {
        return repository.getActiveGoals()
    }

    fun getAllGoals(): Flow<List<Goal>> {
        return repository.getAllGoals()
    }

    suspend fun getById(id: Long): Goal? {
        return repository.getById(id)
    }

    fun getByTagId(tagId: Long): Flow<List<Goal>> {
        return repository.getByTagId(tagId)
    }
}
```

**验收标准**：
- [ ] 查询活动目标
- [ ] 查询所有目标
- [ ] 按标签查询
- [ ] 按 ID 查询

---

### 2.2 目标进度计算

#### T6.2.1 实现目标进度计算 Use Case

| 属性 | 值 |
|------|-----|
| **任务ID** | T6.2.1 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T6.1.4, T3.4.10 |

**任务描述**：
实现目标进度计算逻辑，根据目标周期和类型计算当前进度。

**关键代码**：
```kotlin
// CalculateGoalProgressUseCase.kt
class CalculateGoalProgressUseCase @Inject constructor(
    private val statisticsRepository: StatisticsRepository
) {
    suspend operator fun invoke(goal: Goal): GoalProgress {
        val (startTime, endTime) = goal.currentPeriodRange()

        val currentMinutes = statisticsRepository.getTotalMinutesForTag(
            tagId = goal.tag.id,
            startTime = startTime,
            endTime = endTime
        )

        return GoalProgress(
            goal = goal,
            currentMinutes = currentMinutes,
            targetMinutes = goal.targetMinutes
        )
    }

    suspend fun calculateAll(goals: List<Goal>): List<GoalProgress> {
        return goals.map { invoke(it) }
    }
}

// Goal extension
fun Goal.currentPeriodRange(): Pair<Instant, Instant> {
    val tz = TimeZone.currentSystemDefault()
    val now = Clock.System.now().toLocalDateTime(tz).date

    return when (period) {
        GoalPeriod.DAILY -> {
            val start = now.atStartOfDayIn(tz)
            val end = now.plus(1, DateTimeUnit.DAY).atStartOfDayIn(tz)
            start to end
        }
        GoalPeriod.WEEKLY -> {
            val weekStart = now.startOfWeek()
            val start = weekStart.atStartOfDayIn(tz)
            val end = weekStart.plus(7, DateTimeUnit.DAY).atStartOfDayIn(tz)
            start to end
        }
        GoalPeriod.MONTHLY -> {
            val monthStart = LocalDate(now.year, now.monthNumber, 1)
            val start = monthStart.atStartOfDayIn(tz)
            val end = monthStart.plus(1, DateTimeUnit.MONTH).atStartOfDayIn(tz)
            start to end
        }
        GoalPeriod.CUSTOM -> {
            val start = startDate!!.atStartOfDayIn(tz)
            val end = endDate!!.plus(1, DateTimeUnit.DAY).atStartOfDayIn(tz)
            start to end
        }
    }
}
```

**验收标准**：
- [ ] 日目标进度计算正确
- [ ] 周目标进度计算正确
- [ ] 月目标进度计算正确
- [ ] 自定义周期进度计算正确

---

#### T6.2.2 实现目标完成状态判断

| 属性 | 值 |
|------|-----|
| **任务ID** | T6.2.2 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T6.2.1 |

**任务描述**：
根据目标类型（最少/最多/精确）判断完成状态。

**验收标准**：
- [ ] MIN 类型：当前 >= 目标 为完成
- [ ] MAX 类型：当前 <= 目标 为完成
- [ ] EXACT 类型：当前 == 目标 为完成
- [ ] 超额/不足状态区分

---

#### T6.2.3 实现目标进度历史记录

| 属性 | 值 |
|------|-----|
| **任务ID** | T6.2.3 |
| **优先级** | P2 |
| **预计耗时** | 2h |
| **依赖任务** | T6.2.1 |

**验收标准**：
- [ ] 记录每个周期的完成情况
- [ ] 支持查询历史进度
- [ ] 连续完成天数统计

---

### 2.3 目标UI界面

#### T6.3.1 实现目标列表 ViewModel

| 属性 | 值 |
|------|-----|
| **任务ID** | T6.3.1 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T6.1.4, T6.2.1 |

**关键代码**：
```kotlin
// GoalListViewModel.kt
@HiltViewModel
class GoalListViewModel @Inject constructor(
    private val getGoals: GetGoalsUseCase,
    private val calculateProgress: CalculateGoalProgressUseCase,
    private val deleteGoal: DeleteGoalUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(GoalListUiState())
    val uiState: StateFlow<GoalListUiState> = _uiState.asStateFlow()

    init {
        loadGoals()
    }

    private fun loadGoals() {
        viewModelScope.launch {
            getGoals.getActiveGoals()
                .collect { goals ->
                    val progressList = calculateProgress.calculateAll(goals)
                    _uiState.update { it.copy(
                        goalProgressList = progressList,
                        isLoading = false
                    )}
                }
        }
    }

    fun deleteGoal(goalId: Long) {
        viewModelScope.launch {
            deleteGoal(goalId)
        }
    }
}

data class GoalListUiState(
    val goalProgressList: List<GoalProgress> = emptyList(),
    val isLoading: Boolean = true
)
```

**验收标准**：
- [ ] 目标列表加载
- [ ] 进度计算整合
- [ ] 删除操作处理

---

#### T6.3.2 实现目标列表界面

| 属性 | 值 |
|------|-----|
| **任务ID** | T6.3.2 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T6.3.1 |

**验收标准**：
- [ ] 目标卡片列表显示
- [ ] 空状态显示
- [ ] 添加目标入口
- [ ] 点击进入详情

---

#### T6.3.3 实现目标进度卡片组件

| 属性 | 值 |
|------|-----|
| **任务ID** | T6.3.3 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T2.2.7 |

**关键代码**：
```kotlin
// GoalProgressCard.kt
@Composable
fun GoalProgressCard(
    progress: GoalProgress,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BlockwiseClickableCard(
        onClick = onClick,
        modifier = modifier
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Progress circle
            BlockwiseCircularProgress(
                progress = progress.progress,
                size = 64.dp,
                color = progress.goal.tag.color
            ) {
                Text(
                    text = "${progress.progressPercentage}%",
                    style = MaterialTheme.typography.labelMedium
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Goal info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = progress.goal.tag.name,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "${progress.goal.goalTypeLabel}${progress.goal.formattedTarget}/${progress.goal.periodLabel}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "已完成 ${formatMinutes(progress.currentMinutes)}",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            // Status indicator
            GoalStatusBadge(progress = progress)
        }
    }
}

@Composable
private fun GoalStatusBadge(progress: GoalProgress) {
    val (text, color) = when {
        progress.isCompleted -> "已完成" to MaterialTheme.colorScheme.primary
        progress.progress >= 0.8f -> "即将完成" to Color(0xFFFFC107)
        else -> "进行中" to MaterialTheme.colorScheme.outline
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp)
    ) {
        Text(
            text = text,
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color
        )
    }
}
```

**验收标准**：
- [ ] 圆形进度显示
- [ ] 目标信息显示
- [ ] 状态标签显示
- [ ] 点击反馈效果

---

#### T6.3.4 实现目标编辑 ViewModel

| 属性 | 值 |
|------|-----|
| **任务ID** | T6.3.4 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T6.1.1, T6.1.2 |

**验收标准**：
- [ ] 创建/编辑模式区分
- [ ] 表单状态管理
- [ ] 保存逻辑正确

---

#### T6.3.5 实现目标编辑界面

| 属性 | 值 |
|------|-----|
| **任务ID** | T6.3.5 |
| **优先级** | P0 |
| **预计耗时** | 2.5h |
| **依赖任务** | T6.3.4 |

**验收标准**：
- [ ] 标签选择器
- [ ] 目标时长输入
- [ ] 目标类型选择
- [ ] 周期选择
- [ ] 自定义日期选择

---

#### T6.3.6 实现目标详情页面

| 属性 | 值 |
|------|-----|
| **任务ID** | T6.3.6 |
| **优先级** | P1 |
| **预计耗时** | 2h |
| **依赖任务** | T6.3.2 |

**验收标准**：
- [ ] 进度详情显示
- [ ] 历史趋势图表
- [ ] 编辑/删除入口

---

### 2.4 目标通知

#### T6.4.1 实现目标完成通知

| 属性 | 值 |
|------|-----|
| **任务ID** | T6.4.1 |
| **优先级** | P1 |
| **预计耗时** | 1.5h |
| **依赖任务** | T6.2.2 |

**验收标准**：
- [ ] 目标完成时发送通知
- [ ] 通知内容包含目标信息
- [ ] 点击通知跳转目标详情

---

#### T6.4.2 实现目标提醒通知

| 属性 | 值 |
|------|-----|
| **任务ID** | T6.4.2 |
| **优先级** | P2 |
| **预计耗时** | 2h |
| **依赖任务** | T6.2.1 |

**验收标准**：
- [ ] 周期结束前提醒
- [ ] 可配置提醒时间
- [ ] 支持开关控制

---

## 3. 依赖关系图

```
阶段三完成 (Repository)
        │
        ├── T3.4.9 GoalRepository ──┬── T6.1.1 创建目标
        │                           ├── T6.1.2 更新目标
        │                           ├── T6.1.3 删除目标
        │                           └── T6.1.4 查询目标
        │                                      │
        └── T3.4.10 StatisticsRepository       │
                    │                          │
                    └── T6.2.1 进度计算 ───────┤
                              │                │
                              └── T6.2.2 完成判断
                                      │
                                      └── T6.4.1 完成通知

        T6.1.4 + T6.2.1 ─── T6.3.1 ViewModel ─── T6.3.2 列表界面
                                                        │
                                    T6.3.3 进度卡片 ────┤
                                                        │
                                                T6.3.6 详情页面

        T6.1.1 + T6.1.2 ─── T6.3.4 编辑ViewModel ─── T6.3.5 编辑界面
```

---

## 4. 验收标准清单

### 4.1 目标业务逻辑验收

| 验收项 | 要求 |
|--------|------|
| 创建目标 | 验证通过、同标签唯一、返回ID |
| 更新目标 | 验证通过、不能修改已完成目标 |
| 删除目标 | 软删除/硬删除、归档后不计入活动 |
| 查询目标 | 活动/全部/按标签/按ID查询正确 |

### 4.2 进度计算验收

| 验收项 | 要求 |
|--------|------|
| 日目标 | 当日0:00-24:00统计正确 |
| 周目标 | 本周一至周日统计正确 |
| 月目标 | 本月1日至月末统计正确 |
| 完成判断 | MIN/MAX/EXACT三种类型判断正确 |

### 4.3 目标UI验收

| 验收项 | 要求 |
|--------|------|
| 目标列表 | 卡片显示、空状态、添加入口 |
| 进度卡片 | 圆形进度、信息显示、状态标签 |
| 编辑界面 | 表单完整、验证提示、保存成功 |
| 详情页面 | 进度详情、趋势图表、操作入口 |

---

## 5. 风险与注意事项

### 5.1 潜在风险

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 进度计算延迟 | 显示不准确 | 缓存+定时刷新 |
| 时区问题 | 周期边界错误 | 统一使用系统时区 |
| 通知权限 | 无法发送通知 | 引导用户授权 |

### 5.2 注意事项

1. **进度实时性**：记录变化时触发进度重算
2. **周期边界**：注意周一/周日作为周起始的配置
3. **目标唯一性**：同一标签只能有一个活动目标
4. **历史保留**：归档目标保留历史数据

---

*文档版本: v1.0*
*阶段状态: 已完成*
