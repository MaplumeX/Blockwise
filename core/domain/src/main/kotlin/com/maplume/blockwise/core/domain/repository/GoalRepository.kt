package com.maplume.blockwise.core.domain.repository

import com.maplume.blockwise.core.domain.model.Goal
import com.maplume.blockwise.core.domain.model.GoalInput
import kotlinx.coroutines.flow.Flow

interface GoalRepository {
    suspend fun create(input: GoalInput): Long

    suspend fun update(id: Long, input: GoalInput)

    suspend fun delete(id: Long)

    suspend fun getById(id: Long): Goal?

    fun getByIdFlow(id: Long): Flow<Goal?>

    fun getActiveGoals(): Flow<List<Goal>>

    fun getAllGoals(): Flow<List<Goal>>

    fun getByTagId(tagId: Long): Flow<List<Goal>>

    suspend fun getActiveByTagId(tagId: Long): Goal?

    suspend fun setActive(id: Long, isActive: Boolean)

    suspend fun countActive(): Int
}

