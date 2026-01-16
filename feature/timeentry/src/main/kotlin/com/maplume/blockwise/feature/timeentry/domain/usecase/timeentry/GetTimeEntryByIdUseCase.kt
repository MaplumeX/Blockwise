package com.maplume.blockwise.feature.timeentry.domain.usecase.timeentry

import com.maplume.blockwise.core.domain.model.TimeEntry
import com.maplume.blockwise.core.domain.repository.TimeEntryRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for retrieving a single time entry by ID.
 */
class GetTimeEntryByIdUseCase @Inject constructor(
    private val repository: TimeEntryRepository
) {
    /**
     * Get time entry by ID (suspend version).
     * @param id The time entry ID.
     * @return The time entry or null if not found.
     */
    suspend operator fun invoke(id: Long): TimeEntry? {
        return repository.getById(id)
    }

    /**
     * Get time entry by ID as Flow for reactive updates.
     * @param id The time entry ID.
     * @return Flow of the time entry.
     */
    fun asFlow(id: Long): Flow<TimeEntry?> {
        return repository.getByIdFlow(id)
    }
}
