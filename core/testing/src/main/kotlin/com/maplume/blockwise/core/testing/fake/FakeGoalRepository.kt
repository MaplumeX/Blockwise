package com.maplume.blockwise.core.testing.fake

import com.maplume.blockwise.core.domain.model.Goal
import com.maplume.blockwise.core.domain.model.GoalInput
import com.maplume.blockwise.core.domain.repository.GoalRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map

/**
 * Fake implementation of GoalRepository for testing.
 * Stores goals in memory and provides controllable behavior.
 */
class FakeGoalRepository : GoalRepository {

    private val goals = MutableStateFlow<List<Goal>>(emptyList())
    private var nextId = 1L

    /**
     * Function to convert GoalInput to Goal.
     * Must be set before using create/update methods.
     */
    var inputToGoalMapper: ((GoalInput, Long) -> Goal)? = null

    /**
     * Control whether operations should fail.
     */
    var shouldFail = false
    var failureException: Exception = RuntimeException("Test failure")

    /**
     * Get all goals for inspection.
     */
    fun getAllGoalsList(): List<Goal> = goals.value

    /**
     * Set goals directly for testing.
     */
    fun setGoals(newGoals: List<Goal>) {
        goals.value = newGoals
        nextId = (newGoals.maxOfOrNull { it.id } ?: 0) + 1
    }

    /**
     * Clear all goals.
     */
    fun clear() {
        goals.value = emptyList()
        nextId = 1L
    }

    override suspend fun create(input: GoalInput): Long {
        if (shouldFail) throw failureException

        val mapper = inputToGoalMapper
            ?: throw IllegalStateException("inputToGoalMapper must be set before creating goals")

        val id = nextId++
        val goal = mapper(input, id)
        goals.value = goals.value + goal
        return id
    }

    override suspend fun update(id: Long, input: GoalInput) {
        if (shouldFail) throw failureException

        val mapper = inputToGoalMapper
            ?: throw IllegalStateException("inputToGoalMapper must be set before updating goals")

        goals.value = goals.value.map { goal ->
            if (goal.id == id) mapper(input, id) else goal
        }
    }

    override suspend fun delete(id: Long) {
        if (shouldFail) throw failureException
        goals.value = goals.value.filter { it.id != id }
    }

    override suspend fun getById(id: Long): Goal? {
        if (shouldFail) throw failureException
        return goals.value.find { it.id == id }
    }

    override fun getByIdFlow(id: Long): Flow<Goal?> {
        return goals.map { list -> list.find { it.id == id } }
    }

    override fun getActiveGoals(): Flow<List<Goal>> {
        return goals.map { list -> list.filter { it.isActive } }
    }

    override fun getAllGoals(): Flow<List<Goal>> {
        return goals
    }

    override fun getByTagId(tagId: Long): Flow<List<Goal>> {
        return goals.map { list -> list.filter { it.tagId == tagId } }
    }

    override suspend fun getActiveByTagId(tagId: Long): Goal? {
        if (shouldFail) throw failureException
        return goals.value.find { it.tagId == tagId && it.isActive }
    }

    override suspend fun setActive(id: Long, isActive: Boolean) {
        if (shouldFail) throw failureException
        goals.value = goals.value.map { goal ->
            if (goal.id == id) goal.copy(isActive = isActive) else goal
        }
    }

    override suspend fun countActive(): Int {
        if (shouldFail) throw failureException
        return goals.value.count { it.isActive }
    }
}
