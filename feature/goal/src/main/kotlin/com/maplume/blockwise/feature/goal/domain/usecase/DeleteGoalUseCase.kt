package com.maplume.blockwise.feature.goal.domain.usecase

import com.maplume.blockwise.core.domain.repository.GoalRepository
import javax.inject.Inject

/**
 * Use case for deleting or archiving a goal.
 * Supports both soft delete (archive) and hard delete.
 */
class DeleteGoalUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    /**
     * Archive a goal (soft delete).
     * The goal will be marked as inactive but data is preserved.
     */
    suspend fun archive(id: Long): Result<Unit> {
        return try {
            val goal = repository.getById(id)
                ?: return Result.failure(IllegalArgumentException("目标不存在"))

            if (!goal.isActive) {
                return Result.failure(IllegalArgumentException("目标已归档"))
            }

            repository.setActive(id, false)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Restore an archived goal.
     */
    suspend fun restore(id: Long): Result<Unit> {
        return try {
            val goal = repository.getById(id)
                ?: return Result.failure(IllegalArgumentException("目标不存在"))

            if (goal.isActive) {
                return Result.failure(IllegalArgumentException("目标未归档"))
            }

            // Check if tag already has active goal
            val existingActive = repository.getActiveByTagId(goal.tagId)
            if (existingActive != null) {
                return Result.failure(IllegalArgumentException("该标签已有活动目标，无法恢复"))
            }

            repository.setActive(id, true)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Permanently delete a goal (hard delete).
     */
    suspend fun delete(id: Long): Result<Unit> {
        return try {
            repository.delete(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
