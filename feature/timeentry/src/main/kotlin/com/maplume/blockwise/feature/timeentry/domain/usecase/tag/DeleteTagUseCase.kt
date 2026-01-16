package com.maplume.blockwise.feature.timeentry.domain.usecase.tag

import com.maplume.blockwise.core.domain.repository.TagRepository
import javax.inject.Inject

/**
 * Use case for deleting (archiving) a tag.
 * Uses soft delete to preserve historical data.
 */
class DeleteTagUseCase @Inject constructor(
    private val repository: TagRepository
) {
    /**
     * Delete (archive) a tag.
     * @param id The tag ID to delete.
     * @return Result indicating success or failure with error message.
     */
    suspend operator fun invoke(id: Long): Result<Unit> {
        // Validate tag exists
        val existing = repository.getById(id)
            ?: return Result.failure(IllegalArgumentException("标签不存在"))

        if (existing.isArchived) {
            return Result.failure(IllegalArgumentException("该标签已被删除"))
        }

        return try {
            repository.delete(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Restore a previously deleted (archived) tag.
     * @param id The tag ID to restore.
     * @return Result indicating success or failure with error message.
     */
    suspend fun restore(id: Long): Result<Unit> {
        // Validate tag exists
        val existing = repository.getById(id)
            ?: return Result.failure(IllegalArgumentException("标签不存在"))

        if (!existing.isArchived) {
            return Result.failure(IllegalArgumentException("该标签未被删除"))
        }

        return try {
            repository.restore(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
