package com.maplume.blockwise.feature.statistics.presentation.chart

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

/**
 * Chart color palette for statistics visualization.
 */
object ChartColors {
    val Primary = Color(0xFF135BEC)
    val Secondary = Color(0xFF34A853)
    val Tertiary = Color(0xFFFBBC04)
    val Quaternary = Color(0xFFEA4335)
    val Quinary = Color(0xFF9C27B0)

    // Time period colors
    val Morning = Color(0xFF4CAF50)   // 6-11: Green
    val Afternoon = Color(0xFFFFC107) // 12-17: Yellow
    val Evening = Color(0xFFFF9800)   // 18-21: Orange
    val Night = Color(0xFF9E9E9E)     // Other: Gray

    val palette = listOf(Primary, Secondary, Tertiary, Quaternary, Quinary)

    /**
     * Get color for hour of day.
     */
    fun getHourColor(hour: Int): Color {
        return when (hour) {
            in 6..11 -> Morning
            in 12..17 -> Afternoon
            in 18..21 -> Evening
            else -> Night
        }
    }
}

/**
 * Default chart dimensions.
 */
object ChartDimensions {
    val DefaultHeight = 200.dp
    val BarWidth = 8.dp
    val BarSpacing = 4.dp
    val LineThickness = 2.dp
    val PointSize = 4.dp
}
