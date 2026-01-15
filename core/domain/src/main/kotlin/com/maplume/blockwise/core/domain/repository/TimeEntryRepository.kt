package com.maplume.blockwise.core.domain.repository

import com.maplume.blockwise.core.domain.model.TimeEntry
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.Instant

/**
 * Repository interface for time entry operations.
 * Implementations should handle data access and persistence.
 */
interface TimeEntryRepository {

    /**
     * Get all time entries as a Flow.
     */
    fun getAllTimeEntries(): Flow<List<TimeEntry>>

    /**
     * Get a time entry by its ID.
     */
    suspend fun getTimeEntryById(id: Long): TimeEntry?

    /**
     * Get time entries within a date range.
     */
    fun getTimeEntriesInRange(startTime: Instant, endTime: Instant): Flow<List<TimeEntry>>

    /**
     * Get time entries for a specific activity type.
     */
    fun getTimeEntriesByActivityId(activityId: Long): Flow<List<TimeEntry>>

    /**
     * Insert a new time entry.
     * @return The ID of the inserted entry
     */
    suspend fun insertTimeEntry(entry: TimeEntry): Long

    /**
     * Update an existing time entry.
     */
    suspend fun updateTimeEntry(entry: TimeEntry)

    /**
     * Delete a time entry.
     */
    suspend fun deleteTimeEntry(entry: TimeEntry)

    /**
     * Delete a time entry by ID.
     */
    suspend fun deleteTimeEntryById(id: Long)

    /**
     * Get the total count of time entries.
     */
    suspend fun getTimeEntryCount(): Int

    /**
     * Get total duration in minutes within a date range.
     */
    suspend fun getTotalDurationInRange(startTime: Instant, endTime: Instant): Int
}
