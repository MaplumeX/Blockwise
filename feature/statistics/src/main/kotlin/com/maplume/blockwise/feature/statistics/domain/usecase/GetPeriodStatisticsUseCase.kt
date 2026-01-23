package com.maplume.blockwise.feature.statistics.domain.usecase

import com.maplume.blockwise.core.domain.model.PeriodStatistics
import com.maplume.blockwise.feature.statistics.domain.model.StatisticsPeriod
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetPeriodStatisticsUseCase @Inject constructor(
    private val getStatsByActivityType: GetStatsByActivityTypeUseCase,
    private val getStatsByTag: GetStatsByTagUseCase,
    private val getDailyTrends: GetDailyTrendsUseCase,
    private val getHourlyDistribution: GetHourlyDistributionUseCase,
    private val getStatisticsSummary: GetStatisticsSummaryUseCase
) {
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

    private fun getSummaryFlow(period: StatisticsPeriod) = flow {
        emit(getStatisticsSummary.withComparison(period))
    }
}
