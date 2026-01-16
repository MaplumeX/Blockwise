package com.maplume.blockwise.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// =============================================================================
// Primary Colors - 深蓝色主色
// =============================================================================
val Primary = Color(0xFF135BEC)
val OnPrimary = Color(0xFFFFFFFF)

// =============================================================================
// Slate Colors - 中性灰色系（8个层级）
// =============================================================================
val Slate50 = Color(0xFFF8FAFC)
val Slate100 = Color(0xFFF1F5F9)
val Slate200 = Color(0xFFE2E8F0)
val Slate300 = Color(0xFFCBD5E1)
val Slate400 = Color(0xFF94A3B8)
val Slate500 = Color(0xFF64748B)
val Slate600 = Color(0xFF475569)
val Slate700 = Color(0xFF334155)
val Slate800 = Color(0xFF1E293B)
val Slate900 = Color(0xFF0F172A)

// =============================================================================
// Background Colors - 背景色
// =============================================================================
val BackgroundLight = Color(0xFFF6F6F8)
val BackgroundDark = Color(0xFF101622)

// =============================================================================
// Surface Colors - 表面色
// =============================================================================
val SurfaceLight = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF1E2430)
val SurfaceCardDark = Color(0xFF232D3D)

// =============================================================================
// Semantic Colors - 语义颜色
// =============================================================================
val Success = Color(0xFF4CAF50)
val Warning = Color(0xFFFFC107)
val Error = Color(0xFFEA4335)

// =============================================================================
// Activity Type Colors - 活动类型颜色（12种可区分颜色）
// =============================================================================
val ActivityColors = listOf(
    Color(0xFF4285F4), // Blue - 工作
    Color(0xFF34A853), // Green - 学习
    Color(0xFFFBBC04), // Yellow - 运动
    Color(0xFFEA4335), // Red - 休息
    Color(0xFF9C27B0), // Purple - 社交
    Color(0xFF00ACC1), // Cyan - 娱乐
    Color(0xFFFF7043), // Deep Orange - 健康
    Color(0xFF5C6BC0), // Indigo - 创意
    Color(0xFF66BB6A), // Light Green - 户外
    Color(0xFFFFCA28), // Amber - 阅读
    Color(0xFFEC407A), // Pink - 个人
    Color(0xFF78909C), // Blue Grey - 其他
)

/**
 * Get activity color by index with safe fallback
 */
fun getActivityColor(index: Int): Color {
    return ActivityColors.getOrElse(index % ActivityColors.size) { ActivityColors.first() }
}

// =============================================================================
// Chart Colors - 图表颜色（用于数据可视化）
// =============================================================================
val ChartColors = listOf(
    Color(0xFF135BEC), // Primary Blue
    Color(0xFF34A853), // Green
    Color(0xFFFBBC04), // Yellow
    Color(0xFFEA4335), // Red
    Color(0xFF9C27B0), // Purple
    Color(0xFF00ACC1), // Cyan
    Color(0xFFFF7043), // Orange
    Color(0xFF5C6BC0), // Indigo
)
