package com.maplume.blockwise.core.domain.model

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
 * Domain model representing a time goal.
 */
data class Goal(
    val id: Long = 0,
    val tag: Tag,
    val targetMinutes: Int,
    val goalType: GoalType,
    val period: GoalPeriod,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val isActive: Boolean = true
) {
    val tagId: Long get() = tag.id

    val formattedTarget: String
        get() {
            val hours = targetMinutes / 60
            val minutes = targetMinutes % 60
            return when {
                hours > 0 && minutes > 0 -> "${hours}小时${minutes}分钟"
                hours > 0 -> "${hours}小时"
                else -> "${minutes}分钟"
            }
        }

    val periodLabel: String
        get() = when (period) {
            GoalPeriod.DAILY -> "每天"
            GoalPeriod.WEEKLY -> "每周"
            GoalPeriod.MONTHLY -> "每月"
            GoalPeriod.CUSTOM -> "自定义"
        }

    val goalTypeLabel: String
        get() = when (goalType) {
            GoalType.MIN -> "至少"
            GoalType.MAX -> "最多"
            GoalType.EXACT -> "精确"
        }
}

data class GoalInput(
    val tagId: Long,
    val targetMinutes: Int,
    val goalType: GoalType,
    val period: GoalPeriod,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val isActive: Boolean = true
)

data class GoalProgress(
    val goal: Goal,
    val currentMinutes: Int,
    val targetMinutes: Int
) {
    val progress: Float
        get() = if (targetMinutes > 0) {
            (currentMinutes.toFloat() / targetMinutes).coerceIn(0f, 1f)
        } else {
            0f
        }

    val progressPercentage: Int get() = (progress * 100).toInt()

    val isCompleted: Boolean
        get() = when (goal.goalType) {
            GoalType.MIN -> currentMinutes >= targetMinutes
            GoalType.MAX -> currentMinutes <= targetMinutes
            GoalType.EXACT -> currentMinutes == targetMinutes
        }

    val remainingMinutes: Int get() = (targetMinutes - currentMinutes).coerceAtLeast(0)
}
