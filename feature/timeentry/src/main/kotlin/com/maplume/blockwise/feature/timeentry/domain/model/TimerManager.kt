package com.maplume.blockwise.feature.timeentry.domain.model

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.time.Duration.Companion.milliseconds

/**
 * Manages the timer state and provides operations for starting, pausing, resuming, and stopping the timer.
 * This class is a singleton to ensure consistent state across the application.
 */
@Singleton
class TimerManager @Inject constructor() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    private val _state = MutableStateFlow<TimerState>(TimerState.Idle)
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private val _elapsedMillis = MutableStateFlow(0L)
    val elapsedMillis: StateFlow<Long> = _elapsedMillis.asStateFlow()

    private var timerJob: Job? = null

    /**
     * Start the timer with the specified activity.
     * @param activityId The ID of the activity being timed.
     * @param activityName The name of the activity for display.
     * @param activityColorHex The color of the activity for display.
     * @param tagIds Optional list of tag IDs associated with this timer session.
     */
    fun start(
        activityId: Long,
        activityName: String,
        activityColorHex: String,
        tagIds: List<Long> = emptyList()
    ) {
        val now = Clock.System.now()
        _state.value = TimerState.Running(
            runningActivityId = activityId,
            runningActivityName = activityName,
            activityColorHex = activityColorHex,
            startTime = now,
            tagIds = tagIds
        )
        _elapsedMillis.value = 0
        startTicking()
    }

    /**
     * Pause the currently running timer.
     * Does nothing if the timer is not running.
     */
    fun pause() {
        val current = _state.value
        if (current is TimerState.Running) {
            timerJob?.cancel()
            _state.value = TimerState.Paused(
                pausedActivityId = current.runningActivityId,
                pausedActivityName = current.runningActivityName,
                activityColorHex = current.activityColorHex,
                startTime = current.startTime,
                elapsedMillis = _elapsedMillis.value,
                tagIds = current.tagIds
            )
        }
    }

    /**
     * Resume a paused timer.
     * Does nothing if the timer is not paused.
     */
    fun resume() {
        val current = _state.value
        if (current is TimerState.Paused) {
            _state.value = TimerState.Running(
                runningActivityId = current.pausedActivityId,
                runningActivityName = current.pausedActivityName,
                activityColorHex = current.activityColorHex,
                startTime = current.startTime,
                tagIds = current.tagIds
            )
            startTicking(current.elapsedMillis)
        }
    }

    /**
     * Stop the timer and return the result.
     * @return TimerResult containing the timer data, or null if the timer was idle.
     */
    fun stop(): TimerResult? {
        val current = _state.value
        timerJob?.cancel()
        _state.value = TimerState.Idle
        val elapsed = _elapsedMillis.value
        _elapsedMillis.value = 0

        return when (current) {
            is TimerState.Running -> TimerResult(
                activityId = current.runningActivityId,
                startTime = current.startTime,
                endTime = Clock.System.now(),
                tagIds = current.tagIds
            )
            is TimerState.Paused -> TimerResult(
                activityId = current.pausedActivityId,
                startTime = current.startTime,
                endTime = current.startTime + elapsed.milliseconds,
                tagIds = current.tagIds
            )
            else -> null
        }
    }

    /**
     * Restore the timer state from persisted data.
     * Used for crash recovery and app restart scenarios.
     */
    fun restore(state: TimerState, elapsedMillis: Long = 0) {
        _state.value = state
        when (state) {
            is TimerState.Running -> {
                // Calculate elapsed time since start
                val now = Clock.System.now()
                val actualElapsed = now.toEpochMilliseconds() - state.startTime.toEpochMilliseconds()
                _elapsedMillis.value = actualElapsed
                startTicking(actualElapsed)
            }
            is TimerState.Paused -> {
                _elapsedMillis.value = state.elapsedMillis
            }
            else -> {
                _elapsedMillis.value = 0
            }
        }
    }

    /**
     * Discard the current timer without creating a time entry.
     */
    fun discard() {
        timerJob?.cancel()
        _state.value = TimerState.Idle
        _elapsedMillis.value = 0
    }

    private fun startTicking(initialElapsed: Long = 0) {
        timerJob?.cancel()
        timerJob = scope.launch {
            var elapsed = initialElapsed
            while (isActive) {
                _elapsedMillis.value = elapsed
                delay(1000)
                elapsed += 1000
            }
        }
    }
}
