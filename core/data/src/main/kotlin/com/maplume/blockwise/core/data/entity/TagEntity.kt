package com.maplume.blockwise.core.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Database entity for tags.
 */
@Entity(tableName = "tags")
data class TagEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "color_hex")
    val colorHex: String,

    @ColumnInfo(name = "is_archived")
    val isArchived: Boolean = false
)

/**
 * Cross-reference entity for TimeEntry and Tag many-to-many relationship.
 */
@Entity(
    tableName = "time_entry_tags",
    primaryKeys = ["entry_id", "tag_id"]
)
data class TimeEntryTagCrossRef(
    @ColumnInfo(name = "entry_id")
    val entryId: Long,

    @ColumnInfo(name = "tag_id")
    val tagId: Long
)
