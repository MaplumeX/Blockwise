package com.maplume.blockwise.core.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.datetime.LocalDate

/**
 * Goal type enumeration.
 */
enum class GoalType {
    /** Minimum time goal - aim to spend at least this much time */
    MIN,
    /** Maximum time goal - aim to spend no more than this much time */
    MAX,
    /** Exact time goal - aim to spend exactly this much time */
    EXACT
}

/**
 * Goal period enumeration.
 */
enum class GoalPeriod {
    DAILY,
    WEEKLY,
    MONTHLY,
    CUSTOM
}

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
        Index(value = ["tag_id"])
    ]
)
data class GoalEntity(
    @PrimaryKey(autoGenerate = true)
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

    @ColumnInfo(name = "is_active")
    val isActive: Boolean = true
)
