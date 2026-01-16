package com.maplume.blockwise.feature.timeentry.data.repository

import com.maplume.blockwise.core.data.dao.ActivityTypeDao
import com.maplume.blockwise.core.data.mapper.toDomain
import com.maplume.blockwise.core.data.mapper.toEntity
import com.maplume.blockwise.core.domain.model.ActivityType
import com.maplume.blockwise.core.domain.repository.ActivityTypeRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import javax.inject.Inject

class ActivityTypeRepositoryImpl @Inject constructor(
    private val dao: ActivityTypeDao
) : ActivityTypeRepository {

    override fun getAllActive(): Flow<List<ActivityType>> {
        return dao.getAllActive().map { list -> list.map { it.toDomain() } }
    }

    override fun getAll(): Flow<List<ActivityType>> {
        return dao.getAll().map { list -> list.map { it.toDomain() } }
    }

    override suspend fun getById(id: Long): ActivityType? {
        return dao.getById(id)?.toDomain()
    }

    override fun getByIdFlow(id: Long): Flow<ActivityType?> {
        return dao.getByIdFlow(id).map { it?.toDomain() }
    }

    override fun getRootLevel(): Flow<List<ActivityType>> {
        return dao.getRootLevel().map { list -> list.map { it.toDomain() } }
    }

    override fun getChildren(parentId: Long): Flow<List<ActivityType>> {
        return dao.getChildren(parentId).map { list -> list.map { it.toDomain() } }
    }

    override suspend fun isNameExists(name: String, excludeId: Long): Boolean = withContext(Dispatchers.IO) {
        val existing = dao.getByName(name) ?: return@withContext false
        existing.id != excludeId
    }

    override suspend fun create(
        name: String,
        colorHex: String,
        icon: String?,
        parentId: Long?
    ): Long = withContext(Dispatchers.IO) {
        val now = Clock.System.now()
        val entity = ActivityType(
            name = name,
            colorHex = colorHex,
            icon = icon,
            parentId = parentId
        ).toEntity(createdAt = now, updatedAt = now)
        dao.insert(entity)
    }

    override suspend fun update(
        id: Long,
        name: String,
        colorHex: String,
        icon: String?,
        parentId: Long?
    ) = withContext(Dispatchers.IO) {
        val existing = dao.getById(id) ?: return@withContext
        val now = Clock.System.now()
        val updated = existing.copy(
            name = name,
            colorHex = colorHex,
            icon = icon,
            parentId = parentId,
            updatedAt = now
        )
        dao.update(updated)
    }

    override suspend fun updateDisplayOrder(id: Long, order: Int) = withContext(Dispatchers.IO) {
        dao.updateDisplayOrder(id, order, Clock.System.now().toEpochMilliseconds())
    }

    override suspend fun delete(id: Long) = withContext(Dispatchers.IO) {
        dao.archive(id, Clock.System.now().toEpochMilliseconds())
    }

    override suspend fun restore(id: Long) = withContext(Dispatchers.IO) {
        dao.unarchive(id, Clock.System.now().toEpochMilliseconds())
    }
}

