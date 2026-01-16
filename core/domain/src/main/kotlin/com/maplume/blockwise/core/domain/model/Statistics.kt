package com.maplume.blockwise.core.domain.model

import kotlinx.datetime.LocalDate

data class StatisticsSummary(
    val totalMinutes: Int,
    val entryCount: Int,
    val previousPeriodMinutes: Int? = null
) {
    val formattedTotal: String
        get() {
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            return when {
                hours > 0 && minutes > 0 -> "${hours}小时${minutes}分钟"
                hours > 0 -> "${hours}小时"
                else -> "${minutes}分钟"
            }
        }

    val changePercentage: Int?
        get() = previousPeriodMinutes?.let { previous ->
            if (previous > 0) ((totalMinutes - previous) * 100 / previous) else null
        }

    val isIncrease: Boolean?
        get() = previousPeriodMinutes?.let { totalMinutes > it }
}

data class CategoryStatistics(
    val id: Long,
    val name: String,
    val colorHex: String,
    val totalMinutes: Int,
    val entryCount: Int,
    val percentage: Float
) {
    val formattedDuration: String
        get() {
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            return when {
                hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
                hours > 0 -> "${hours}h"
                else -> "${minutes}m"
            }
        }
}

data class DailyTrend(
    val date: LocalDate,
    val totalMinutes: Int,
    val entryCount: Int
)

data class HourlyPattern(
    val hour: Int,
    val totalMinutes: Int
) {
    val formattedHour: String get() = "${hour}:00"
}

data class PeriodStatistics(
    val summary: StatisticsSummary,
    val byActivity: List<CategoryStatistics>,
    val byTag: List<CategoryStatistics>,
    val dailyTrends: List<DailyTrend>,
    val hourlyPattern: List<HourlyPattern>
)

