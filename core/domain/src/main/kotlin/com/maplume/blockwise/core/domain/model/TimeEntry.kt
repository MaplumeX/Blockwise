package com.maplume.blockwise.core.domain.model

import kotlinx.datetime.Instant
import kotlin.time.Duration
import kotlin.time.Duration.Companion.minutes

/**
 * Domain model representing a time entry record.
 */
data class TimeEntry(
    val id: Long = 0,
    val activity: ActivityType,
    val startTime: Instant,
    val endTime: Instant,
    val durationMinutes: Int,
    val note: String? = null,
    val tags: List<Tag> = emptyList()
) {
    val activityId: Long get() = activity.id

    val duration: Duration get() = durationMinutes.minutes

    val formattedDuration: String
        get() {
            val hours = durationMinutes / 60
            val minutes = durationMinutes % 60
            return when {
                hours > 0 && minutes > 0 -> "${hours}小时${minutes}分钟"
                hours > 0 -> "${hours}小时"
                else -> "${minutes}分钟"
            }
        }

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

data class TimeEntryInput(
    val activityId: Long,
    val startTime: Instant,
    val endTime: Instant,
    val note: String? = null,
    val tagIds: List<Long> = emptyList()
) {
    val durationMinutes: Int
        get() = ((endTime.toEpochMilliseconds() - startTime.toEpochMilliseconds()) / 60000).toInt()
}
