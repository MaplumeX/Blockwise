package com.maplume.blockwise.core.domain.repository

import com.maplume.blockwise.core.domain.model.Tag
import kotlinx.coroutines.flow.Flow

interface TagRepository {
    fun getAllActive(): Flow<List<Tag>>

    fun getAll(): Flow<List<Tag>>

    suspend fun getById(id: Long): Tag?

    fun getByIdFlow(id: Long): Flow<Tag?>

    suspend fun getByIds(ids: List<Long>): List<Tag>

    suspend fun isNameExists(name: String, excludeId: Long = 0): Boolean

    suspend fun create(name: String, colorHex: String): Long

    suspend fun update(id: Long, name: String, colorHex: String)

    suspend fun delete(id: Long)

    suspend fun restore(id: Long)
}

