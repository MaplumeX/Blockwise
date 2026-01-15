package com.maplume.blockwise.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// Primary colors
val Primary = Color(0xFF2196F3)
val PrimaryDark = Color(0xFF1976D2)
val PrimaryLight = Color(0xFFBBDEFB)

// Secondary colors
val Secondary = Color(0xFF4CAF50)
val SecondaryDark = Color(0xFF388E3C)
val SecondaryLight = Color(0xFFC8E6C9)

// Surface colors
val Surface = Color(0xFFFFFFFF)
val SurfaceDark = Color(0xFF121212)
val SurfaceVariant = Color(0xFFF5F5F5)
val SurfaceVariantDark = Color(0xFF1E1E1E)

// Background colors
val Background = Color(0xFFFAFAFA)
val BackgroundDark = Color(0xFF121212)

// Text colors
val OnPrimary = Color(0xFFFFFFFF)
val OnSecondary = Color(0xFFFFFFFF)
val OnSurface = Color(0xFF212121)
val OnSurfaceDark = Color(0xFFE0E0E0)
val OnBackground = Color(0xFF212121)
val OnBackgroundDark = Color(0xFFE0E0E0)

// Error colors
val Error = Color(0xFFD32F2F)
val ErrorLight = Color(0xFFEF5350)
val OnError = Color(0xFFFFFFFF)

// Activity type colors (for time blocks)
object ActivityColors {
    val Work = Color(0xFF2196F3)        // Blue
    val Study = Color(0xFF9C27B0)       // Purple
    val Exercise = Color(0xFF4CAF50)    // Green
    val Rest = Color(0xFFFF9800)        // Orange
    val Social = Color(0xFFE91E63)      // Pink
    val Entertainment = Color(0xFF00BCD4) // Cyan
    val Health = Color(0xFFFF5722)      // Deep Orange
    val Other = Color(0xFF607D8B)       // Blue Grey

    val all = listOf(
        Work, Study, Exercise, Rest,
        Social, Entertainment, Health, Other
    )
}

// Chart colors
object ChartColors {
    val palette = listOf(
        Color(0xFF2196F3),
        Color(0xFF4CAF50),
        Color(0xFFFF9800),
        Color(0xFF9C27B0),
        Color(0xFFE91E63),
        Color(0xFF00BCD4),
        Color(0xFFFF5722),
        Color(0xFF607D8B)
    )
}
