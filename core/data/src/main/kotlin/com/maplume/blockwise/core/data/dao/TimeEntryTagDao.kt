package com.maplume.blockwise.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.maplume.blockwise.core.data.entity.TimeEntryTagEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TimeEntryTagDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(relation: TimeEntryTagEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(relations: List<TimeEntryTagEntity>)

    @Delete
    suspend fun delete(relation: TimeEntryTagEntity)

    @Query("DELETE FROM time_entry_tags WHERE entry_id = :entryId")
    suspend fun deleteByEntryId(entryId: Long)

    @Query("DELETE FROM time_entry_tags WHERE tag_id = :tagId")
    suspend fun deleteByTagId(tagId: Long)

    @Query("DELETE FROM time_entry_tags WHERE entry_id = :entryId AND tag_id = :tagId")
    suspend fun deleteRelation(entryId: Long, tagId: Long)

    @Query("SELECT tag_id FROM time_entry_tags WHERE entry_id = :entryId")
    suspend fun getTagIdsByEntryId(entryId: Long): List<Long>

    @Query("SELECT entry_id FROM time_entry_tags WHERE tag_id = :tagId")
    suspend fun getEntryIdsByTagId(tagId: Long): List<Long>

    @Query("SELECT * FROM time_entry_tags WHERE entry_id = :entryId")
    fun getByEntryIdFlow(entryId: Long): Flow<List<TimeEntryTagEntity>>

    @Transaction
    suspend fun replaceTagsForEntry(entryId: Long, tagIds: List<Long>) {
        deleteByEntryId(entryId)
        if (tagIds.isEmpty()) return
        val relations = tagIds.map { TimeEntryTagEntity(entryId, it) }
        insertAll(relations)
    }
}

