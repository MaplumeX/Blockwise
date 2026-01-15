package com.maplume.blockwise.core.domain.repository

import com.maplume.blockwise.core.domain.model.Tag
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for tag operations.
 */
interface TagRepository {

    /**
     * Get all active (non-archived) tags.
     */
    fun getAllActiveTags(): Flow<List<Tag>>

    /**
     * Get all tags including archived ones.
     */
    fun getAllTags(): Flow<List<Tag>>

    /**
     * Get a tag by its ID.
     */
    suspend fun getTagById(id: Long): Tag?

    /**
     * Get tags associated with a time entry.
     */
    fun getTagsForTimeEntry(entryId: Long): Flow<List<Tag>>

    /**
     * Insert a new tag.
     * @return The ID of the inserted tag
     */
    suspend fun insertTag(tag: Tag): Long

    /**
     * Update an existing tag.
     */
    suspend fun updateTag(tag: Tag)

    /**
     * Archive a tag (soft delete).
     */
    suspend fun archiveTag(id: Long)

    /**
     * Add a tag to a time entry.
     */
    suspend fun addTagToTimeEntry(entryId: Long, tagId: Long)

    /**
     * Remove a tag from a time entry.
     */
    suspend fun removeTagFromTimeEntry(entryId: Long, tagId: Long)

    /**
     * Replace all tags for a time entry.
     */
    suspend fun setTagsForTimeEntry(entryId: Long, tagIds: List<Long>)
}
