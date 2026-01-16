package com.maplume.blockwise.core.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Embedded
import androidx.room.Index
import androidx.room.Junction
import androidx.room.PrimaryKey
import androidx.room.Relation
import kotlinx.datetime.Instant

/**
 * Database entity for time entries.
 */
@Entity(
    tableName = "time_entries",
    foreignKeys = [
        ForeignKey(
            entity = ActivityTypeEntity::class,
            parentColumns = ["id"],
            childColumns = ["activity_id"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["activity_id"]),
        Index(value = ["start_time"]),
        Index(value = ["end_time"]),
        Index(value = ["start_time", "activity_id"])
    ]
)
data class TimeEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "activity_id")
    val activityId: Long,

    @ColumnInfo(name = "start_time")
    val startTime: Instant,

    @ColumnInfo(name = "end_time")
    val endTime: Instant,

    @ColumnInfo(name = "duration_minutes")
    val durationMinutes: Int,

    @ColumnInfo(name = "note")
    val note: String? = null,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant
)

data class TimeEntryWithDetails(
    @Embedded
    val entry: TimeEntryEntity,

    @Relation(
        parentColumn = "activity_id",
        entityColumn = "id"
    )
    val activity: ActivityTypeEntity,

    @Relation(
        parentColumn = "id",
        entityColumn = "id",
        associateBy = Junction(
            value = TimeEntryTagEntity::class,
            parentColumn = "entry_id",
            entityColumn = "tag_id"
        )
    )
    val tags: List<TagEntity>
)
