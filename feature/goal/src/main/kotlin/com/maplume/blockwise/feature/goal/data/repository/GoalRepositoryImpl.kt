package com.maplume.blockwise.feature.goal.data.repository

import com.maplume.blockwise.core.data.dao.GoalDao
import com.maplume.blockwise.core.data.mapper.toDomain
import com.maplume.blockwise.core.data.mapper.toEntity
import com.maplume.blockwise.core.domain.model.Goal
import com.maplume.blockwise.core.domain.model.GoalInput
import com.maplume.blockwise.core.domain.repository.GoalRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import javax.inject.Inject

class GoalRepositoryImpl @Inject constructor(
    private val dao: GoalDao
) : GoalRepository {

    override suspend fun create(input: GoalInput): Long = withContext(Dispatchers.IO) {
        val now = Clock.System.now()
        dao.insert(input.toEntity(createdAt = now, updatedAt = now))
    }

    override suspend fun update(id: Long, input: GoalInput) = withContext(Dispatchers.IO) {
        val existing = dao.getById(id) ?: return@withContext
        val updated = input.toEntity(
            id = id,
            createdAt = existing.createdAt,
            updatedAt = Clock.System.now()
        )
        dao.update(updated)
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        dao.deleteById(id)
    }

    override suspend fun getById(id: Long): Goal? {
        return dao.getByIdWithTag(id)?.toDomain()
    }

    override fun getByIdFlow(id: Long): Flow<Goal?> {
        return dao.getByIdWithTagFlow(id).map { it?.toDomain() }
    }

    override fun getActiveGoals(): Flow<List<Goal>> {
        return dao.getActiveGoals().map { list -> list.map { it.toDomain() } }
    }

    override fun getAllGoals(): Flow<List<Goal>> {
        return dao.getAllGoals().map { list -> list.map { it.toDomain() } }
    }

    override fun getByTagId(tagId: Long): Flow<List<Goal>> {
        return dao.getByTagId(tagId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getActiveByTagId(tagId: Long): Goal? {
        return dao.getActiveByTagIdWithTag(tagId)?.toDomain()
    }

    override suspend fun setActive(id: Long, isActive: Boolean) = withContext(Dispatchers.IO) {
        dao.setActive(id, isActive, Clock.System.now().toEpochMilliseconds())
    }

    override suspend fun countActive(): Int {
        return dao.countActive()
    }
}

