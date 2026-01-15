---
文档类型: 阶段任务文档
阶段编号: 02
阶段名称: 设计系统与基础UI
版本: v1.0
创建日期: 2026-01-15
预计工期: 3-4天
前置条件: 阶段一完成
---

# 阶段二：设计系统与基础UI

## 1. 阶段目标

### 1.1 核心目标

建立 Blockwise 应用的设计系统，包括主题、颜色、字体、通用UI组件，以及应用的基础导航框架，为功能模块开发提供统一的视觉基础和组件库。

### 1.2 交付成果

- 完整的 Material3 主题系统（明暗模式）
- 统一的颜色体系和字体排版
- 可复用的通用UI组件库
- 应用导航框架和主界面骨架
- 底部导航栏和顶部应用栏

### 1.3 设计原则

| 原则 | 说明 |
|------|------|
| Material Design 3 | 遵循 Google 最新设计规范 |
| 一致性 | 全应用统一的视觉语言 |
| 可访问性 | 支持深色模式、足够的对比度 |
| 复用性 | 组件高度可复用，减少重复代码 |

---

## 2. 任务列表

### 2.1 主题系统

#### T2.1.1 定义颜色体系

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.1.1 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T1.3.1 |

**任务描述**：
定义应用的完整颜色体系，包括主色、次色、语义颜色以及活动类型专用颜色。

**执行步骤**：
1. 定义主色调（Primary）和次色调（Secondary）
2. 定义表面色（Surface）和背景色（Background）
3. 定义语义颜色（Success、Warning、Error）
4. 定义活动类型颜色调色板（12-16种可区分颜色）
5. 为明暗模式分别定义颜色值

**关键代码**：
```kotlin
// Color.kt
package com.blockwise.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// Primary Colors
val Primary = Color(0xFF6750A4)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFEADDFF)
val OnPrimaryContainer = Color(0xFF21005D)

// Secondary Colors
val Secondary = Color(0xFF625B71)
val OnSecondary = Color(0xFFFFFFFF)

// Surface Colors
val Surface = Color(0xFFFFFBFE)
val SurfaceVariant = Color(0xFFE7E0EC)

// Semantic Colors
val Success = Color(0xFF4CAF50)
val Warning = Color(0xFFFFC107)
val Error = Color(0xFFB3261E)

// Activity Type Colors (12 distinguishable colors)
val ActivityColors = listOf(
    Color(0xFF4285F4), // Blue
    Color(0xFF34A853), // Green
    Color(0xFFFBBC04), // Yellow
    Color(0xFFEA4335), // Red
    Color(0xFF9C27B0), // Purple
    Color(0xFF00ACC1), // Cyan
    Color(0xFFFF7043), // Deep Orange
    Color(0xFF5C6BC0), // Indigo
    Color(0xFF66BB6A), // Light Green
    Color(0xFFFFCA28), // Amber
    Color(0xFFEC407A), // Pink
    Color(0xFF78909C), // Blue Grey
)

// Dark Theme Colors
val PrimaryDark = Color(0xFFD0BCFF)
val OnPrimaryDark = Color(0xFF381E72)
// ... 其他深色主题颜色
```

**验收标准**：
- [ ] 主色、次色定义完成
- [ ] 语义颜色定义完成
- [ ] 活动类型颜色至少12种
- [ ] 明暗模式颜色分别定义
- [ ] 颜色对比度符合可访问性标准

---

#### T2.1.2 定义字体排版

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.1.2 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T1.3.1 |

**任务描述**：
定义应用的字体排版系统，包括标题、正文、标签等各级字体样式。

**执行步骤**：
1. 定义 Display 样式（大标题）
2. 定义 Headline 样式（标题）
3. 定义 Title 样式（小标题）
4. 定义 Body 样式（正文）
5. 定义 Label 样式（标签、按钮）

**关键代码**：
```kotlin
// Type.kt
package com.blockwise.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 57.sp,
        lineHeight = 64.sp,
        letterSpacing = (-0.25).sp
    ),
    headlineLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.15.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.25.sp
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
)
```

**验收标准**：
- [ ] Typography 对象定义完成
- [ ] 各级字体样式清晰区分
- [ ] 字体大小符合 Material3 规范
- [ ] 行高和字间距配置合理

---

#### T2.1.3 实现明暗主题

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.1.3 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T2.1.1, T2.1.2 |

**任务描述**：
实现完整的 Material3 主题配置，支持明暗模式切换。

**执行步骤**：
1. 创建 LightColorScheme
2. 创建 DarkColorScheme
3. 实现 BlockwiseTheme Composable
4. 添加动态颜色支持（Android 12+）
5. 实现主题状态记忆

**关键代码**：
```kotlin
// Theme.kt
package com.blockwise.core.designsystem.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = PrimaryContainer,
    onPrimaryContainer = OnPrimaryContainer,
    secondary = Secondary,
    onSecondary = OnSecondary,
    surface = Surface,
    surfaceVariant = SurfaceVariant,
    error = Error,
    // ... 其他颜色
)

private val DarkColorScheme = darkColorScheme(
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    // ... 其他深色主题颜色
)

@Composable
fun BlockwiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context)
            else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
```

**验收标准**：
- [ ] LightColorScheme 配置完成
- [ ] DarkColorScheme 配置完成
- [ ] BlockwiseTheme 函数可用
- [ ] 动态颜色在 Android 12+ 设备生效
- [ ] 状态栏颜色随主题自动切换

---

#### T2.1.4 定义间距系统

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.1.4 |
| **优先级** | P1 |
| **预计耗时** | 0.5h |
| **依赖任务** | T1.3.1 |

**任务描述**：
定义统一的间距和尺寸规范，确保全应用视觉一致性。

**执行步骤**：
1. 定义基础间距单位（4dp 倍数）
2. 定义组件间距常量
3. 定义圆角半径常量
4. 定义图标尺寸常量

**关键代码**：
```kotlin
// Dimensions.kt
package com.blockwise.core.designsystem.theme

import androidx.compose.ui.unit.dp

object Spacing {
    val extraSmall = 4.dp
    val small = 8.dp
    val medium = 16.dp
    val large = 24.dp
    val extraLarge = 32.dp
    val huge = 48.dp
}

object CornerRadius {
    val small = 4.dp
    val medium = 8.dp
    val large = 12.dp
    val extraLarge = 16.dp
    val full = 50 // percentage for fully rounded
}

object IconSize {
    val small = 16.dp
    val medium = 24.dp
    val large = 32.dp
    val extraLarge = 48.dp
}

object ComponentSize {
    val buttonHeight = 48.dp
    val inputHeight = 56.dp
    val cardMinHeight = 72.dp
    val bottomNavHeight = 80.dp
}
```

**验收标准**：
- [ ] 间距常量定义完成
- [ ] 圆角半径常量定义完成
- [ ] 图标尺寸常量定义完成
- [ ] 组件尺寸常量定义完成

---

### 2.2 通用UI组件

#### T2.2.1 实现通用按钮组件

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.2.1 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T2.1.3 |

**任务描述**：
实现应用通用按钮组件，包括主按钮、次按钮、文字按钮和图标按钮。

**执行步骤**：
1. 实现 BlockwisePrimaryButton
2. 实现 BlockwiseSecondaryButton
3. 实现 BlockwiseTextButton
4. 实现 BlockwiseIconButton
5. 支持加载状态和禁用状态

**关键代码**：
```kotlin
// Button.kt
package com.blockwise.core.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BlockwisePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled && !loading
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text)
    }
}

@Composable
fun BlockwiseSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(48.dp),
        enabled = enabled
    ) {
        Text(text = text)
    }
}

@Composable
fun BlockwiseTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Text(text = text)
    }
}
```

**验收标准**：
- [ ] 主按钮样式正确
- [ ] 次按钮（OutlinedButton）样式正确
- [ ] 文字按钮样式正确
- [ ] 加载状态显示正确
- [ ] 禁用状态显示正确

---

#### T2.2.2 实现通用卡片组件

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.2.2 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T2.1.3 |

**任务描述**：
实现应用通用卡片组件，包括基础卡片和可点击卡片。

**执行步骤**：
1. 实现 BlockwiseCard 基础卡片
2. 实现 BlockwiseClickableCard 可点击卡片
3. 实现 BlockwiseElevatedCard 阴影卡片
4. 支持自定义内边距和圆角

**关键代码**：
```kotlin
// Card.kt
package com.blockwise.core.designsystem.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun BlockwiseCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun BlockwiseClickableCard(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}

@Composable
fun BlockwiseElevatedCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    ElevatedCard(
        modifier = modifier,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 4.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            content = content
        )
    }
}
```

**验收标准**：
- [ ] 基础卡片样式正确
- [ ] 可点击卡片点击效果正常
- [ ] 阴影卡片阴影效果正确
- [ ] 内边距和圆角配置正确

---

#### T2.2.3 实现通用对话框组件

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.2.3 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T2.1.3 |

**任务描述**：
实现应用通用对话框组件，包括确认对话框、信息对话框和选择对话框。

**执行步骤**：
1. 实现 BlockwiseAlertDialog 确认对话框
2. 实现 BlockwiseInfoDialog 信息对话框
3. 实现 BlockwiseSelectionDialog 选择对话框
4. 实现对话框状态管理 Hook

**关键代码**：
```kotlin
// Dialog.kt
package com.blockwise.core.designsystem.component

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier

@Composable
fun BlockwiseAlertDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "确认",
    dismissText: String = "取消",
    onConfirm: () -> Unit
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = title) },
            text = { Text(text = message) },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm()
                    onDismiss()
                }) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(dismissText)
                }
            }
        )
    }
}

@Composable
fun BlockwiseInfoDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    buttonText: String = "知道了"
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text(text = title) },
            text = { Text(text = message) },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(buttonText)
                }
            }
        )
    }
}

// Dialog State Hook
@Composable
fun rememberDialogState(initialVisible: Boolean = false): DialogState {
    return remember { DialogState(initialVisible) }
}

class DialogState(initialVisible: Boolean) {
    var visible by mutableStateOf(initialVisible)
        private set

    fun show() { visible = true }
    fun hide() { visible = false }
}
```

**验收标准**：
- [ ] 确认对话框显示正确
- [ ] 信息对话框显示正确
- [ ] 选择对话框显示正确
- [ ] 对话框状态管理正常
- [ ] 点击外部可关闭对话框

---

#### T2.2.4 实现日期选择器组件

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.2.4 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T2.1.3 |

**任务描述**：
实现日期选择器组件，支持单日选择和日期范围选择。

**执行步骤**：
1. 实现单日期选择器 BlockwiseDatePicker
2. 实现日期范围选择器 BlockwiseDateRangePicker
3. 封装为对话框形式
4. 支持最小/最大日期限制

**关键代码**：
```kotlin
// DatePicker.kt
package com.blockwise.core.designsystem.component

import androidx.compose.material3.*
import androidx.compose.runtime.*
import kotlinx.datetime.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockwiseDatePickerDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    initialDate: LocalDate? = null,
    minDate: LocalDate? = null,
    maxDate: LocalDate? = null
) {
    if (visible) {
        val datePickerState = rememberDatePickerState(
            initialSelectedDateMillis = initialDate?.toEpochDays()?.times(86400000L)
        )

        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val date = LocalDate.fromEpochDays((millis / 86400000).toInt())
                        onDateSelected(date)
                    }
                    onDismiss()
                }) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockwiseDateRangePickerDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onRangeSelected: (LocalDate, LocalDate) -> Unit,
    initialStartDate: LocalDate? = null,
    initialEndDate: LocalDate? = null
) {
    if (visible) {
        val dateRangePickerState = rememberDateRangePickerState()

        DatePickerDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    val startMillis = dateRangePickerState.selectedStartDateMillis
                    val endMillis = dateRangePickerState.selectedEndDateMillis
                    if (startMillis != null && endMillis != null) {
                        val startDate = LocalDate.fromEpochDays((startMillis / 86400000).toInt())
                        val endDate = LocalDate.fromEpochDays((endMillis / 86400000).toInt())
                        onRangeSelected(startDate, endDate)
                    }
                    onDismiss()
                }) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            }
        ) {
            DateRangePicker(state = dateRangePickerState)
        }
    }
}
```

**验收标准**：
- [ ] 单日期选择器可正常选择日期
- [ ] 日期范围选择器可选择起止日期
- [ ] 对话框显示/隐藏正常
- [ ] 日期限制功能正常
- [ ] 返回的日期格式正确（LocalDate）

---

#### T2.2.5 实现时间选择器组件

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.2.5 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T2.1.3 |

**任务描述**：
实现时间选择器组件，支持时间选择和时长选择。

**执行步骤**：
1. 实现时间选择器 BlockwiseTimePicker
2. 实现时长选择器 BlockwiseDurationPicker
3. 封装为对话框形式
4. 支持24小时制/12小时制切换

**关键代码**：
```kotlin
// TimePicker.kt
package com.blockwise.core.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockwiseTimePickerDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onTimeSelected: (LocalTime) -> Unit,
    initialTime: LocalTime? = null,
    is24Hour: Boolean = true
) {
    if (visible) {
        val timePickerState = rememberTimePickerState(
            initialHour = initialTime?.hour ?: 12,
            initialMinute = initialTime?.minute ?: 0,
            is24Hour = is24Hour
        )

        AlertDialog(
            onDismissRequest = onDismiss,
            confirmButton = {
                TextButton(onClick = {
                    val time = LocalTime(timePickerState.hour, timePickerState.minute)
                    onTimeSelected(time)
                    onDismiss()
                }) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            },
            text = {
                TimePicker(state = timePickerState)
            }
        )
    }
}

@Composable
fun BlockwiseDurationPickerDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    onDurationSelected: (hours: Int, minutes: Int) -> Unit,
    initialHours: Int = 0,
    initialMinutes: Int = 0,
    maxHours: Int = 24
) {
    if (visible) {
        var hours by remember { mutableIntStateOf(initialHours) }
        var minutes by remember { mutableIntStateOf(initialMinutes) }

        AlertDialog(
            onDismissRequest = onDismiss,
            title = { Text("选择时长") },
            confirmButton = {
                TextButton(onClick = {
                    onDurationSelected(hours, minutes)
                    onDismiss()
                }) {
                    Text("确认")
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("取消")
                }
            },
            text = {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Hours Picker
                    NumberPicker(
                        value = hours,
                        onValueChange = { hours = it },
                        range = 0..maxHours
                    )
                    Text("时", modifier = Modifier.padding(horizontal = 8.dp))

                    // Minutes Picker
                    NumberPicker(
                        value = minutes,
                        onValueChange = { minutes = it },
                        range = 0..59
                    )
                    Text("分", modifier = Modifier.padding(start = 8.dp))
                }
            }
        )
    }
}
```

**验收标准**：
- [ ] 时间选择器可正常选择时间
- [ ] 时长选择器可选择小时和分钟
- [ ] 24小时制/12小时制切换正常
- [ ] 返回格式正确

---

#### T2.2.6 实现标签选择器组件

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.2.6 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T2.1.3 |

**任务描述**：
实现标签选择器组件，支持多选标签并显示标签颜色。

**执行步骤**：
1. 实现单个标签 Chip 组件
2. 实现标签选择器组件（FlowRow布局）
3. 支持单选和多选模式
4. 显示标签颜色标识

**关键代码**：
```kotlin
// TagSelector.kt
package com.blockwise.core.designsystem.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

data class TagItem(
    val id: Long,
    val name: String,
    val color: Color
)

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun BlockwiseTagSelector(
    tags: List<TagItem>,
    selectedTagIds: Set<Long>,
    onTagSelected: (Long) -> Unit,
    onTagDeselected: (Long) -> Unit,
    modifier: Modifier = Modifier,
    multiSelect: Boolean = true
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            val isSelected = selectedTagIds.contains(tag.id)
            BlockwiseTagChip(
                tag = tag,
                selected = isSelected,
                onClick = {
                    if (isSelected) {
                        onTagDeselected(tag.id)
                    } else {
                        if (!multiSelect) {
                            // Single select: deselect all first
                            selectedTagIds.forEach { onTagDeselected(it) }
                        }
                        onTagSelected(tag.id)
                    }
                }
            )
        }
    }
}

@Composable
fun BlockwiseTagChip(
    tag: TagItem,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        label = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(tag.color, CircleShape)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(tag.name)
            }
        }
    )
}
```

**验收标准**：
- [ ] 标签 Chip 样式正确
- [ ] 标签颜色标识显示
- [ ] 单选模式正常工作
- [ ] 多选模式正常工作
- [ ] FlowRow 自动换行

---

#### T2.2.7 实现进度条组件

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.2.7 |
| **优先级** | P1 |
| **预计耗时** | 1h |
| **依赖任务** | T2.1.3 |

**任务描述**：
实现进度条组件，包括线性进度条和圆形进度条。

**执行步骤**：
1. 实现带标签的线性进度条
2. 实现圆形进度条（用于目标进度）
3. 支持颜色自定义
4. 支持动画效果

**关键代码**：
```kotlin
// Progress.kt
package com.blockwise.core.designsystem.component

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BlockwiseLinearProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    label: String? = null,
    showPercentage: Boolean = true,
    color: Color = MaterialTheme.colorScheme.primary
) {
    Column(modifier = modifier) {
        if (label != null || showPercentage) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                if (label != null) {
                    Text(
                        text = label,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
                if (showPercentage) {
                    Text(
                        text = "${(progress * 100).toInt()}%",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        val animatedProgress by animateFloatAsState(
            targetValue = progress.coerceIn(0f, 1f),
            label = "progress"
        )

        LinearProgressIndicator(
            progress = { animatedProgress },
            modifier = Modifier.fillMaxWidth().height(8.dp),
            color = color,
            trackColor = color.copy(alpha = 0.2f)
        )
    }
}

@Composable
fun BlockwiseCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 80.dp,
    strokeWidth: Dp = 8.dp,
    color: Color = MaterialTheme.colorScheme.primary,
    content: @Composable () -> Unit = {}
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "progress"
    )

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            // Background arc
            drawArc(
                color = color.copy(alpha = 0.2f),
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
            // Progress arc
            drawArc(
                color = color,
                startAngle = -90f,
                sweepAngle = 360f * animatedProgress,
                useCenter = false,
                style = Stroke(width = strokeWidth.toPx(), cap = StrokeCap.Round)
            )
        }
        content()
    }
}
```

**验收标准**：
- [ ] 线性进度条样式正确
- [ ] 圆形进度条样式正确
- [ ] 进度动画平滑
- [ ] 百分比显示正确
- [ ] 颜色自定义生效

---

#### T2.2.8 实现空状态组件

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.2.8 |
| **优先级** | P1 |
| **预计耗时** | 1h |
| **依赖任务** | T2.1.3 |

**任务描述**：
实现空状态和错误状态显示组件。

**执行步骤**：
1. 实现空数据状态组件
2. 实现错误状态组件
3. 支持自定义图标和文案
4. 支持操作按钮

**关键代码**：
```kotlin
// EmptyState.kt
package com.blockwise.core.designsystem.component

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Inbox
import androidx.compose.material.icons.outlined.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun BlockwiseEmptyState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    icon: ImageVector = Icons.Outlined.Inbox,
    actionText: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center
        )

        if (description != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = description,
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

@Composable
fun BlockwiseErrorState(
    title: String,
    modifier: Modifier = Modifier,
    description: String? = null,
    retryText: String = "重试",
    onRetry: (() -> Unit)? = null
) {
    BlockwiseEmptyState(
        title = title,
        modifier = modifier,
        description = description,
        icon = Icons.Outlined.Warning,
        actionText = if (onRetry != null) retryText else null,
        onAction = onRetry
    )
}
```

**验收标准**：
- [ ] 空状态组件样式正确
- [ ] 错误状态组件样式正确
- [ ] 图标和文案可自定义
- [ ] 操作按钮功能正常

---

#### T2.2.9 实现加载状态组件

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.2.9 |
| **优先级** | P1 |
| **预计耗时** | 1.5h |
| **依赖任务** | T2.1.3 |

**任务描述**：
实现加载状态组件，包括全屏加载、内容加载和骨架屏。

**执行步骤**：
1. 实现全屏加载指示器
2. 实现内联加载指示器
3. 实现骨架屏组件
4. 实现 Shimmer 动画效果

**关键代码**：
```kotlin
// Loading.kt
package com.blockwise.core.designsystem.component

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun BlockwiseFullScreenLoading(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
fun BlockwiseInlineLoading(
    modifier: Modifier = Modifier,
    text: String? = null
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(24.dp),
            strokeWidth = 2.dp
        )
        if (text != null) {
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun BlockwiseShimmerBox(
    modifier: Modifier = Modifier
) {
    val shimmerColors = listOf(
        Color.LightGray.copy(alpha = 0.6f),
        Color.LightGray.copy(alpha = 0.2f),
        Color.LightGray.copy(alpha = 0.6f)
    )

    val transition = rememberInfiniteTransition(label = "shimmer")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmer"
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset(translateAnim - 500f, 0f),
        end = Offset(translateAnim, 0f)
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .background(brush)
    )
}

@Composable
fun BlockwiseSkeletonCard(
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Column(modifier = Modifier.padding(16.dp)) {
            BlockwiseShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.7f)
                    .height(20.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            BlockwiseShimmerBox(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            BlockwiseShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .height(16.dp)
            )
        }
    }
}
```

**验收标准**：
- [ ] 全屏加载指示器显示正确
- [ ] 内联加载指示器样式正确
- [ ] 骨架屏 Shimmer 动画流畅
- [ ] 骨架卡片布局合理

---

### 2.3 应用框架UI

#### T2.3.1 实现底部导航栏

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.3.1 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T1.3.8 |

**任务描述**：
实现应用主页面的底部 Tab 导航栏。

**执行步骤**：
1. 定义导航项枚举
2. 实现 NavigationBar 组件
3. 实现导航项图标和标签
4. 实现选中状态样式

**关键代码**：
```kotlin
// BottomNavigation.kt
package com.blockwise.core.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector

enum class BottomNavItem(
    val route: String,
    val label: String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector
) {
    Timeline(
        route = "timeline",
        label = "时间线",
        selectedIcon = Icons.Filled.ViewTimeline,
        unselectedIcon = Icons.Outlined.ViewTimeline
    ),
    TimeBlock(
        route = "timeblock",
        label = "时间块",
        selectedIcon = Icons.Filled.CalendarViewDay,
        unselectedIcon = Icons.Outlined.CalendarViewDay
    ),
    Statistics(
        route = "statistics",
        label = "统计",
        selectedIcon = Icons.Filled.BarChart,
        unselectedIcon = Icons.Outlined.BarChart
    ),
    Goals(
        route = "goals",
        label = "目标",
        selectedIcon = Icons.Filled.Flag,
        unselectedIcon = Icons.Outlined.Flag
    ),
    Settings(
        route = "settings",
        label = "设置",
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings
    )
}

@Composable
fun BlockwiseBottomNavigation(
    currentRoute: String,
    onNavigate: (BottomNavItem) -> Unit
) {
    NavigationBar {
        BottomNavItem.entries.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(item) },
                icon = {
                    Icon(
                        imageVector = if (selected) item.selectedIcon else item.unselectedIcon,
                        contentDescription = item.label
                    )
                },
                label = { Text(item.label) }
            )
        }
    }
}
```

**验收标准**：
- [ ] 底部导航栏显示5个Tab
- [ ] 选中/未选中图标切换正确
- [ ] 点击切换Tab功能正常
- [ ] 样式符合 Material3 规范

---

#### T2.3.2 实现顶部应用栏

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.3.2 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T2.1.3 |

**任务描述**：
实现顶部应用栏组件，支持标题、导航图标和操作按钮。

**执行步骤**：
1. 实现基础 TopAppBar
2. 实现带返回按钮的 TopAppBar
3. 实现带操作按钮的 TopAppBar
4. 支持大标题模式

**关键代码**：
```kotlin
// TopAppBar.kt
package com.blockwise.core.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockwiseTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = navigationIcon ?: {},
        actions = { actions() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockwiseTopAppBarWithBack(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回"
                )
            }
        },
        actions = { actions() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockwiseLargeTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
    scrollBehavior: TopAppBarScrollBehavior? = null
) {
    LargeTopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = navigationIcon ?: {},
        actions = { actions() },
        scrollBehavior = scrollBehavior
    )
}
```

**验收标准**：
- [ ] 基础 TopAppBar 样式正确
- [ ] 返回按钮功能正常
- [ ] 操作按钮显示正确
- [ ] 大标题模式样式正确

---

#### T2.3.3 实现导航框架

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.3.3 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T1.3.8 |

**任务描述**：
配置应用的 Navigation 框架，定义导航路由和导航图。

**执行步骤**：
1. 定义所有页面路由常量
2. 创建导航参数定义
3. 实现 NavHost 配置
4. 创建导航扩展函数

**关键代码**：
```kotlin
// Navigation.kt
package com.blockwise.app.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

// Route definitions
object Routes {
    // Main tabs
    const val Timeline = "timeline"
    const val TimeBlock = "timeblock"
    const val Statistics = "statistics"
    const val Goals = "goals"
    const val Settings = "settings"

    // Detail screens
    const val TimeEntryDetail = "timeentry/{entryId}"
    const val TimeEntryEdit = "timeentry/edit?entryId={entryId}"
    const val ActivityTypeManage = "activitytype"
    const val TagManage = "tag"
    const val GoalDetail = "goal/{goalId}"
    const val GoalEdit = "goal/edit?goalId={goalId}"
    const val StatisticsDetail = "statistics/detail/{tagId}"
    const val DataExport = "settings/export"
    const val DataImport = "settings/import"
    const val About = "settings/about"

    // Helper functions
    fun timeEntryDetail(entryId: Long) = "timeentry/$entryId"
    fun timeEntryEdit(entryId: Long? = null) =
        if (entryId != null) "timeentry/edit?entryId=$entryId" else "timeentry/edit"
    fun goalDetail(goalId: Long) = "goal/$goalId"
    fun goalEdit(goalId: Long? = null) =
        if (goalId != null) "goal/edit?goalId=$goalId" else "goal/edit"
    fun statisticsDetail(tagId: Long) = "statistics/detail/$tagId"
}

// Navigation arguments
object NavArgs {
    const val EntryId = "entryId"
    const val GoalId = "goalId"
    const val TagId = "tagId"
}

@Composable
fun BlockwiseNavHost(
    navController: NavHostController,
    startDestination: String = Routes.Timeline
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Main tabs
        composable(Routes.Timeline) {
            // TimelineScreen(navController)
        }
        composable(Routes.TimeBlock) {
            // TimeBlockScreen(navController)
        }
        composable(Routes.Statistics) {
            // StatisticsScreen(navController)
        }
        composable(Routes.Goals) {
            // GoalsScreen(navController)
        }
        composable(Routes.Settings) {
            // SettingsScreen(navController)
        }

        // Detail screens with arguments
        composable(
            route = Routes.TimeEntryDetail,
            arguments = listOf(
                navArgument(NavArgs.EntryId) { type = NavType.LongType }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong(NavArgs.EntryId) ?: return@composable
            // TimeEntryDetailScreen(entryId, navController)
        }

        composable(
            route = Routes.TimeEntryEdit,
            arguments = listOf(
                navArgument(NavArgs.EntryId) {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { backStackEntry ->
            val entryId = backStackEntry.arguments?.getLong(NavArgs.EntryId)
                ?.takeIf { it != -1L }
            // TimeEntryEditScreen(entryId, navController)
        }

        // Other screens...
    }
}

// Navigation extensions
fun NavHostController.navigateToTimeEntryDetail(entryId: Long) {
    navigate(Routes.timeEntryDetail(entryId))
}

fun NavHostController.navigateToTimeEntryEdit(entryId: Long? = null) {
    navigate(Routes.timeEntryEdit(entryId))
}

fun NavHostController.navigateToGoalDetail(goalId: Long) {
    navigate(Routes.goalDetail(goalId))
}

fun NavHostController.navigateToGoalEdit(goalId: Long? = null) {
    navigate(Routes.goalEdit(goalId))
}
```

**验收标准**：
- [ ] 所有路由常量定义完成
- [ ] NavHost 配置正确
- [ ] 带参数的路由可正常传递参数
- [ ] 导航扩展函数可用

---

#### T2.3.4 实现主Activity

| 属性 | 值 |
|------|-----|
| **任务ID** | T2.3.4 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T2.3.1, T2.3.2, T2.3.3 |

**任务描述**：
实现 MainActivity，整合导航框架、底部导航栏和主题。

**执行步骤**：
1. 配置 MainActivity 注解
2. 实现主界面 Scaffold 布局
3. 整合 NavHost 和 BottomNavigation
4. 处理系统栏适配

**关键代码**：
```kotlin
// MainActivity.kt
package com.blockwise.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.blockwise.app.navigation.BlockwiseNavHost
import com.blockwise.app.navigation.Routes
import com.blockwise.core.designsystem.component.BlockwiseBottomNavigation
import com.blockwise.core.designsystem.component.BottomNavItem
import com.blockwise.core.designsystem.theme.BlockwiseTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BlockwiseTheme {
                BlockwiseApp()
            }
        }
    }
}

@Composable
fun BlockwiseApp() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: Routes.Timeline

    // Determine if bottom nav should be shown
    val showBottomNav = remember(currentRoute) {
        currentRoute in listOf(
            Routes.Timeline,
            Routes.TimeBlock,
            Routes.Statistics,
            Routes.Goals,
            Routes.Settings
        )
    }

    Scaffold(
        bottomBar = {
            if (showBottomNav) {
                BlockwiseBottomNavigation(
                    currentRoute = currentRoute,
                    onNavigate = { item ->
                        navController.navigate(item.route) {
                            // Pop up to the start destination to avoid building up a large stack
                            popUpTo(Routes.Timeline) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        BlockwiseNavHost(
            navController = navController,
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }
}
```

**验收标准**：
- [ ] MainActivity 正确启动
- [ ] 底部导航栏显示正确
- [ ] Tab 切换功能正常
- [ ] 详情页面隐藏底部导航栏
- [ ] 系统栏适配正确（Edge to Edge）

---

## 3. 依赖关系图

```
阶段一完成
    │
    └── T1.3.1 集成Jetpack Compose
            │
            ├── T2.1.1 定义颜色体系 ──────┐
            │                             │
            ├── T2.1.2 定义字体排版 ──────┼── T2.1.3 实现明暗主题
            │                             │         │
            └── T2.1.4 定义间距系统       │         │
                                          │         │
                    ┌─────────────────────┘         │
                    │                               │
                    ▼                               ▼
            ┌───────────────────────────────────────────────┐
            │           通用UI组件 (T2.2.x)                  │
            │  T2.2.1 按钮  T2.2.2 卡片  T2.2.3 对话框       │
            │  T2.2.4 日期选择器  T2.2.5 时间选择器          │
            │  T2.2.6 标签选择器  T2.2.7 进度条             │
            │  T2.2.8 空状态  T2.2.9 加载状态               │
            └───────────────────────────────────────────────┘
                                    │
    T1.3.8 集成Compose Navigation   │
            │                       │
            ├── T2.3.1 底部导航栏 ──┤
            │                       │
            └── T2.3.3 导航框架 ────┼── T2.3.4 主Activity
                                    │
            T2.3.2 顶部应用栏 ──────┘
```

---

## 4. 验收标准清单

### 4.1 主题系统验收

| 验收项 | 要求 | 检查方式 |
|--------|------|----------|
| 颜色体系 | 主色、次色、语义色完整 | 代码审查 |
| 活动类型颜色 | 至少12种可区分颜色 | 视觉检查 |
| 字体排版 | Typography 配置完整 | 代码审查 |
| 明暗主题 | 切换正常，颜色适配 | 设备测试 |
| 动态颜色 | Android 12+ 设备生效 | 设备测试 |
| 间距系统 | 常量定义完整 | 代码审查 |

### 4.2 通用组件验收

| 组件 | 验收标准 |
|------|----------|
| 按钮组件 | 主/次/文字按钮样式正确，加载/禁用状态正常 |
| 卡片组件 | 基础/可点击/阴影卡片样式正确 |
| 对话框组件 | 确认/信息/选择对话框显示正常 |
| 日期选择器 | 单日/范围选择功能正常 |
| 时间选择器 | 时间/时长选择功能正常 |
| 标签选择器 | 单选/多选模式正常，颜色显示正确 |
| 进度条 | 线性/圆形进度显示正确，动画流畅 |
| 空状态 | 图标、文案、按钮显示正确 |
| 加载状态 | 全屏/内联加载显示正确，骨架屏动画流畅 |

### 4.3 应用框架验收

| 验收项 | 要求 | 检查方式 |
|--------|------|----------|
| 底部导航栏 | 5个Tab显示正确，切换正常 | 设备测试 |
| 顶部应用栏 | 标题、返回、操作按钮正常 | 设备测试 |
| 导航框架 | 路由定义完整，参数传递正确 | 代码审查 + 测试 |
| 主Activity | 启动正常，布局正确 | 设备测试 |
| 系统栏适配 | Edge to Edge 效果正确 | 设备测试 |

### 4.4 代码质量验收

| 验收项 | 要求 |
|--------|------|
| 组件命名 | 统一 `Blockwise` 前缀 |
| 代码位置 | 全部在 `core/designsystem` 模块 |
| 预览函数 | 每个组件有 `@Preview` 预览 |
| 文档注释 | 公共组件有 KDoc 注释 |

---

## 5. 组件目录结构

```
core/designsystem/
├── build.gradle.kts
└── src/main/kotlin/com/blockwise/core/designsystem/
    ├── theme/
    │   ├── Color.kt              # 颜色定义
    │   ├── Type.kt               # 字体排版
    │   ├── Theme.kt              # 主题配置
    │   └── Dimensions.kt         # 间距和尺寸
    │
    ├── component/
    │   ├── Button.kt             # 按钮组件
    │   ├── Card.kt               # 卡片组件
    │   ├── Dialog.kt             # 对话框组件
    │   ├── DatePicker.kt         # 日期选择器
    │   ├── TimePicker.kt         # 时间选择器
    │   ├── TagSelector.kt        # 标签选择器
    │   ├── Progress.kt           # 进度条组件
    │   ├── EmptyState.kt         # 空状态组件
    │   ├── Loading.kt            # 加载状态组件
    │   ├── TopAppBar.kt          # 顶部应用栏
    │   └── BottomNavigation.kt   # 底部导航栏
    │
    └── icon/
        └── BlockwiseIcons.kt     # 图标资源引用
```

---

## 6. 风险与注意事项

### 6.1 潜在风险

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| Material3 API 变化 | 组件可能过时 | 使用稳定版本，关注更新 |
| 深色模式颜色对比度 | 可读性问题 | 遵循 WCAG 标准，测试验证 |
| 组件过度封装 | 灵活性降低 | 提供足够的自定义参数 |

### 6.2 注意事项

1. **@OptIn 注解**：部分 Material3 API 需要 `@OptIn(ExperimentalMaterial3Api::class)`
2. **Compose 预览**：为每个组件提供 `@Preview` 函数便于开发调试
3. **状态提升**：组件应该是无状态的，状态由调用方管理
4. **可访问性**：确保 `contentDescription` 正确设置
5. **内存优化**：使用 `remember` 缓存计算结果

---

*文档版本: v1.0*
*阶段状态: 待开始*
