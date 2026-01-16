package com.maplume.blockwise.feature.statistics.domain.usecase

import com.maplume.blockwise.core.domain.model.CategoryStatistics
import com.maplume.blockwise.core.domain.repository.StatisticsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import javax.inject.Inject

/**
 * Use case for getting statistics grouped by activity type.
 * Calculates percentage distribution and sorts by duration.
 */
class GetStatsByActivityTypeUseCase @Inject constructor(
    private val repository: StatisticsRepository
) {
    /**
     * Get activity type statistics for a time range.
     * @param startTime Start of the time range.
     * @param endTime End of the time range.
     * @return Flow of category statistics with calculated percentages.
     */
    operator fun invoke(startTime: Instant, endTime: Instant): Flow<List<CategoryStatistics>> {
        return repository.getStatsByActivityType(startTime, endTime)
            .map { stats ->
                val total = stats.sumOf { it.totalMinutes }
                stats.map { stat ->
                    stat.copy(
                        percentage = if (total > 0) {
                            (stat.totalMinutes * 100f / total)
                        } else {
                            0f
                        }
                    )
                }.sortedByDescending { it.totalMinutes }
            }
    }
}
