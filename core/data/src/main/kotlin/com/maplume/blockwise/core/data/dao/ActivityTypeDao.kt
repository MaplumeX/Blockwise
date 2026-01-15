package com.maplume.blockwise.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.maplume.blockwise.core.data.entity.ActivityTypeEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for activity types.
 */
@Dao
interface ActivityTypeDao {

    @Query("SELECT * FROM activity_types WHERE is_archived = 0 ORDER BY display_order, name")
    fun getAllActiveActivityTypes(): Flow<List<ActivityTypeEntity>>

    @Query("SELECT * FROM activity_types ORDER BY display_order, name")
    fun getAllActivityTypes(): Flow<List<ActivityTypeEntity>>

    @Query("SELECT * FROM activity_types WHERE id = :id")
    suspend fun getActivityTypeById(id: Long): ActivityTypeEntity?

    @Query("SELECT * FROM activity_types WHERE parent_id = :parentId AND is_archived = 0 ORDER BY display_order")
    fun getChildActivityTypes(parentId: Long): Flow<List<ActivityTypeEntity>>

    @Query("SELECT * FROM activity_types WHERE parent_id IS NULL AND is_archived = 0 ORDER BY display_order")
    fun getRootActivityTypes(): Flow<List<ActivityTypeEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityType(activityType: ActivityTypeEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertActivityTypes(activityTypes: List<ActivityTypeEntity>)

    @Update
    suspend fun updateActivityType(activityType: ActivityTypeEntity)

    @Delete
    suspend fun deleteActivityType(activityType: ActivityTypeEntity)

    @Query("UPDATE activity_types SET is_archived = 1 WHERE id = :id")
    suspend fun archiveActivityType(id: Long)

    @Query("UPDATE activity_types SET is_archived = 0 WHERE id = :id")
    suspend fun unarchiveActivityType(id: Long)

    @Query("SELECT COUNT(*) FROM activity_types WHERE is_archived = 0")
    suspend fun getActiveActivityTypeCount(): Int
}
