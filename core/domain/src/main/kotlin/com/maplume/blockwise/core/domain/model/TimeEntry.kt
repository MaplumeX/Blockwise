package com.maplume.blockwise.core.domain.model

import kotlinx.datetime.Instant

/**
 * Domain model representing a time entry record.
 */
data class TimeEntry(
    val id: Long = 0,
    val activityId: Long,
    val startTime: Instant,
    val endTime: Instant,
    val durationMinutes: Int,
    val note: String? = null,
    val tags: List<Tag> = emptyList(),
    val createdAt: Instant,
    val updatedAt: Instant
) {
    /**
     * Calculate duration in hours and minutes format.
     * @return Pair of (hours, minutes)
     */
    fun durationAsHoursMinutes(): Pair<Int, Int> {
        val hours = durationMinutes / 60
        val minutes = durationMinutes % 60
        return hours to minutes
    }
}
