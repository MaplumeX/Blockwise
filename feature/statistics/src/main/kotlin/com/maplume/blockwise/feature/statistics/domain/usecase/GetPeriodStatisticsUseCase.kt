package com.maplume.blockwise.feature.statistics.domain.usecase

import com.maplume.blockwise.core.domain.model.PeriodStatistics
import com.maplume.blockwise.feature.statistics.domain.model.StatisticsPeriod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for getting comprehensive period statistics.
 * Aggregates all statistics dimensions for a given period.
 */
class GetPeriodStatisticsUseCase @Inject constructor(
    private val getStatsByActivityType: GetStatsByActivityTypeUseCase,
    private val getStatsByTag: GetStatsByTagUseCase,
    private val getDailyTrends: GetDailyTrendsUseCase,
    private val getHourlyDistribution: GetHourlyDistributionUseCase,
    private val getStatisticsSummary: GetStatisticsSummaryUseCase
) {
    /**
     * Get comprehensive statistics for a period.
     * Combines all statistics dimensions into a single data class.
     * @param period The period to analyze.
     * @return Flow of period statistics.
     */
    operator fun invoke(period: StatisticsPeriod): Flow<PeriodStatistics> {
        val startTime = period.startTime
        val endTime = period.endTime

        return combine(
            getStatsByActivityType(startTime, endTime),
            getStatsByTag(startTime, endTime),
            getDailyTrends(startTime, endTime),
            getHourlyDistribution(startTime, endTime),
            getSummaryFlow(period)
        ) { byActivity, byTag, dailyTrends, hourlyPattern, summary ->
            PeriodStatistics(
                summary = summary,
                byActivity = byActivity,
                byTag = byTag,
                dailyTrends = dailyTrends,
                hourlyPattern = hourlyPattern
            )
        }
    }

    /**
     * Convert suspend function to Flow for combine operation.
     */
    private fun getSummaryFlow(period: StatisticsPeriod) = flow {
        emit(getStatisticsSummary.withComparison(period))
    }
}
