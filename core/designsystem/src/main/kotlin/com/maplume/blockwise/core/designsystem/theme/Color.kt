package com.maplume.blockwise.core.designsystem.theme

import androidx.compose.ui.graphics.Color

// =============================================================================
// Light Theme Colors
// =============================================================================

// Primary Colors - Material3 Purple
val Primary = Color(0xFF6750A4)
val OnPrimary = Color(0xFFFFFFFF)
val PrimaryContainer = Color(0xFFEADDFF)
val OnPrimaryContainer = Color(0xFF21005D)

// Secondary Colors
val Secondary = Color(0xFF625B71)
val OnSecondary = Color(0xFFFFFFFF)
val SecondaryContainer = Color(0xFFE8DEF8)
val OnSecondaryContainer = Color(0xFF1D192B)

// Tertiary Colors
val Tertiary = Color(0xFF7D5260)
val OnTertiary = Color(0xFFFFFFFF)
val TertiaryContainer = Color(0xFFFFD8E4)
val OnTertiaryContainer = Color(0xFF31111D)

// Surface Colors
val Surface = Color(0xFFFFFBFE)
val OnSurface = Color(0xFF1C1B1F)
val SurfaceVariant = Color(0xFFE7E0EC)
val OnSurfaceVariant = Color(0xFF49454F)

// Background Colors
val Background = Color(0xFFFFFBFE)
val OnBackground = Color(0xFF1C1B1F)

// Outline Colors
val Outline = Color(0xFF79747E)
val OutlineVariant = Color(0xFFCAC4D0)

// =============================================================================
// Dark Theme Colors
// =============================================================================

val PrimaryDark = Color(0xFFD0BCFF)
val OnPrimaryDark = Color(0xFF381E72)
val PrimaryContainerDark = Color(0xFF4F378B)
val OnPrimaryContainerDark = Color(0xFFEADDFF)

val SecondaryDark = Color(0xFFCCC2DC)
val OnSecondaryDark = Color(0xFF332D41)
val SecondaryContainerDark = Color(0xFF4A4458)
val OnSecondaryContainerDark = Color(0xFFE8DEF8)

val TertiaryDark = Color(0xFFEFB8C8)
val OnTertiaryDark = Color(0xFF492532)
val TertiaryContainerDark = Color(0xFF633B48)
val OnTertiaryContainerDark = Color(0xFFFFD8E4)

val SurfaceDark = Color(0xFF1C1B1F)
val OnSurfaceDark = Color(0xFFE6E1E5)
val SurfaceVariantDark = Color(0xFF49454F)
val OnSurfaceVariantDark = Color(0xFFCAC4D0)

val BackgroundDark = Color(0xFF1C1B1F)
val OnBackgroundDark = Color(0xFFE6E1E5)

val OutlineDark = Color(0xFF938F99)
val OutlineVariantDark = Color(0xFF49454F)

// =============================================================================
// Semantic Colors
// =============================================================================

val Success = Color(0xFF4CAF50)
val OnSuccess = Color(0xFFFFFFFF)
val SuccessContainer = Color(0xFFC8E6C9)
val OnSuccessContainer = Color(0xFF1B5E20)

val Warning = Color(0xFFFFC107)
val OnWarning = Color(0xFF000000)
val WarningContainer = Color(0xFFFFF8E1)
val OnWarningContainer = Color(0xFFFF6F00)

val Error = Color(0xFFB3261E)
val OnError = Color(0xFFFFFFFF)
val ErrorContainer = Color(0xFFF9DEDC)
val OnErrorContainer = Color(0xFF410E0B)

// =============================================================================
// Activity Type Colors (12 distinguishable colors for time blocks)
// =============================================================================

val ActivityColors = listOf(
    Color(0xFF4285F4),  // Blue - Work
    Color(0xFF34A853),  // Green - Study
    Color(0xFFFBBC04),  // Yellow - Exercise
    Color(0xFFEA4335),  // Red - Rest
    Color(0xFF9C27B0),  // Purple - Social
    Color(0xFF00ACC1),  // Cyan - Entertainment
    Color(0xFFFF7043),  // Deep Orange - Health
    Color(0xFF5C6BC0),  // Indigo - Creative
    Color(0xFF66BB6A),  // Light Green - Outdoor
    Color(0xFFFFCA28),  // Amber - Learning
    Color(0xFFEC407A),  // Pink - Personal
    Color(0xFF78909C),  // Blue Grey - Other
)

/**
 * Get activity color by index with safe fallback
 */
fun getActivityColor(index: Int): Color {
    return ActivityColors.getOrElse(index % ActivityColors.size) { ActivityColors.first() }
}

// =============================================================================
// Chart Colors
// =============================================================================

val ChartColors = listOf(
    Color(0xFF4285F4),  // Blue
    Color(0xFF34A853),  // Green
    Color(0xFFFBBC04),  // Yellow
    Color(0xFFEA4335),  // Red
    Color(0xFF9C27B0),  // Purple
    Color(0xFF00ACC1),  // Cyan
    Color(0xFFFF7043),  // Deep Orange
    Color(0xFF5C6BC0),  // Indigo
)
