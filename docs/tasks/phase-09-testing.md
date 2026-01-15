---
文档类型: 阶段任务文档
阶段编号: 09
阶段名称: 测试与质量保证模块
版本: v1.0
创建日期: 2026-01-15
预计工期: 4-5天
前置条件: 阶段一至八完成
---

# 阶段九：测试与质量保证模块

## 1. 阶段目标

### 1.1 核心目标

建立 Blockwise 应用的测试体系，包括单元测试、集成测试、UI测试，确保代码质量和应用稳定性。

### 1.2 交付成果

- 单元测试覆盖核心业务逻辑
- Repository 层集成测试
- ViewModel 测试
- UI 组件测试
- 端到端测试
- 测试覆盖率报告

### 1.3 功能优先级

| 功能 | 优先级 | 说明 |
|------|--------|------|
| Use Case 单元测试 | P0 | 业务逻辑验证 |
| Repository 测试 | P0 | 数据层验证 |
| ViewModel 测试 | P0 | 状态管理验证 |
| UI 组件测试 | P1 | 界面交互验证 |
| 端到端测试 | P2 | 完整流程验证 |

---

## 2. 任务列表

### 2.1 测试基础设施

#### T9.1.1 配置测试依赖

| 属性 | 值 |
|------|-----|
| **任务ID** | T9.1.1 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T1.3.1 |

**关键代码**：
```kotlin
// build.gradle.kts (app)
dependencies {
    // Unit Testing
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)

    // Android Testing
    androidTestImplementation(libs.androidx.test.ext)
    androidTestImplementation(libs.androidx.test.espresso)
    androidTestImplementation(libs.androidx.compose.ui.test)
    androidTestImplementation(libs.hilt.android.testing)

    // Room Testing
    testImplementation(libs.room.testing)
}
```

**验收标准**：
- [ ] JUnit 5 配置
- [ ] MockK 配置
- [ ] Coroutines Test 配置
- [ ] Compose UI Test 配置

---

#### T9.1.2 创建测试工具类

| 属性 | 值 |
|------|-----|
| **任务ID** | T9.1.2 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T9.1.1 |

**关键代码**：
```kotlin
// TestDispatcherRule.kt
class TestDispatcherRule(
    private val dispatcher: TestDispatcher = UnconfinedTestDispatcher()
) : TestWatcher() {
    override fun starting(description: Description) {
        Dispatchers.setMain(dispatcher)
    }

    override fun finished(description: Description) {
        Dispatchers.resetMain()
    }
}

// FakeTimeEntryRepository.kt
class FakeTimeEntryRepository : TimeEntryRepository {
    private val entries = mutableListOf<TimeEntry>()
    private val _entriesFlow = MutableStateFlow<List<TimeEntry>>(emptyList())

    override fun getAll(): Flow<List<TimeEntry>> = _entriesFlow

    override suspend fun create(input: TimeEntryInput): Long {
        val id = (entries.maxOfOrNull { it.id } ?: 0) + 1
        // Create and add entry
        _entriesFlow.value = entries.toList()
        return id
    }
}
```

**验收标准**：
- [ ] TestDispatcherRule 实现
- [ ] Fake Repository 实现
- [ ] 测试数据工厂方法

---

### 2.2 Use Case 单元测试

#### T9.2.1 时间记录 Use Case 测试

| 属性 | 值 |
|------|-----|
| **任务ID** | T9.2.1 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T9.1.2, T4.3.1 |

**关键代码**：
```kotlin
class CreateTimeEntryUseCaseTest {
    @get:Rule
    val dispatcherRule = TestDispatcherRule()

    private lateinit var useCase: CreateTimeEntryUseCase
    private lateinit var repository: FakeTimeEntryRepository

    @Before
    fun setup() {
        repository = FakeTimeEntryRepository()
        useCase = CreateTimeEntryUseCase(repository)
    }

    @Test
    fun `create entry with valid input returns success`() = runTest {
        val input = TimeEntryInput(
            activityId = 1L,
            startTime = Clock.System.now(),
            endTime = Clock.System.now() + 1.hours,
            tagIds = emptyList()
        )

        val result = useCase(input)

        assertTrue(result.isSuccess)
        assertEquals(1L, result.getOrNull())
    }

    @Test
    fun `create entry with end before start returns failure`() = runTest {
        val now = Clock.System.now()
        val input = TimeEntryInput(
            activityId = 1L,
            startTime = now,
            endTime = now - 1.hours,
            tagIds = emptyList()
        )

        val result = useCase(input)

        assertTrue(result.isFailure)
    }
}
```

**验收标准**：
- [ ] 创建记录成功测试
- [ ] 时间验证失败测试
- [ ] 边界条件测试

---

#### T9.2.2 统计 Use Case 测试

| 属性 | 值 |
|------|-----|
| **任务ID** | T9.2.2 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T9.1.2, T5.1.1 |

**验收标准**：
- [ ] 按活动类型统计测试
- [ ] 按标签统计测试
- [ ] 百分比计算测试

---

#### T9.2.3 目标 Use Case 测试

| 属性 | 值 |
|------|-----|
| **任务ID** | T9.2.3 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T9.1.2, T6.1.1 |

**验收标准**：
- [ ] 创建目标测试
- [ ] 进度计算测试
- [ ] 完成状态判断测试

---

### 2.3 Repository 集成测试

#### T9.3.1 Room 数据库测试配置

| 属性 | 值 |
|------|-----|
| **任务ID** | T9.3.1 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T9.1.1, T3.2.1 |

**验收标准**：
- [ ] 内存数据库配置
- [ ] 测试数据库创建/销毁

---

#### T9.3.2 TimeEntry Repository 测试

| 属性 | 值 |
|------|-----|
| **任务ID** | T9.3.2 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T9.3.1 |

**验收标准**：
- [ ] CRUD 操作测试
- [ ] 查询条件测试
- [ ] 关联数据测试

---

#### T9.3.3 Statistics Repository 测试

| 属性 | 值 |
|------|-----|
| **任务ID** | T9.3.3 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T9.3.1 |

**验收标准**：
- [ ] 聚合查询测试
- [ ] 时间范围过滤测试

---

### 2.4 ViewModel 测试

#### T9.4.1 TimelineViewModel 测试

| 属性 | 值 |
|------|-----|
| **任务ID** | T9.4.1 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T9.1.2, T4.5.2 |

**验收标准**：
- [ ] 初始状态测试
- [ ] 数据加载测试
- [ ] 删除操作测试

---

#### T9.4.2 StatisticsViewModel 测试

| 属性 | 值 |
|------|-----|
| **任务ID** | T9.4.2 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T9.1.2, T5.3.1 |

**验收标准**：
- [ ] 周期切换测试
- [ ] 数据加载测试
- [ ] 错误状态测试

---

#### T9.4.3 GoalListViewModel 测试

| 属性 | 值 |
|------|-----|
| **任务ID** | T9.4.3 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T9.1.2, T6.3.1 |

**验收标准**：
- [ ] 目标列表加载测试
- [ ] 进度计算整合测试

---

### 2.5 UI 组件测试

#### T9.5.1 Compose 测试配置

| 属性 | 值 |
|------|-----|
| **任务ID** | T9.5.1 |
| **优先级** | P1 |
| **预计耗时** | 1h |
| **依赖任务** | T9.1.1 |

**验收标准**：
- [ ] ComposeTestRule 配置
- [ ] 测试主题配置

---

#### T9.5.2 基础组件测试

| 属性 | 值 |
|------|-----|
| **任务ID** | T9.5.2 |
| **优先级** | P1 |
| **预计耗时** | 2h |
| **依赖任务** | T9.5.1 |

**验收标准**：
- [ ] 按钮点击测试
- [ ] 输入框测试
- [ ] 卡片组件测试

---

#### T9.5.3 图表组件测试

| 属性 | 值 |
|------|-----|
| **任务ID** | T9.5.3 |
| **优先级** | P1 |
| **预计耗时** | 1.5h |
| **依赖任务** | T9.5.1 |

**验收标准**：
- [ ] 饼图渲染测试
- [ ] 柱状图渲染测试

---

## 3. 依赖关系图

```
T9.1.1 测试依赖配置
        │
        └── T9.1.2 测试工具类
                │
                ├── T9.2.1 时间记录UseCase测试
                ├── T9.2.2 统计UseCase测试
                └── T9.2.3 目标UseCase测试

T9.1.1 ─── T9.3.1 Room测试配置
                │
                ├── T9.3.2 TimeEntry Repository测试
                └── T9.3.3 Statistics Repository测试

T9.1.2 ─── T9.4.1 TimelineViewModel测试
        ├── T9.4.2 StatisticsViewModel测试
        └── T9.4.3 GoalListViewModel测试

T9.1.1 ─── T9.5.1 Compose测试配置
                │
                ├── T9.5.2 基础组件测试
                └── T9.5.3 图表组件测试
```

---

## 4. 验收标准清单

### 4.1 单元测试验收

| 验收项 | 要求 |
|--------|------|
| Use Case 测试 | 核心业务逻辑覆盖 |
| 边界条件 | 异常输入处理测试 |
| 覆盖率 | Use Case 层 > 80% |

### 4.2 集成测试验收

| 验收项 | 要求 |
|--------|------|
| Repository 测试 | CRUD 操作正确 |
| 数据库测试 | 查询结果正确 |
| 覆盖率 | Repository 层 > 70% |

### 4.3 ViewModel 测试验收

| 验收项 | 要求 |
|--------|------|
| 状态管理 | 状态变化正确 |
| 事件处理 | 用户操作响应正确 |
| 覆盖率 | ViewModel 层 > 70% |

### 4.4 UI 测试验收

| 验收项 | 要求 |
|--------|------|
| 组件渲染 | 正确显示 |
| 交互测试 | 点击响应正确 |

---

## 5. 风险与注意事项

### 5.1 潜在风险

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 测试执行慢 | CI/CD 效率低 | 并行执行、分层测试 |
| Flaky 测试 | 结果不稳定 | 使用 TestDispatcher |

### 5.2 注意事项

1. **测试隔离**：每个测试独立，不依赖执行顺序
2. **Mock 策略**：优先使用 Fake 而非 Mock
3. **异步测试**：使用 runTest 和 Turbine
4. **覆盖率目标**：核心业务逻辑 > 80%

---

*文档版本: v1.0*
*阶段状态: 待开始*
