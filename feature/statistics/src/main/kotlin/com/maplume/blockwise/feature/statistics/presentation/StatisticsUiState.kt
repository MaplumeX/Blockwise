package com.maplume.blockwise.feature.statistics.presentation

import com.maplume.blockwise.core.domain.model.PeriodStatistics
import com.maplume.blockwise.feature.statistics.domain.model.StatisticsPeriod

/**
 * UI state for the statistics screen.
 */
data class StatisticsUiState(
    val currentPeriod: StatisticsPeriod = StatisticsPeriod.Week.current(),
    val periodType: PeriodType = PeriodType.WEEK,
    val statistics: PeriodStatistics? = null,
    val isLoading: Boolean = true,
    val error: String? = null
) {
    /**
     * Whether the current period is the latest (cannot navigate to next).
     */
    val isCurrentPeriod: Boolean
        get() = currentPeriod.isCurrent()

    /**
     * Formatted period label for display.
     */
    val periodLabel: String
        get() = currentPeriod.label
}

/**
 * Period type for tab selection.
 */
enum class PeriodType {
    DAY,
    WEEK,
    MONTH,
    YEAR;

    /**
     * Get display name for the period type.
     */
    val displayName: String
        get() = when (this) {
            DAY -> "日"
            WEEK -> "周"
            MONTH -> "月"
            YEAR -> "年"
        }

    /**
     * Create a StatisticsPeriod for the current time.
     */
    fun createCurrentPeriod(): StatisticsPeriod = when (this) {
        DAY -> StatisticsPeriod.Day.today()
        WEEK -> StatisticsPeriod.Week.current()
        MONTH -> StatisticsPeriod.Month.current()
        YEAR -> StatisticsPeriod.Year.current()
    }
}

/**
 * Events that can be triggered from the statistics screen.
 */
sealed class StatisticsEvent {
    data class SelectPeriodType(val type: PeriodType) : StatisticsEvent()
    data object NavigateToPrevious : StatisticsEvent()
    data object NavigateToNext : StatisticsEvent()
    data object Refresh : StatisticsEvent()
    data class NavigateToActivityDetail(val activityId: Long) : StatisticsEvent()
    data class NavigateToTagDetail(val tagId: Long) : StatisticsEvent()
}
