---
文档类型: 阶段任务文档
阶段编号: 04
阶段名称: 时间记录功能模块
版本: v1.0
创建日期: 2026-01-15
预计工期: 7-9天
前置条件: 阶段二、阶段三完成
---

# 阶段四：时间记录功能模块

## 1. 阶段目标

### 1.1 核心目标

实现 Blockwise 应用的核心功能——时间记录模块，包括活动类型管理、标签管理、手动记录、计时器功能、时间线视图和时间块视图。

### 1.2 交付成果

- 活动类型管理（创建/编辑/删除/层级）
- 标签管理（创建/编辑/删除）
- 手动时间记录（创建/编辑/删除）
- 计时器功能（开始/暂停/停止/后台运行）
- 时间线视图（按时间顺序展示记录）
- 时间块视图（日视图/周视图可视化）

### 1.3 功能优先级

| 功能 | 优先级 | 说明 |
|------|--------|------|
| 活动类型管理 | P0 | 记录的基础分类 |
| 标签管理 | P0 | 记录的标签系统 |
| 手动记录 | P0 | 核心记录功能 |
| 计时器 | P0 | 实时记录功能 |
| 时间线视图 | P0 | 记录列表展示 |
| 时间块视图 | P0 | 可视化展示 |

---

## 2. 任务列表

### 2.1 活动类型管理

#### T4.1.1 实现活动类型Use Cases

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.1.1 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T3.4.6 |

**任务描述**：
实现活动类型的业务逻辑用例，包括创建、编辑、删除、查询。

**执行步骤**：
1. 创建 GetActivityTypesUseCase
2. 创建 GetActivityTypeByIdUseCase
3. 创建 CreateActivityTypeUseCase
4. 创建 UpdateActivityTypeUseCase
5. 创建 DeleteActivityTypeUseCase

**关键代码**：
```kotlin
// GetActivityTypesUseCase.kt
package com.blockwise.feature.timeentry.domain.usecase

import com.blockwise.core.domain.model.ActivityType
import com.blockwise.core.domain.repository.ActivityTypeRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetActivityTypesUseCase @Inject constructor(
    private val repository: ActivityTypeRepository
) {
    operator fun invoke(includeArchived: Boolean = false): Flow<List<ActivityType>> {
        return if (includeArchived) {
            repository.getAll()
        } else {
            repository.getAllActive()
        }
    }
}

// CreateActivityTypeUseCase.kt
class CreateActivityTypeUseCase @Inject constructor(
    private val repository: ActivityTypeRepository
) {
    suspend operator fun invoke(
        name: String,
        colorHex: String,
        icon: String? = null,
        parentId: Long? = null
    ): Result<Long> {
        // Validation
        if (name.isBlank()) {
            return Result.failure(IllegalArgumentException("名称不能为空"))
        }
        if (repository.isNameExists(name)) {
            return Result.failure(IllegalArgumentException("该名称已存在"))
        }

        return try {
            val id = repository.create(name, colorHex, icon, parentId)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**验收标准**：
- [ ] 所有 CRUD Use Case 实现
- [ ] 包含输入验证逻辑
- [ ] 返回 Result 类型处理错误

---

#### T4.1.2 实现活动类型列表ViewModel

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.1.2 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T4.1.1 |

**任务描述**：
实现活动类型列表的状态管理和事件处理。

**关键代码**：
```kotlin
// ActivityTypeListViewModel.kt
@HiltViewModel
class ActivityTypeListViewModel @Inject constructor(
    private val getActivityTypes: GetActivityTypesUseCase,
    private val deleteActivityType: DeleteActivityTypeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ActivityTypeListUiState())
    val uiState: StateFlow<ActivityTypeListUiState> = _uiState.asStateFlow()

    private val _events = Channel<ActivityTypeListEvent>()
    val events = _events.receiveAsFlow()

    init {
        loadActivityTypes()
    }

    private fun loadActivityTypes() {
        viewModelScope.launch {
            getActivityTypes(includeArchived = false)
                .collect { types ->
                    _uiState.update { it.copy(
                        activityTypes = types,
                        isLoading = false
                    )}
                }
        }
    }

    fun onDelete(id: Long) {
        viewModelScope.launch {
            deleteActivityType(id)
                .onSuccess { _events.send(ActivityTypeListEvent.DeleteSuccess) }
                .onFailure { _events.send(ActivityTypeListEvent.Error(it.message ?: "删除失败")) }
        }
    }
}

data class ActivityTypeListUiState(
    val activityTypes: List<ActivityType> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

sealed class ActivityTypeListEvent {
    object DeleteSuccess : ActivityTypeListEvent()
    data class Error(val message: String) : ActivityTypeListEvent()
}
```

**验收标准**：
- [ ] StateFlow 暴露 UI 状态
- [ ] Channel 处理一次性事件
- [ ] 加载状态正确管理

---

#### T4.1.3 实现活动类型列表界面

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.1.3 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T4.1.2 |

**任务描述**：
实现活动类型列表的 Compose UI 界面。

**关键代码**：
```kotlin
@Composable
fun ActivityTypeListScreen(
    onNavigateToEdit: (Long?) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ActivityTypeListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            BlockwiseTopAppBarWithBack(
                title = "活动类型",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { onNavigateToEdit(null) }) {
                        Icon(Icons.Default.Add, "添加")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> BlockwiseFullScreenLoading()
            uiState.activityTypes.isEmpty() -> BlockwiseEmptyState(
                title = "暂无活动类型",
                description = "点击右上角添加按钮创建",
                actionText = "添加活动类型",
                onAction = { onNavigateToEdit(null) }
            )
            else -> ActivityTypeList(
                activityTypes = uiState.activityTypes,
                onItemClick = { onNavigateToEdit(it.id) },
                modifier = Modifier.padding(padding)
            )
        }
    }
}
```

**验收标准**：
- [ ] 列表正确显示活动类型
- [ ] 空状态显示正确
- [ ] 加载状态显示正确
- [ ] 点击跳转编辑页面

---

#### T4.1.4 实现活动类型编辑界面

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.1.4 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T4.1.2 |

**任务描述**：
实现活动类型创建/编辑的表单界面。

**验收标准**：
- [ ] 创建模式正常工作
- [ ] 编辑模式加载现有数据
- [ ] 表单验证正确
- [ ] 保存成功后返回

---

#### T4.1.5 实现活动类型颜色选择器

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.1.5 |
| **优先级** | P1 |
| **预计耗时** | 1.5h |
| **依赖任务** | T4.1.4 |

**任务描述**：
实现颜色选择组件，提供预设颜色和自定义颜色。

**验收标准**：
- [ ] 预设颜色网格显示
- [ ] 选中状态标识
- [ ] 颜色预览正确

---

#### T4.1.6 预置默认活动类型

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.1.6 |
| **优先级** | P1 |
| **预计耗时** | 1h |
| **依赖任务** | T4.1.1 |

**任务描述**：
首次启动时预置默认活动类型数据。

**预置数据**：
- 工作（蓝色）
- 学习（绿色）
- 运动（橙色）
- 休息（灰色）
- 娱乐（紫色）
- 其他（青色）

**验收标准**：
- [ ] 首次启动自动创建
- [ ] 非首次启动不重复创建
- [ ] 颜色和图标正确

---

### 2.2 标签管理

#### T4.2.1 实现标签Use Cases

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.2.1 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T3.4.7 |

**任务描述**：
实现标签的业务逻辑用例。

**验收标准**：
- [ ] GetTagsUseCase 实现
- [ ] CreateTagUseCase 实现（含名称唯一性验证）
- [ ] UpdateTagUseCase 实现
- [ ] DeleteTagUseCase 实现

---

#### T4.2.2 实现标签管理ViewModel

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.2.2 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T4.2.1 |

**验收标准**：
- [ ] 标签列表状态管理
- [ ] CRUD 操作处理
- [ ] 错误状态处理

---

#### T4.2.3 实现标签管理界面

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.2.3 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T4.2.2 |

**验收标准**：
- [ ] 标签列表显示
- [ ] 添加/编辑入口
- [ ] 删除确认对话框

---

#### T4.2.4 实现标签编辑对话框

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.2.4 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T4.2.2 |

**验收标准**：
- [ ] 对话框形式编辑
- [ ] 名称和颜色输入
- [ ] 保存/取消功能

---

### 2.3 手动记录功能

#### T4.3.1 实现创建时间记录Use Case

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.3.1 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T3.4.8 |

**任务描述**：
实现创建时间记录的业务逻辑。

**关键代码**：
```kotlin
class CreateTimeEntryUseCase @Inject constructor(
    private val repository: TimeEntryRepository
) {
    suspend operator fun invoke(input: TimeEntryInput): Result<Long> {
        // Validation
        if (input.startTime >= input.endTime) {
            return Result.failure(IllegalArgumentException("结束时间必须晚于开始时间"))
        }
        if (input.durationMinutes <= 0) {
            return Result.failure(IllegalArgumentException("时长必须大于0"))
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
- [ ] 时间有效性验证
- [ ] 活动类型存在性验证
- [ ] 创建成功返回 ID

---

#### T4.3.2 实现编辑时间记录Use Case

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.3.2 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T3.4.8 |

**验收标准**：
- [ ] 更新现有记录
- [ ] 验证逻辑复用
- [ ] 标签关联更新

---

#### T4.3.3 实现删除时间记录Use Case

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.3.3 |
| **优先级** | P0 |
| **预计耗时** | 0.5h |
| **依赖任务** | T3.4.8 |

**验收标准**：
- [ ] 删除记录
- [ ] 关联标签自动删除（CASCADE）

---

#### T4.3.4 实现时间记录编辑ViewModel

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.3.4 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T4.3.1, T4.3.2, T4.3.3 |

**关键代码**：
```kotlin
@HiltViewModel
class TimeEntryEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getTimeEntry: GetTimeEntryByIdUseCase,
    private val createTimeEntry: CreateTimeEntryUseCase,
    private val updateTimeEntry: UpdateTimeEntryUseCase,
    private val getActivityTypes: GetActivityTypesUseCase,
    private val getTags: GetTagsUseCase
) : ViewModel() {

    private val entryId: Long? = savedStateHandle.get<Long>("entryId")?.takeIf { it > 0 }
    val isEditMode = entryId != null

    private val _uiState = MutableStateFlow(TimeEntryEditUiState())
    val uiState: StateFlow<TimeEntryEditUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            // Load activity types and tags
            combine(
                getActivityTypes(),
                getTags()
            ) { activities, tags ->
                _uiState.update { it.copy(
                    activityTypes = activities,
                    availableTags = tags
                )}
            }.collect()
        }

        // Load existing entry if editing
        entryId?.let { id ->
            viewModelScope.launch {
                getTimeEntry(id)?.let { entry ->
                    _uiState.update { it.copy(
                        selectedActivityId = entry.activity.id,
                        startTime = entry.startTime,
                        endTime = entry.endTime,
                        note = entry.note ?: "",
                        selectedTagIds = entry.tags.map { it.id }.toSet()
                    )}
                }
            }
        }
    }

    fun save() {
        viewModelScope.launch {
            val state = _uiState.value
            val input = TimeEntryInput(
                activityId = state.selectedActivityId ?: return@launch,
                startTime = state.startTime ?: return@launch,
                endTime = state.endTime ?: return@launch,
                note = state.note.takeIf { it.isNotBlank() },
                tagIds = state.selectedTagIds.toList()
            )

            val result = if (isEditMode) {
                updateTimeEntry(entryId!!, input)
            } else {
                createTimeEntry(input)
            }

            result
                .onSuccess { _events.send(TimeEntryEditEvent.SaveSuccess) }
                .onFailure { _events.send(TimeEntryEditEvent.Error(it.message ?: "保存失败")) }
        }
    }
}
```

**验收标准**：
- [ ] 创建/编辑模式区分
- [ ] 表单状态管理
- [ ] 保存逻辑正确

---

#### T4.3.5 实现时间记录编辑界面

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.3.5 |
| **优先级** | P0 |
| **预计耗时** | 3h |
| **依赖任务** | T4.3.4 |

**验收标准**：
- [ ] 活动类型选择器
- [ ] 日期时间选择器
- [ ] 标签多选
- [ ] 备注输入
- [ ] 表单验证提示

---

#### T4.3.6 实现记录时间重叠校验

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.3.6 |
| **优先级** | P1 |
| **预计耗时** | 1h |
| **依赖任务** | T4.3.1 |

**任务描述**：
在创建/编辑记录时检测时间重叠，给出警告。

**验收标准**：
- [ ] 检测重叠记录
- [ ] 显示警告对话框
- [ ] 允许用户选择继续或取消

---
