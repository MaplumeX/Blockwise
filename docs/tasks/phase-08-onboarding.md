---
文档类型: 阶段任务文档
阶段编号: 08
阶段名称: 引导与用户体验模块
版本: v1.0
创建日期: 2026-01-15
预计工期: 3-4天
前置条件: 阶段二完成
---

# 阶段八：引导与用户体验模块

## 1. 阶段目标

### 1.1 核心目标

实现 Blockwise 应用的新用户引导流程和用户体验优化功能，包括首次启动引导、功能提示、空状态设计、加载状态优化等。

### 1.2 交付成果

- 首次启动引导页面
- 功能引导提示
- 空状态设计
- 加载状态优化
- 错误处理与提示
- 手势操作引导

### 1.3 功能优先级

| 功能 | 优先级 | 说明 |
|------|--------|------|
| 首次引导 | P0 | 新用户体验 |
| 空状态设计 | P0 | 界面完整性 |
| 加载状态 | P0 | 用户反馈 |
| 错误提示 | P0 | 异常处理 |
| 功能提示 | P1 | 功能发现 |
| 手势引导 | P2 | 高级交互 |

---

## 2. 任务列表

### 2.1 首次启动引导

#### T8.1.1 实现引导页面数据模型

| 属性 | 值 |
|------|-----|
| **任务ID** | T8.1.1 |
| **优先级** | P0 |
| **预计耗时** | 0.5h |
| **依赖任务** | T2.1.3 |

**关键代码**：
```kotlin
// OnboardingPage.kt
data class OnboardingPage(
    val title: String,
    val description: String,
    @DrawableRes val imageRes: Int,
    val backgroundColor: Color
)

val onboardingPages = listOf(
    OnboardingPage(
        title = "记录你的时间",
        description = "轻松追踪每一分钟的去向，了解时间都花在了哪里",
        imageRes = R.drawable.onboarding_track,
        backgroundColor = Color(0xFF6200EE)
    ),
    OnboardingPage(
        title = "可视化分析",
        description = "直观的图表展示，帮助你发现时间使用规律",
        imageRes = R.drawable.onboarding_analyze,
        backgroundColor = Color(0xFF03DAC5)
    ),
    OnboardingPage(
        title = "设定目标",
        description = "制定时间目标，养成良好的时间管理习惯",
        imageRes = R.drawable.onboarding_goal,
        backgroundColor = Color(0xFFFF5722)
    )
)
```

**验收标准**：
- [ ] 数据模型定义完整
- [ ] 预置3-4个引导页内容

---

#### T8.1.2 实现引导页面 ViewModel

| 属性 | 值 |
|------|-----|
| **任务ID** | T8.1.2 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T8.1.1, T7.1.1 |

**关键代码**：
```kotlin
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _currentPage = MutableStateFlow(0)
    val currentPage: StateFlow<Int> = _currentPage.asStateFlow()

    val pages = onboardingPages

    fun nextPage() {
        if (_currentPage.value < pages.size - 1) {
            _currentPage.value++
        }
    }

    fun previousPage() {
        if (_currentPage.value > 0) {
            _currentPage.value--
        }
    }

    fun setPage(index: Int) {
        _currentPage.value = index.coerceIn(0, pages.size - 1)
    }

    fun completeOnboarding() {
        viewModelScope.launch {
            settingsDataStore.setOnboardingCompleted(true)
        }
    }
}
```

**验收标准**：
- [ ] 页面切换逻辑
- [ ] 完成状态持久化

---

#### T8.1.3 实现引导页面 UI

| 属性 | 值 |
|------|-----|
| **任务ID** | T8.1.3 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T8.1.2 |

**关键代码**：
```kotlin
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val currentPage by viewModel.currentPage.collectAsStateWithLifecycle()
    val pagerState = rememberPagerState { viewModel.pages.size }

    LaunchedEffect(currentPage) {
        pagerState.animateScrollToPage(currentPage)
    }

    LaunchedEffect(pagerState.currentPage) {
        viewModel.setPage(pagerState.currentPage)
    }

    Box(modifier = Modifier.fillMaxSize()) {
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize()
        ) { page ->
            OnboardingPageContent(
                page = viewModel.pages[page],
                modifier = Modifier.fillMaxSize()
            )
        }

        // Page indicators
        Row(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 100.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            repeat(viewModel.pages.size) { index ->
                PageIndicator(isSelected = index == currentPage)
            }
        }

        // Navigation buttons
        OnboardingNavigation(
            currentPage = currentPage,
            totalPages = viewModel.pages.size,
            onNext = viewModel::nextPage,
            onSkip = {
                viewModel.completeOnboarding()
                onComplete()
            },
            onComplete = {
                viewModel.completeOnboarding()
                onComplete()
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(24.dp)
        )
    }
}
```

**验收标准**：
- [ ] HorizontalPager 滑动切换
- [ ] 页面指示器显示
- [ ] 跳过/下一步/完成按钮

---

#### T8.1.4 实现引导页面动画

| 属性 | 值 |
|------|-----|
| **任务ID** | T8.1.4 |
| **优先级** | P1 |
| **预计耗时** | 1.5h |
| **依赖任务** | T8.1.3 |

**验收标准**：
- [ ] 页面切换动画
- [ ] 图片淡入效果
- [ ] 指示器动画

---

### 2.2 功能引导提示

#### T8.2.1 实现 Tooltip 组件

| 属性 | 值 |
|------|-----|
| **任务ID** | T8.2.1 |
| **优先级** | P1 |
| **预计耗时** | 1.5h |
| **依赖任务** | T2.1.3 |

**关键代码**：
```kotlin
@Composable
fun FeatureTooltip(
    text: String,
    anchor: TooltipAnchor = TooltipAnchor.BOTTOM,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit
) {
    var showTooltip by remember { mutableStateOf(true) }

    Box {
        content()

        if (showTooltip) {
            Popup(
                alignment = anchor.toAlignment(),
                onDismissRequest = {
                    showTooltip = false
                    onDismiss()
                }
            ) {
                TooltipContent(
                    text = text,
                    onDismiss = {
                        showTooltip = false
                        onDismiss()
                    }
                )
            }
        }
    }
}

enum class TooltipAnchor {
    TOP, BOTTOM, START, END
}
```

**验收标准**：
- [ ] 四个方向定位
- [ ] 点击关闭
- [ ] 箭头指向

---

#### T8.2.2 实现功能提示管理器

| 属性 | 值 |
|------|-----|
| **任务ID** | T8.2.2 |
| **优先级** | P1 |
| **预计耗时** | 1h |
| **依赖任务** | T8.2.1, T7.1.1 |

**验收标准**：
- [ ] 记录已显示的提示
- [ ] 每个提示只显示一次
- [ ] 支持重置提示状态

---

#### T8.2.3 实现首次使用功能提示

| 属性 | 值 |
|------|-----|
| **任务ID** | T8.2.3 |
| **优先级** | P1 |
| **预计耗时** | 1h |
| **依赖任务** | T8.2.2 |

**验收标准**：
- [ ] 计时器首次使用提示
- [ ] 时间块视图首次使用提示
- [ ] 统计页面首次使用提示

---

### 2.3 空状态设计

#### T8.3.1 实现通用空状态组件

| 属性 | 值 |
|------|-----|
| **任务ID** | T8.3.1 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T2.1.3 |

**关键代码**：
```kotlin
@Composable
fun BlockwiseEmptyState(
    @DrawableRes imageRes: Int? = null,
    title: String,
    description: String? = null,
    actionText: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        imageRes?.let {
            Image(
                painter = painterResource(it),
                contentDescription = null,
                modifier = Modifier.size(120.dp),
                colorFilter = ColorFilter.tint(
                    MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(modifier = Modifier.height(24.dp))
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center
        )

        description?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        if (actionText != null && onAction != null) {
            Spacer(modifier = Modifier.height(24.dp))
            BlockwisePrimaryButton(
                text = actionText,
                onClick = onAction
            )
        }
    }
}
```

**验收标准**：
- [ ] 图片/图标显示
- [ ] 标题和描述文本
- [ ] 可选操作按钮

---

#### T8.3.2 实现各页面空状态

| 属性 | 值 |
|------|-----|
| **任务ID** | T8.3.2 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T8.3.1 |

**验收标准**：
- [ ] 时间线空状态（无记录）
- [ ] 统计空状态（无数据）
- [ ] 目标空状态（无目标）
- [ ] 活动类型空状态

---

### 2.4 加载状态优化

#### T8.4.1 实现加载状态组件

| 属性 | 值 |
|------|-----|
| **任务ID** | T8.4.1 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T2.1.3 |

**验收标准**：
- [ ] 全屏加载指示器
- [ ] 内容区域加载指示器
- [ ] 按钮加载状态

---

#### T8.4.2 实现骨架屏组件

| 属性 | 值 |
|------|-----|
| **任务ID** | T8.4.2 |
| **优先级** | P1 |
| **预计耗时** | 1.5h |
| **依赖任务** | T8.4.1 |

**验收标准**：
- [ ] 列表项骨架屏
- [ ] 卡片骨架屏
- [ ] 闪烁动画效果

---

### 2.5 错误处理与提示

#### T8.5.1 实现错误提示组件

| 属性 | 值 |
|------|-----|
| **任务ID** | T8.5.1 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T2.1.3 |

**验收标准**：
- [ ] Snackbar 错误提示
- [ ] 全屏错误页面
- [ ] 重试按钮

---

#### T8.5.2 实现网络错误处理

| 属性 | 值 |
|------|-----|
| **任务ID** | T8.5.2 |
| **优先级** | P1 |
| **预计耗时** | 1h |
| **依赖任务** | T8.5.1 |

**验收标准**：
- [ ] 网络不可用提示
- [ ] 请求超时提示
- [ ] 离线模式提示

---

#### T8.5.3 实现表单验证提示

| 属性 | 值 |
|------|-----|
| **任务ID** | T8.5.3 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T8.5.1 |

**验收标准**：
- [ ] 输入框错误状态
- [ ] 错误信息显示
- [ ] 实时验证反馈

---

## 3. 依赖关系图

```
阶段二完成 (UI组件)
        │
        └── T2.1.3 基础组件
                │
                ├── T8.1.1 引导数据模型
                │       │
                │       └── T8.1.2 ViewModel ─── T8.1.3 引导UI
                │                                    │
                │                                    └── T8.1.4 动画
                │
                ├── T8.2.1 Tooltip组件
                │       │
                │       └── T8.2.2 提示管理器
                │               │
                │               └── T8.2.3 功能提示
                │
                ├── T8.3.1 空状态组件
                │       │
                │       └── T8.3.2 各页面空状态
                │
                ├── T8.4.1 加载组件
                │       │
                │       └── T8.4.2 骨架屏
                │
                └── T8.5.1 错误组件
                        │
                        ├── T8.5.2 网络错误
                        └── T8.5.3 表单验证
```

---

## 4. 验收标准清单

### 4.1 首次引导验收

| 验收项 | 要求 |
|--------|------|
| 引导页面 | 3-4页滑动切换 |
| 页面指示器 | 当前页高亮显示 |
| 导航按钮 | 跳过/下一步/完成正常 |
| 完成状态 | 只显示一次，状态持久化 |

### 4.2 功能提示验收

| 验收项 | 要求 |
|--------|------|
| Tooltip | 四方向定位正确 |
| 提示管理 | 每个提示只显示一次 |
| 首次提示 | 关键功能首次使用时显示 |

### 4.3 空状态验收

| 验收项 | 要求 |
|--------|------|
| 通用组件 | 图片、标题、描述、按钮 |
| 各页面 | 时间线、统计、目标空状态 |

### 4.4 加载与错误验收

| 验收项 | 要求 |
|--------|------|
| 加载指示器 | 全屏/内容区域/按钮 |
| 骨架屏 | 列表/卡片闪烁效果 |
| 错误提示 | Snackbar/全屏/重试 |

---

## 5. 风险与注意事项

### 5.1 潜在风险

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 引导页跳过率高 | 用户不了解功能 | 简洁有吸引力的内容 |
| 提示过多干扰 | 用户体验差 | 控制提示频率和数量 |

### 5.2 注意事项

1. **引导简洁**：3-4页为宜，避免冗长
2. **提示时机**：在用户需要时显示，而非强制
3. **空状态引导**：提供明确的下一步操作
4. **错误友好**：使用用户能理解的语言

---

*文档版本: v1.0*
*阶段状态: 已完成*
