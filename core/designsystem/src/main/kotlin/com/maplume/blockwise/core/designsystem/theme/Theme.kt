package com.maplume.blockwise.core.designsystem.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * 浅色主题配色方案 - 现代极简风格
 */
private val LightColorScheme = lightColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = Primary.copy(alpha = 0.1f),
    onPrimaryContainer = Primary,

    secondary = Slate600,
    onSecondary = Slate50,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate900,

    tertiary = Slate500,
    onTertiary = Slate50,
    tertiaryContainer = Slate200,
    onTertiaryContainer = Slate800,

    surface = SurfaceLight,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,

    background = BackgroundLight,
    onBackground = Slate900,

    error = Error,
    onError = Slate50,
    errorContainer = Error.copy(alpha = 0.1f),
    onErrorContainer = Error,

    outline = Slate300,
    outlineVariant = Slate200
)

/**
 * 深色主题配色方案 - 现代极简风格
 */
private val DarkColorScheme = darkColorScheme(
    primary = Primary,
    onPrimary = OnPrimary,
    primaryContainer = Primary.copy(alpha = 0.2f),
    onPrimaryContainer = Primary,

    secondary = Slate400,
    onSecondary = Slate900,
    secondaryContainer = Slate800,
    onSecondaryContainer = Slate100,

    tertiary = Slate500,
    onTertiary = Slate900,
    tertiaryContainer = Slate700,
    onTertiaryContainer = Slate200,

    surface = SurfaceDark,
    onSurface = Slate100,
    surfaceVariant = SurfaceCardDark,
    onSurfaceVariant = Slate400,

    background = BackgroundDark,
    onBackground = Slate100,

    error = Error,
    onError = Slate50,
    errorContainer = Error.copy(alpha = 0.2f),
    onErrorContainer = Error,

    outline = Slate700,
    outlineVariant = Slate800
)

/**
 * Blockwise 应用主题 - 现代极简风格
 *
 * 移除了 Material Design 3 的动态色彩支持，使用固定的深蓝色主色和 Slate 灰色系
 *
 * @param darkTheme 是否使用深色主题
 * @param content 可组合内容
 */
@Composable
fun BlockwiseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
