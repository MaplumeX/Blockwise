package com.maplume.blockwise.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.maplume.blockwise.core.data.entity.TagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tags: List<TagEntity>): List<Long>

    @Update
    suspend fun update(tag: TagEntity)

    @Delete
    suspend fun delete(tag: TagEntity)

    @Query("DELETE FROM tags WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE tags SET is_archived = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun archive(id: Long, updatedAt: Long)

    @Query("UPDATE tags SET is_archived = 0, updated_at = :updatedAt WHERE id = :id")
    suspend fun unarchive(id: Long, updatedAt: Long)

    @Query("SELECT * FROM tags WHERE is_archived = 0 ORDER BY name ASC")
    fun getAllActive(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags ORDER BY name ASC")
    fun getAll(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getById(id: Long): TagEntity?

    @Query("SELECT * FROM tags WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<TagEntity?>

    @Query("SELECT * FROM tags WHERE id IN (:ids)")
    suspend fun getByIds(ids: List<Long>): List<TagEntity>

    @Query("SELECT * FROM tags WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): TagEntity?

    @Query("SELECT EXISTS(SELECT 1 FROM tags WHERE name = :name AND id != :excludeId)")
    suspend fun isNameExists(name: String, excludeId: Long = 0): Boolean

    @Query("SELECT COUNT(*) FROM tags WHERE is_archived = 0")
    suspend fun countActive(): Int
}

