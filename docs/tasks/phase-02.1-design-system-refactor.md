---
文档类型: 阶段任务文档
阶段编号: 02.1
阶段名称: 设计系统重构 - 现代极简风格
版本: v1.0
创建日期: 2026-01-16
预计工期: 7天
前置条件: 阶段二完成
---

# 阶段二点一：设计系统重构 - 现代极简风格

## 1. 阶段目标

### 1.1 核心目标

将 Blockwise 应用的设计系统从 Material Design 3 风格重构为现代极简主义风格，参考 `docs/prototype/` 中的设计原型，建立全新的视觉语言和组件库。

### 1.2 交付成果

- 现代极简风格的主题系统（深蓝色主色 + Slate 灰色系）
- 重构后的 UI 组件库（34个组件）
- 玻璃态效果的导航组件
- 新增 6 个缺失组件（Input, Switch, Checkbox, Radio, Dropdown, FAB）
- 完整的设计规范文档

### 1.3 设计原则

| 原则 | 说明 |
|------|------|
| 现代极简主义 | 大量留白、细腻边框、微妙阴影 |
| Glassmorphism | 半透明背景、模糊效果 |
| 一致性 | 全应用统一的极简视觉语言 |
| 可访问性 | 确保对比度符合 WCAG 标准 |
| 性能优先 | 优化动画和效果，避免性能问题 |

---

## 2. 设计风格分析

### 2.1 参考原型

**原型文件**:
- `docs/prototype/timeline/code.html` - 时间线页面
- `docs/prototype/add_record/code.html` - 添加记录页面

**设计风格**: 现代极简主义（Modern Minimalism）+ Glassmorphism

### 2.2 核心特征

#### 2.2.1 色彩系统

**主色调**:
```
Primary: #135bec (深蓝色)
```

**背景色**:
```
Light Background: #f6f6f8 (浅灰)
Dark Background: #101622 / #0f1218 (深灰黑)
```

**表面色**:
```
Light Surface: #ffffff (纯白)
Dark Surface: #1e2430 / #181f2b / #232d3d (多层次深灰)
```

**中性色 - Slate 色系**:
```
Slate 50:  #f8fafc
Slate 100: #f1f5f9
Slate 200: #e2e8f0
Slate 300: #cbd5e1
Slate 400: #94a3b8
Slate 500: #64748b
Slate 600: #475569
Slate 700: #334155
Slate 800: #1e293b
Slate 900: #0f172a
```

#### 2.2.2 排版系统

**字体**: Inter（无衬线，现代感）

**特殊样式**:
- 小号大写字母标签: `uppercase tracking-wider`
- 等宽字体显示时间: `font-mono`

**字号层级**（简化）:
```
Headline Large:  28sp / 36sp
Headline Medium: 24sp / 32sp
Title Large:     20sp / 28sp
Title Medium:    16sp / 24sp
Title Small:     14sp / 20sp
Body Large:      16sp / 24sp
Body Medium:     14sp / 20sp
Label Large:     12sp / 16sp
```

#### 2.2.3 视觉元素

**圆角**:
```
Small:      8dp  (rounded-lg)
Medium:     12dp (rounded-xl)
Large:      16dp (rounded-2xl)
Extra Large: 24dp (rounded-3xl)
```

**边框**:
```
Width: 1dp
Color: Slate 100 (浅色) / Slate 700 (深色)
Opacity: 0.2-0.6
```

**阴影**:
```
微妙阴影 + 细腻边框（而非 Material Design 3 的明显阴影）
```

**背景效果**:
```
玻璃态: backdrop-blur-xl (半透明 + 模糊)
```

#### 2.2.4 交互设计

**动画效果**:
- Hover: 边框颜色变化、背景色微调
- Active: Scale 0.95 (缩放动画)
- Transition: 平滑过渡 (200-300ms)
- Pulse: 脉冲动画（实时追踪指示器）

---

## 3. 任务列表

### 3.1 阶段一：主题系统重构（优先级 P0）

#### T2.1.1 重构 Color.kt

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.1.1 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | - |

**任务描述**:
重构颜色系统，移除 Material Design 3 紫色系，采用深蓝色主色和 Slate 灰色系。

**执行步骤**:
1. 定义主色 `#135bec`
2. 定义 Slate 灰色系（8个层级）
3. 定义浅色/深色背景色
4. 定义多层次表面色
5. 保留并调整 12 种活动类型颜色
6. 移除 Material Design 3 容器色

**关键代码**:
```kotlin
// Primary Color
val Primary = Color(0xFF135BEC)
val OnPrimary = Color(0xFFFFFFFF)

// Slate Colors
val Slate50 = Color(0xFFF8FAFC)
val Slate100 = Color(0xFFF1F5F9)
// ... 其他 Slate 色

// Background Colors
val BackgroundLight = Color(0xFFF6F6F8)
val BackgroundDark = Color(0xFF101622)

// Surface Colors
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1E2430)
val SurfaceCardDark = Color(0xFF232D3D)
```

**验收标准**:
- [ ] 主色改为深蓝色 `#135bec`
- [ ] Slate 灰色系 8 个层级定义完成
- [ ] 浅色/深色背景色定义完成
- [ ] 多层次表面色定义完成
- [ ] 活动类型颜色保留并调整

---

#### T2.1.2 简化 Type.kt

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.1.2 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | - |

**任务描述**:
简化排版系统，从 15 个层级减少到 8 个核心层级。

**执行步骤**:
1. 移除 Display 系列（displayLarge, displayMedium, displaySmall）
2. 保留 Headline (2个)
3. 保留 Title (3个)
4. 保留 Body (2个)
5. 保留 Label (1个)
6. 调整字号和行高

**关键代码**:
```kotlin
val Typography = Typography(
    headlineLarge = TextStyle(
        fontSize = 28.sp,
        lineHeight = 36.sp,
        fontWeight = FontWeight.Bold
    ),
    headlineMedium = TextStyle(
        fontSize = 24.sp,
        lineHeight = 32.sp,
        fontWeight = FontWeight.Bold
    ),
    // ... 其他层级
)
```

**验收标准**:
- [ ] Display 系列已移除
- [ ] 保留 8 个核心层级
- [ ] 字号和行高调整完成
- [ ] 排版层级清晰区分

---

#### T2.1.3 调整 Dimensions.kt

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.1.3 |
| **优先级** | P0 |
| **预计耗时** | 0.5h |
| **依赖任务** | - |

**任务描述**:
调整间距和尺寸系统，增大圆角半径，增加留白。

**执行步骤**:
1. 增大圆角半径（8dp, 12dp, 16dp, 24dp）
2. 增加间距（更多留白）
3. 调整组件尺寸

**关键代码**:
```kotlin
object CornerRadius {
    val small = 8.dp       // rounded-lg
    val medium = 12.dp     // rounded-xl
    val large = 16.dp      // rounded-2xl
    val extraLarge = 24.dp // rounded-3xl
}

object Spacing {
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val extraLarge = 32.dp
    val huge = 48.dp
}
```

**验收标准**:
- [ ] 圆角半径增大
- [ ] 间距系统调整完成
- [ ] 组件尺寸调整完成

---

#### T2.1.4 重构 Theme.kt

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.1.4 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T2.1.1, T2.1.2, T2.1.3 |

**任务描述**:
重构主题配置，移除 Material Design 3 动态色彩，简化为固定色彩方案。

**执行步骤**:
1. 移除动态色彩支持
2. 使用新的色彩系统
3. 优化深色模式配色
4. 简化主题配置

**关键代码**:
```kotlin
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    background = BackgroundLight,
    surface = SurfaceLight,
    // ... 使用新色彩系统
)

private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    background = BackgroundDark,
    surface = SurfaceDark,
    // ... 使用新色彩系统
)

@Composable
fun BlockwiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

**验收标准**:
- [ ] 动态色彩支持已移除
- [ ] 使用新的色彩系统
- [ ] 深色模式配色优化
- [ ] 主题配置简化

---

### 3.2 阶段二：基础组件重构（优先级 P0）

#### T2.2.1 重构 Button.kt

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.2.1 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T2.1.4 |

**任务描述**:
重构按钮组件，采用边框样式，增加动画效果。

**执行步骤**:
1. 主按钮：深蓝色背景 + 白色文字
2. 次按钮：透明背景 + 深蓝色边框
3. 文字按钮：无边框，仅文字
4. 增加 hover 和 active 状态动画
5. 增大圆角

**验收标准**:
- [ ] 主按钮样式正确
- [ ] 次按钮边框样式正确
- [ ] 文字按钮样式正确
- [ ] 动画效果流畅
- [ ] 圆角增大

---

#### T2.2.2 重构 Card.kt

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.2.2 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T2.1.4 |

**任务描述**:
重构卡片组件，移除阴影，使用细腻边框。

**执行步骤**:
1. 移除阴影效果
2. 使用细腻边框（1dp, Slate 色）
3. 增大圆角（16-24dp）
4. 添加 hover 效果

**验收标准**:
- [ ] 阴影已移除
- [ ] 边框样式正确
- [ ] 圆角增大
- [ ] Hover 效果正常

---

#### T2.2.3 重构 Dialog.kt

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.2.3 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T2.1.4 |

**任务描述**:
重构对话框组件，简化样式，增大圆角。

**执行步骤**:
1. 简化对话框样式
2. 增大圆角
3. 优化按钮布局
4. 添加背景模糊效果（可选）

**验收标准**:
- [ ] 对话框样式简化
- [ ] 圆角增大
- [ ] 按钮布局优化
- [ ] 背景效果正确

---

### 3.3 阶段三：导航组件重构（优先级 P0）

#### T2.3.1 重构 TopAppBar.kt

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.3.1 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T2.1.4 |

**任务描述**:
重构顶部应用栏，添加玻璃态效果。

**执行步骤**:
1. 半透明背景
2. 细腻的底部边框
3. 简化图标和文字样式
4. 添加模糊效果（Android 12+）

**验收标准**:
- [ ] 半透明背景正确
- [ ] 底部边框细腻
- [ ] 图标文字简化
- [ ] 模糊效果正常（Android 12+）

---

#### T2.3.2 重构 BottomNavigation.kt

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.3.2 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T2.1.4 |

**任务描述**:
重构底部导航栏，添加玻璃态效果。

**执行步骤**:
1. 半透明背景
2. 细腻的顶部边框
3. 简化图标样式
4. 优化选中状态指示

**验收标准**:
- [ ] 半透明背景正确
- [ ] 顶部边框细腻
- [ ] 图标样式简化
- [ ] 选中状态优化

---

### 3.4 阶段四：选择器组件重构（优先级 P1）

#### T2.4.1 重构 DatePicker.kt

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.4.1 |
| **优先级** | P1 |
| **预计耗时** | 1h |
| **依赖任务** | T2.1.4 |

**任务描述**:
重构日期选择器，使用新的色彩系统。

**验收标准**:
- [ ] 使用新色彩系统
- [ ] 选中状态优化
- [ ] 样式简化

---

#### T2.4.2 重构 TimePicker.kt

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.4.2 |
| **优先级** | P1 |
| **预计耗时** | 2h |
| **依赖任务** | T2.1.4 |

**任务描述**:
重构时间选择器，参考 prototype 中的滚轮选择器。

**验收标准**:
- [ ] 滚轮选择器样式正确
- [ ] 渐变遮罩效果正常
- [ ] 选中状态高亮

---

#### T2.4.3 重构 TagSelector.kt

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.4.3 |
| **优先级** | P1 |
| **预计耗时** | 1h |
| **依赖任务** | T2.1.4 |

**任务描述**:
重构标签选择器，简化样式。

**验收标准**:
- [ ] 标签芯片样式简化
- [ ] 颜色标识优化
- [ ] 间距和圆角调整

---

### 3.5 阶段五：状态组件重构（优先级 P1）

#### T2.5.1 重构 Progress.kt

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.5.1 |
| **优先级** | P1 |
| **预计耗时** | 1h |
| **依赖任务** | T2.1.4 |

**任务描述**:
重构进度条组件，简化样式。

**验收标准**:
- [ ] 进度条样式简化
- [ ] 颜色和圆角调整
- [ ] 动画效果优化

---

#### T2.5.2 重构 LoadingIndicator.kt

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.5.2 |
| **优先级** | P1 |
| **预计耗时** | 1h |
| **依赖任务** | T2.1.4 |

**任务描述**:
重构加载指示器，优化 Shimmer 效果。

**验收标准**:
- [ ] 加载指示器简化
- [ ] Shimmer 效果颜色调整
- [ ] 骨架屏样式优化

---

#### T2.5.3 重构 EmptyState.kt

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.5.3 |
| **优先级** | P1 |
| **预计耗时** | 0.5h |
| **依赖任务** | T2.1.4 |

**任务描述**:
重构空状态组件，简化样式。

**验收标准**:
- [ ] 空状态样式简化
- [ ] 图标大小和颜色调整
- [ ] 文字排版优化

---

### 3.6 阶段六：新增组件（优先级 P2）

#### T2.6.1 新增 Input.kt

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.6.1 |
| **优先级** | P2 |
| **预计耗时** | 2h |
| **依赖任务** | T2.1.4 |

**任务描述**:
创建输入框组件。

**验收标准**:
- [ ] 透明背景 + 底部边框
- [ ] Focus 状态边框颜色变化
- [ ] 支持标签和错误提示

---

#### T2.6.2 新增 Switch.kt

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.6.2 |
| **优先级** | P2 |
| **预计耗时** | 1h |
| **依赖任务** | T2.1.4 |

**任务描述**:
创建开关组件。

**验收标准**:
- [ ] 开关样式符合极简风格
- [ ] 动画效果流畅
- [ ] 颜色使用新色彩系统

---

#### T2.6.3 新增 Checkbox.kt

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.6.3 |
| **优先级** | P2 |
| **预计耗时** | 1h |
| **依赖任务** | T2.1.4 |

**任务描述**:
创建复选框组件。

**验收标准**:
- [ ] 复选框样式符合极简风格
- [ ] 选中动画流畅
- [ ] 颜色使用新色彩系统

---

#### T2.6.4 新增 Radio.kt

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.6.4 |
| **优先级** | P2 |
| **预计耗时** | 1h |
| **依赖任务** | T2.1.4 |

**任务描述**:
创建单选框组件。

**验收标准**:
- [ ] 单选框样式符合极简风格
- [ ] 选中动画流畅
- [ ] 颜色使用新色彩系统

---

#### T2.6.5 新增 Dropdown.kt

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.6.5 |
| **优先级** | P2 |
| **预计耗时** | 2h |
| **依赖任务** | T2.1.4 |

**任务描述**:
创建下拉菜单组件。

**验收标准**:
- [ ] 下拉菜单样式符合极简风格
- [ ] 展开/收起动画流畅
- [ ] 颜色使用新色彩系统

---

#### T2.6.6 新增 FAB.kt

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.6.6 |
| **优先级** | P2 |
| **预计耗时** | 1h |
| **依赖任务** | T2.1.4 |

**任务描述**:
创建浮动操作按钮组件。

**验收标准**:
- [ ] FAB 样式符合极简风格
- [ ] 阴影效果微妙
- [ ] 颜色使用新色彩系统

---

## 4. 技术实现要点

### 4.1 玻璃态效果实现

**方案 1**: 使用半透明背景 + 模糊图层（Android 12+）
```kotlin
Modifier
    .background(Color.White.copy(alpha = 0.9f))
    .blur(radius = 20.dp)
```

**方案 2**: 使用渐变背景模拟（兼容方案）
```kotlin
Modifier.background(
    Brush.verticalGradient(
        colors = listOf(
            Color.White.copy(alpha = 0.95f),
            Color.White.copy(alpha = 0.85f)
        )
    )
)
```

### 4.2 动画效果

**Scale 动画**:
```kotlin
val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.95f else 1f,
    animationSpec = spring(stiffness = Spring.StiffnessLow)
)
```

**颜色过渡**:
```kotlin
val borderColor by animateColorAsState(
    targetValue = if (isHovered) Primary else Slate200,
    animationSpec = tween(durationMillis = 200)
)
```

### 4.3 圆角和边框

```kotlin
Modifier
    .clip(RoundedCornerShape(16.dp))
    .border(
        width = 1.dp,
        color = Slate200.copy(alpha = 0.6f),
        shape = RoundedCornerShape(16.dp)
    )
```

---

## 5. 验收标准

### 5.1 视觉验收
- [ ] 色彩系统符合 prototype 设计
- [ ] 圆角、边框、间距符合极简风格
- [ ] 深色模式配色协调
- [ ] 所有组件风格统一

### 5.2 功能验收
- [ ] 所有组件功能正常
- [ ] 动画效果流畅
- [ ] 交互反馈明确
- [ ] 无性能问题

### 5.3 代码质量验收
- [ ] 所有组件有 Preview 函数
- [ ] 代码注释完整
- [ ] 遵循 Kotlin 编码规范
- [ ] 无编译警告

---

## 6. 预计工期

| 阶段 | 任务 | 预计工期 |
|------|------|----------|
| 阶段 1 | 主题系统重构 | 1 天 |
| 阶段 2 | 基础组件重构 | 1.5 天 |
| 阶段 3 | 导航组件重构 | 0.5 天 |
| 阶段 4 | 选择器组件重构 | 1 天 |
| 阶段 5 | 状态组件重构 | 0.5 天 |
| 阶段 6 | 新增组件 | 1.5 天 |
| 测试与优化 | 全面测试和调优 | 1 天 |
| **总计** | | **7 天** |

---

*文档版本: v1.0*
*阶段状态: 进行中*
*创建时间: 2026-01-16*
*预计完成: 2026-01-23*
