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
    val tagId: Long,
    val targetMinutes: Int,
    val goalType: GoalType,
    val period: GoalPeriod,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
    val isActive: Boolean = true
) {
    /**
     * Calculate target duration in hours and minutes format.
     * @return Pair of (hours, minutes)
     */
    fun targetAsHoursMinutes(): Pair<Int, Int> {
        val hours = targetMinutes / 60
        val minutes = targetMinutes % 60
        return hours to minutes
    }
}
