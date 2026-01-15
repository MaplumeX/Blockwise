package com.maplume.blockwise.core.domain.repository

import com.maplume.blockwise.core.domain.model.Goal
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for goal operations.
 */
interface GoalRepository {

    /**
     * Get all active goals.
     */
    fun getAllActiveGoals(): Flow<List<Goal>>

    /**
     * Get all goals including inactive ones.
     */
    fun getAllGoals(): Flow<List<Goal>>

    /**
     * Get a goal by its ID.
     */
    suspend fun getGoalById(id: Long): Goal?

    /**
     * Get goals for a specific tag.
     */
    fun getGoalsForTag(tagId: Long): Flow<List<Goal>>

    /**
     * Insert a new goal.
     * @return The ID of the inserted goal
     */
    suspend fun insertGoal(goal: Goal): Long

    /**
     * Update an existing goal.
     */
    suspend fun updateGoal(goal: Goal)

    /**
     * Delete a goal.
     */
    suspend fun deleteGoal(goal: Goal)

    /**
     * Deactivate a goal.
     */
    suspend fun deactivateGoal(id: Long)

    /**
     * Activate a goal.
     */
    suspend fun activateGoal(id: Long)
}
