package com.maplume.blockwise.feature.timeentry.domain.usecase.timeentry

import com.maplume.blockwise.core.domain.model.TimeEntry
import com.maplume.blockwise.core.domain.repository.TimeEntryRepository
import kotlinx.coroutines.flow.first
import kotlinx.datetime.Instant
import javax.inject.Inject

class CheckTimeOverlapUseCase @Inject constructor(
    private val repository: TimeEntryRepository
) {
    suspend operator fun invoke(
        startTime: Instant,
        endTime: Instant,
        excludeId: Long = 0
    ): OverlapResult {
        // Validate time range
        if (startTime >= endTime) {
            return OverlapResult.InvalidTimeRange
        }

        // Check for overlapping entries
        val hasOverlap = repository.hasOverlapping(startTime, endTime, excludeId)

        return if (hasOverlap) {
            OverlapResult.HasOverlap
        } else {
            OverlapResult.NoOverlap
        }
    }

    suspend fun getOverlappingEntries(
        startTime: Instant,
        endTime: Instant,
        excludeId: Long = 0
    ): List<TimeEntry> {
        return repository.getOverlapping(startTime, endTime)
            .first()
            .filterNot { it.id == excludeId }
    }
}

/**
 * Result of overlap check.
 */
sealed class OverlapResult {
    /**
     * No overlap detected.
     */
    data object NoOverlap : OverlapResult()

    /**
     * Overlap detected with existing entries.
     */
    data object HasOverlap : OverlapResult()

    /**
     * Invalid time range (start >= end).
     */
    data object InvalidTimeRange : OverlapResult()
}
