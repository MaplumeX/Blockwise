package com.maplume.blockwise.core.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.maplume.blockwise.core.domain.model.GoalPeriod
import com.maplume.blockwise.core.domain.model.GoalType
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

/**
 * Database entity for goals.
 */
@Entity(
    tableName = "goals",
    foreignKeys = [
        ForeignKey(
            entity = TagEntity::class,
            parentColumns = ["id"],
            childColumns = ["tag_id"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["tag_id"]),
        Index(value = ["is_active"])
    ]
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "id")
    val id: Long = 0,

    @ColumnInfo(name = "tag_id")
    val tagId: Long,

    @ColumnInfo(name = "target_minutes")
    val targetMinutes: Int,

    @ColumnInfo(name = "goal_type")
    val goalType: GoalType,

    @ColumnInfo(name = "period")
    val period: GoalPeriod,

    @ColumnInfo(name = "start_date")
    val startDate: LocalDate? = null,

    @ColumnInfo(name = "end_date")
    val endDate: LocalDate? = null,

    @ColumnInfo(name = "is_active", defaultValue = "1")
    val isActive: Boolean = true,

    @ColumnInfo(name = "created_at")
    val createdAt: Instant,

    @ColumnInfo(name = "updated_at")
    val updatedAt: Instant
)
