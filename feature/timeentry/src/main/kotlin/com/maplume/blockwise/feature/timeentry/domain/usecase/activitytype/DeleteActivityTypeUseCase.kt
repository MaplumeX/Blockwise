package com.maplume.blockwise.feature.timeentry.domain.usecase.activitytype

import com.maplume.blockwise.core.domain.repository.ActivityTypeRepository
import javax.inject.Inject

/**
 * Use case for deleting (archiving) an activity type.
 * Uses soft delete to preserve historical data.
 */
class DeleteActivityTypeUseCase @Inject constructor(
    private val repository: ActivityTypeRepository
) {
    /**
     * Delete (archive) an activity type.
     * @param id The activity type ID to delete.
     * @return Result indicating success or failure with error message.
     */
    suspend operator fun invoke(id: Long): Result<Unit> {
        // Validate activity type exists
        val existing = repository.getById(id)
            ?: return Result.failure(IllegalArgumentException("活动类型不存在"))

        if (existing.isArchived) {
            return Result.failure(IllegalArgumentException("该活动类型已被删除"))
        }

        return try {
            repository.delete(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Restore a previously deleted (archived) activity type.
     * @param id The activity type ID to restore.
     * @return Result indicating success or failure with error message.
     */
    suspend fun restore(id: Long): Result<Unit> {
        // Validate activity type exists
        val existing = repository.getById(id)
            ?: return Result.failure(IllegalArgumentException("活动类型不存在"))

        if (!existing.isArchived) {
            return Result.failure(IllegalArgumentException("该活动类型未被删除"))
        }

        // If has parent, check parent is not archived
        val parentId = existing.parentId
        if (parentId != null) {
            val parent = repository.getById(parentId)
            if (parent?.isArchived == true) {
                return Result.failure(IllegalArgumentException("父级活动类型已被删除，请先恢复父级"))
            }
        }

        return try {
            repository.restore(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
