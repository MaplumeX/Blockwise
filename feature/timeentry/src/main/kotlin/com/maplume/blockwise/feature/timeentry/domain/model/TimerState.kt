package com.maplume.blockwise.feature.timeentry.domain.model

import kotlinx.datetime.Instant

/**
 * Sealed class representing the possible states of the timer.
 */
sealed class TimerState {
    /**
     * Timer is idle, not running.
     */
    data object Idle : TimerState()

    /**
     * Timer is actively running.
     */
    data class Running(
        val runningActivityId: Long,
        val runningActivityName: String,
        val activityColorHex: String,
        val startTime: Instant,
        val tagIds: List<Long> = emptyList()
    ) : TimerState() {
        override val activityId: Long get() = runningActivityId
        override val activityName: String get() = runningActivityName
    }

    /**
     * Timer is paused.
     */
    data class Paused(
        val pausedActivityId: Long,
        val pausedActivityName: String,
        val activityColorHex: String,
        val startTime: Instant,
        val elapsedMillis: Long,
        val tagIds: List<Long> = emptyList()
    ) : TimerState() {
        override val activityId: Long get() = pausedActivityId
        override val activityName: String get() = pausedActivityName
    }

    val isActive: Boolean
        get() = this is Running || this is Paused

    open val activityId: Long?
        get() = null

    open val activityName: String?
        get() = null
}

/**
 * Result of stopping the timer, containing data needed to create a time entry.
 */
data class TimerResult(
    val activityId: Long,
    val startTime: Instant,
    val endTime: Instant,
    val tagIds: List<Long>
) {
    val durationMillis: Long
        get() = endTime.toEpochMilliseconds() - startTime.toEpochMilliseconds()

    val durationMinutes: Int
        get() = (durationMillis / 60_000).toInt()
}
