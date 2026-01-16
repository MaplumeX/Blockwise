package com.maplume.blockwise.feature.statistics.domain.usecase

import com.maplume.blockwise.core.domain.model.StatisticsSummary
import com.maplume.blockwise.core.domain.repository.StatisticsRepository
import com.maplume.blockwise.feature.statistics.domain.model.StatisticsPeriod
import kotlinx.datetime.Instant
import javax.inject.Inject

/**
 * Use case for getting statistics summary.
 * Calculates total duration, entry count, and period-over-period comparison.
 */
class GetStatisticsSummaryUseCase @Inject constructor(
    private val repository: StatisticsRepository
) {
    /**
     * Get statistics summary for a time range.
     * @param startTime Start of the time range.
     * @param endTime End of the time range.
     * @return Statistics summary with totals.
     */
    suspend operator fun invoke(startTime: Instant, endTime: Instant): StatisticsSummary {
        return repository.getTotalStats(startTime, endTime)
    }

    /**
     * Get statistics summary with period-over-period comparison.
     * @param period The current period to analyze.
     * @return Statistics summary with comparison to previous period.
     */
    suspend fun withComparison(period: StatisticsPeriod): StatisticsSummary {
        val currentStats = repository.getTotalStats(period.startTime, period.endTime)
        val previousPeriod = period.previous()
        val previousStats = repository.getTotalStats(previousPeriod.startTime, previousPeriod.endTime)

        return currentStats.copy(
            previousPeriodMinutes = previousStats.totalMinutes
        )
    }
}
