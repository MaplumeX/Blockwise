package com.maplume.blockwise.feature.statistics.domain.usecase

import com.maplume.blockwise.core.domain.model.DailyTrend
import com.maplume.blockwise.core.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject

/**
 * Use case for getting daily trend statistics.
 * Fills missing dates with zero values to ensure continuous data.
 */
class GetDailyTrendsUseCase @Inject constructor(
    private val repository: StatisticsRepository
) {
    /**
     * Get daily trends for a time range.
     * Missing dates are filled with zero values.
     * @param startTime Start of the time range.
     * @param endTime End of the time range.
     * @return Flow of daily trends with complete date range.
     */
    operator fun invoke(startTime: Instant, endTime: Instant): Flow<List<DailyTrend>> {
        return repository.getDailyTrends(startTime, endTime)
            .map { trends ->
                fillMissingDates(trends, startTime, endTime)
            }
    }

    /**
     * Fill missing dates with zero values.
     */
    private fun fillMissingDates(
        trends: List<DailyTrend>,
        startTime: Instant,
        endTime: Instant
    ): List<DailyTrend> {
        val timeZone = TimeZone.currentSystemDefault()
        val startDate = startTime.toLocalDateTime(timeZone).date
        val endDate = endTime.toLocalDateTime(timeZone).date

        val trendMap = trends.associateBy { it.date }
        val result = mutableListOf<DailyTrend>()

        var currentDate = startDate
        while (currentDate < endDate) {
            val trend = trendMap[currentDate] ?: DailyTrend(
                date = currentDate,
                totalMinutes = 0,
                entryCount = 0
            )
            result.add(trend)
            currentDate = currentDate.plus(1, DateTimeUnit.DAY)
        }

        return result
    }

    /**
     * Get daily trends for a specific tag.
     * @param tagId The tag ID to filter by.
     * @param startTime Start of the time range.
     * @param endTime End of the time range.
     * @return Flow of daily trends for the tag.
     */
    fun forTag(tagId: Long, startTime: Instant, endTime: Instant): Flow<List<DailyTrend>> {
        return repository.getDailyTrendsForTag(tagId, startTime, endTime)
            .map { trends ->
                fillMissingDates(trends, startTime, endTime)
            }
    }
}
