package com.maplume.blockwise.feature.timeentry.data.repository

import com.maplume.blockwise.core.data.dao.TagDao
import com.maplume.blockwise.core.data.mapper.toDomain
import com.maplume.blockwise.core.data.mapper.toEntity
import com.maplume.blockwise.core.domain.model.Tag
import com.maplume.blockwise.core.domain.repository.TagRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import javax.inject.Inject

class TagRepositoryImpl @Inject constructor(
    private val dao: TagDao
) : TagRepository {

    override fun getAllActive(): Flow<List<Tag>> {
        return dao.getAllActive().map { list -> list.map { it.toDomain() } }
    }

    override fun getAll(): Flow<List<Tag>> {
        return dao.getAll().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getById(id: Long): Tag? {
        return dao.getById(id)?.toDomain()
    }

    override fun getByIdFlow(id: Long): Flow<Tag?> {
        return dao.getByIdFlow(id).map { it?.toDomain() }
    }

    override suspend fun getByIds(ids: List<Long>): List<Tag> {
        return dao.getByIds(ids).map { it.toDomain() }
    }

    override suspend fun isNameExists(name: String, excludeId: Long): Boolean {
        return dao.isNameExists(name, excludeId)
    }

    override suspend fun create(name: String, colorHex: String): Long = withContext(Dispatchers.IO) {
        val now = Clock.System.now()
        val entity = Tag(
            name = name,
            colorHex = colorHex
        ).toEntity(createdAt = now, updatedAt = now)
        dao.insert(entity)
    }

    override suspend fun update(id: Long, name: String, colorHex: String) = withContext(Dispatchers.IO) {
        val existing = dao.getById(id) ?: return@withContext
        val updated = existing.copy(
            name = name,
            colorHex = colorHex,
            updatedAt = Clock.System.now()
        )
        dao.update(updated)
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        dao.archive(id, Clock.System.now().toEpochMilliseconds())
    }

    override suspend fun restore(id: Long) = withContext(Dispatchers.IO) {
        dao.unarchive(id, Clock.System.now().toEpochMilliseconds())
    }
}

