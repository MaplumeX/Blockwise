package com.maplume.blockwise.feature.timeentry.domain.usecase.timeentry

import com.maplume.blockwise.core.domain.repository.TimeEntryRepository
import javax.inject.Inject

/**
 * Use case for deleting a time entry.
 */
class DeleteTimeEntryUseCase @Inject constructor(
    private val repository: TimeEntryRepository
) {
    /**
     * Delete a time entry.
     * @param id The time entry ID to delete.
     * @return Result indicating success or failure with error message.
     */
    suspend operator fun invoke(id: Long): Result<Unit> {
        // Validate time entry exists
        val existing = repository.getById(id)
            ?: return Result.failure(IllegalArgumentException("时间记录不存在"))

        return try {
            repository.delete(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
