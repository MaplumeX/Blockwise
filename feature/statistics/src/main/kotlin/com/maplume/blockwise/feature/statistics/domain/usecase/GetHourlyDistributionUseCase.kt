package com.maplume.blockwise.feature.statistics.domain.usecase

import com.maplume.blockwise.core.domain.model.HourlyPattern
import com.maplume.blockwise.core.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import javax.inject.Inject

/**
 * Use case for getting hourly distribution statistics.
 * Provides 24-hour distribution data for time usage analysis.
 */
class GetHourlyDistributionUseCase @Inject constructor(
    private val repository: StatisticsRepository
) {
    /**
     * Get hourly distribution for a time range.
     * Returns complete 24-hour data with missing hours filled as zero.
     * @param startTime Start of the time range.
     * @param endTime End of the time range.
     * @return Flow of hourly patterns for all 24 hours.
     */
    operator fun invoke(startTime: Instant, endTime: Instant): Flow<List<HourlyPattern>> {
        return repository.getHourlyDistribution(startTime, endTime)
            .map { patterns ->
                fillMissingHours(patterns)
            }
    }

    /**
     * Fill missing hours with zero values.
     * Ensures all 24 hours (0-23) are present in the result.
     */
    private fun fillMissingHours(patterns: List<HourlyPattern>): List<HourlyPattern> {
        val patternMap = patterns.associateBy { it.hour }
        return (0..23).map { hour ->
            patternMap[hour] ?: HourlyPattern(hour = hour, totalMinutes = 0)
        }
    }
}
