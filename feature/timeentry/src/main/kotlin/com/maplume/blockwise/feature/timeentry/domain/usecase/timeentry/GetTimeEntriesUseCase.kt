package com.maplume.blockwise.feature.timeentry.domain.usecase.timeentry

import com.maplume.blockwise.core.domain.model.TimeEntry
import com.maplume.blockwise.core.domain.repository.TimeEntryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import javax.inject.Inject

/**
 * Use case for retrieving time entries.
 * Supports various query methods.
 */
class GetTimeEntriesUseCase @Inject constructor(
    private val repository: TimeEntryRepository
) {
    /**
     * Get time entries by time range.
     * @param startTime Start of the time range.
     * @param endTime End of the time range.
     * @return Flow of time entry list.
     */
    operator fun invoke(startTime: Instant, endTime: Instant): Flow<List<TimeEntry>> {
        return repository.getOverlapping(startTime, endTime)
    }

    /**
     * Get time entries for a specific day.
     * @param date The date to query.
     * @return Flow of time entry list.
     */
    fun getByDay(date: LocalDate): Flow<List<TimeEntry>> {
        return repository.getByDay(date)
    }

    /**
     * Get recent time entries with pagination.
     * @param limit Maximum number of entries to return.
     * @param offset Number of entries to skip.
     * @return Flow of time entry list.
     */
    fun getRecent(limit: Int = 20, offset: Int = 0): Flow<List<TimeEntry>> {
        return repository.getRecent(limit, offset)
    }

    /**
     * Get the latest time entry.
     * @return The most recent time entry or null.
     */
    suspend fun getLatest(): TimeEntry? {
        return repository.getLatest()
    }
}
