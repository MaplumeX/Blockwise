package com.maplume.blockwise.feature.timeentry.domain.usecase.timeentry

import com.maplume.blockwise.core.domain.model.TimeEntry
import com.maplume.blockwise.core.domain.repository.TimeEntryRepository
import kotlinx.datetime.Instant
import javax.inject.Inject

/**
 * Use case for checking time entry overlaps.
 * Detects if a new or updated time entry would overlap with existing entries.
 */
class CheckTimeOverlapUseCase @Inject constructor(
    private val repository: TimeEntryRepository
) {
    /**
     * Check if the given time range overlaps with existing entries.
     * @param startTime Start of the time range.
     * @param endTime End of the time range.
     * @param excludeId Optional ID to exclude (for edit mode).
     * @return OverlapResult indicating whether there's an overlap and details.
     */
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

    /**
     * Get detailed overlap information.
     * @param startTime Start of the time range.
     * @param endTime End of the time range.
     * @param excludeId Optional ID to exclude (for edit mode).
     * @return List of overlapping entries.
     */
    suspend fun getOverlappingEntries(
        startTime: Instant,
        endTime: Instant,
        excludeId: Long = 0
    ): List<TimeEntry> {
        // This would require a new repository method to get actual overlapping entries
        // For now, we just check if there's an overlap
        return emptyList()
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
