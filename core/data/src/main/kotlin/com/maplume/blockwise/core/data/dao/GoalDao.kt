package com.maplume.blockwise.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.maplume.blockwise.core.data.entity.GoalEntity
import com.maplume.blockwise.core.data.entity.GoalWithTag
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(goal: GoalEntity): Long

    @Update
    suspend fun update(goal: GoalEntity)

    @Delete
    suspend fun delete(goal: GoalEntity)

    @Query("DELETE FROM goals WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("UPDATE goals SET is_active = :isActive, updated_at = :updatedAt WHERE id = :id")
    suspend fun setActive(id: Long, isActive: Boolean, updatedAt: Long)

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getById(id: Long): GoalEntity?

    @Transaction
    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getByIdWithTag(id: Long): GoalWithTag?

    @Transaction
    @Query("SELECT * FROM goals WHERE id = :id")
    fun getByIdWithTagFlow(id: Long): Flow<GoalWithTag?>

    @Transaction
    @Query("SELECT * FROM goals WHERE is_active = 1 ORDER BY id DESC")
    fun getActiveGoals(): Flow<List<GoalWithTag>>

    @Transaction
    @Query("SELECT * FROM goals ORDER BY is_active DESC, id DESC")
    fun getAllGoals(): Flow<List<GoalWithTag>>

    @Transaction
    @Query("SELECT * FROM goals WHERE tag_id = :tagId")
    fun getByTagId(tagId: Long): Flow<List<GoalWithTag>>

    @Transaction
    @Query("SELECT * FROM goals WHERE tag_id = :tagId AND is_active = 1 LIMIT 1")
    suspend fun getActiveByTagIdWithTag(tagId: Long): GoalWithTag?

    @Query("SELECT COUNT(*) FROM goals WHERE is_active = 1")
    suspend fun countActive(): Int
}

