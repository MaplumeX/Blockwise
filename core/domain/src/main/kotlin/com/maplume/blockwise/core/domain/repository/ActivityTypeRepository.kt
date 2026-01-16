package com.maplume.blockwise.core.domain.repository

import com.maplume.blockwise.core.domain.model.ActivityType
import kotlinx.coroutines.flow.Flow

interface ActivityTypeRepository {
    fun getAllActive(): Flow<List<ActivityType>>

    fun getAll(): Flow<List<ActivityType>>

    suspend fun getById(id: Long): ActivityType?

    fun getByIdFlow(id: Long): Flow<ActivityType?>

    fun getRootLevel(): Flow<List<ActivityType>>

    fun getChildren(parentId: Long): Flow<List<ActivityType>>

    suspend fun isNameExists(name: String, excludeId: Long = 0): Boolean

    suspend fun create(
        name: String,
        colorHex: String,
        icon: String? = null,
        parentId: Long? = null
    ): Long

    suspend fun update(
        id: Long,
        name: String,
        colorHex: String,
        icon: String? = null,
        parentId: Long? = null
    )

    suspend fun updateDisplayOrder(id: Long, order: Int)

    suspend fun delete(id: Long)

    suspend fun restore(id: Long)
}

