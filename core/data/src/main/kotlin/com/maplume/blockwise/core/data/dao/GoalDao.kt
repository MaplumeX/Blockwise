package com.maplume.blockwise.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.maplume.blockwise.core.data.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for goals.
 */
@Dao
interface GoalDao {

    @Query("SELECT * FROM goals WHERE is_active = 1 ORDER BY id")
    fun getAllActiveGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals ORDER BY id")
    fun getAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Long): GoalEntity?

    @Query("SELECT * FROM goals WHERE tag_id = :tagId AND is_active = 1")
    fun getGoalsForTag(tagId: Long): Flow<List<GoalEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGoal(goal: GoalEntity): Long

    @Update
    suspend fun updateGoal(goal: GoalEntity)

    @Delete
    suspend fun deleteGoal(goal: GoalEntity)

    @Query("UPDATE goals SET is_active = 0 WHERE id = :id")
    suspend fun deactivateGoal(id: Long)

    @Query("UPDATE goals SET is_active = 1 WHERE id = :id")
    suspend fun activateGoal(id: Long)
}
