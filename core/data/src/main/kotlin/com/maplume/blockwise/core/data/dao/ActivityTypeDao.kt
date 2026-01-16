package com.maplume.blockwise.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.maplume.blockwise.core.data.entity.ActivityTypeEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ActivityTypeDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(activityType: ActivityTypeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(activityTypes: List<ActivityTypeEntity>): List<Long>

    @Update
    suspend fun update(activityType: ActivityTypeEntity)

    @Delete
    suspend fun delete(activityType: ActivityTypeEntity)

    @Query("DELETE FROM activity_types WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE activity_types SET is_archived = 1, updated_at = :updatedAt WHERE id = :id")
    suspend fun archive(id: Long, updatedAt: Long)

    @Query("UPDATE activity_types SET is_archived = 0, updated_at = :updatedAt WHERE id = :id")
    suspend fun unarchive(id: Long, updatedAt: Long)

    @Query("SELECT * FROM activity_types WHERE is_archived = 0 ORDER BY display_order ASC")
    fun getAllActive(): Flow<List<ActivityTypeEntity>>

    @Query("SELECT * FROM activity_types ORDER BY display_order ASC")
    fun getAll(): Flow<List<ActivityTypeEntity>>

    @Query("SELECT * FROM activity_types WHERE id = :id")
    suspend fun getById(id: Long): ActivityTypeEntity?

    @Query("SELECT * FROM activity_types WHERE id = :id")
    fun getByIdFlow(id: Long): Flow<ActivityTypeEntity?>

    @Query("SELECT * FROM activity_types WHERE parent_id IS NULL AND is_archived = 0 ORDER BY display_order ASC")
    fun getRootLevel(): Flow<List<ActivityTypeEntity>>

    @Query("SELECT * FROM activity_types WHERE parent_id = :parentId AND is_archived = 0 ORDER BY display_order ASC")
    fun getChildren(parentId: Long): Flow<List<ActivityTypeEntity>>

    @Query("SELECT * FROM activity_types WHERE name = :name AND is_archived = 0 LIMIT 1")
    suspend fun getByName(name: String): ActivityTypeEntity?

    @Query("UPDATE activity_types SET display_order = :order, updated_at = :updatedAt WHERE id = :id")
    suspend fun updateDisplayOrder(id: Long, order: Int, updatedAt: Long)

    @Query("SELECT COUNT(*) FROM activity_types WHERE is_archived = 0")
    suspend fun countActive(): Int
}

