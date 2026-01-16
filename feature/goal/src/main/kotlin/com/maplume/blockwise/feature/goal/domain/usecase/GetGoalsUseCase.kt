package com.maplume.blockwise.feature.goal.domain.usecase

import com.maplume.blockwise.core.domain.model.Goal
import com.maplume.blockwise.core.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for querying goals.
 * Provides various query methods for different use cases.
 */
class GetGoalsUseCase @Inject constructor(
    private val repository: GoalRepository
) {
    /**
     * Get all active goals as a Flow.
     */
    fun getActiveGoals(): Flow<List<Goal>> {
        return repository.getActiveGoals()
    }

    /**
     * Get all goals (including archived) as a Flow.
     */
    fun getAllGoals(): Flow<List<Goal>> {
        return repository.getAllGoals()
    }

    /**
     * Get a specific goal by ID.
     */
    suspend fun getById(id: Long): Goal? {
        return repository.getById(id)
    }

    /**
     * Get a specific goal by ID as a Flow.
     */
    fun getByIdFlow(id: Long): Flow<Goal?> {
        return repository.getByIdFlow(id)
    }

    /**
     * Get all goals for a specific tag.
     */
    fun getByTagId(tagId: Long): Flow<List<Goal>> {
        return repository.getByTagId(tagId)
    }

    /**
     * Get the active goal for a specific tag.
     */
    suspend fun getActiveByTagId(tagId: Long): Goal? {
        return repository.getActiveByTagId(tagId)
    }

    /**
     * Get count of active goals.
     */
    suspend fun countActive(): Int {
        return repository.countActive()
    }
}
