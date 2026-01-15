package com.maplume.blockwise.core.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.maplume.blockwise.core.data.entity.TagEntity
import com.maplume.blockwise.core.data.entity.TimeEntryTagCrossRef
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for tags.
 */
@Dao
interface TagDao {

    @Query("SELECT * FROM tags WHERE is_archived = 0 ORDER BY name")
    fun getAllActiveTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags ORDER BY name")
    fun getAllTags(): Flow<List<TagEntity>>

    @Query("SELECT * FROM tags WHERE id = :id")
    suspend fun getTagById(id: Long): TagEntity?

    @Query("""
        SELECT t.* FROM tags t
        INNER JOIN time_entry_tags tet ON t.id = tet.tag_id
        WHERE tet.entry_id = :entryId
    """)
    fun getTagsForTimeEntry(entryId: Long): Flow<List<TagEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTag(tag: TagEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTags(tags: List<TagEntity>)

    @Update
    suspend fun updateTag(tag: TagEntity)

    @Delete
    suspend fun deleteTag(tag: TagEntity)

    @Query("UPDATE tags SET is_archived = 1 WHERE id = :id")
    suspend fun archiveTag(id: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTimeEntryTagCrossRef(crossRef: TimeEntryTagCrossRef)

    @Delete
    suspend fun deleteTimeEntryTagCrossRef(crossRef: TimeEntryTagCrossRef)

    @Query("DELETE FROM time_entry_tags WHERE entry_id = :entryId")
    suspend fun deleteAllTagsForTimeEntry(entryId: Long)
}
