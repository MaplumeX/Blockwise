package com.maplume.blockwise.feature.timeentry.domain.usecase.timer

import com.maplume.blockwise.feature.timeentry.data.local.TimerPreferences
import com.maplume.blockwise.feature.timeentry.domain.model.TimerManager
import com.maplume.blockwise.feature.timeentry.domain.model.TimerState
import javax.inject.Inject

/**
 * Data class representing a recoverable timer session.
 */
data class RecoverableTimer(
    val activityId: Long,
    val activityName: String,
    val activityColorHex: String,
    val elapsedMillis: Long,
    val isPaused: Boolean
)

/**
 * Use case for checking if there's a timer that needs recovery after app restart/crash.
 */
class CheckTimerRecoveryUseCase @Inject constructor(
    private val timerPreferences: TimerPreferences,
    private val timerManager: TimerManager
) {
    /**
     * Check if there's a timer that needs recovery.
     * @return RecoverableTimer if there's a timer to recover, null otherwise.
     */
    operator fun invoke(): RecoverableTimer? {
        // If timer is already active in memory, no recovery needed
        if (timerManager.state.value.isActive) {
            return null
        }

        // Check persisted state
        val savedState = timerPreferences.restoreState()

        return when (savedState) {
            is TimerState.Running -> {
                // Calculate elapsed time since the timer was running
                val now = kotlinx.datetime.Clock.System.now()
                val elapsed = now.toEpochMilliseconds() - savedState.startTime.toEpochMilliseconds()
                RecoverableTimer(
                    activityId = savedState.activityId,
                    activityName = savedState.activityName,
                    activityColorHex = savedState.activityColorHex,
                    elapsedMillis = elapsed,
                    isPaused = false
                )
            }
            is TimerState.Paused -> {
                RecoverableTimer(
                    activityId = savedState.activityId,
                    activityName = savedState.activityName,
                    activityColorHex = savedState.activityColorHex,
                    elapsedMillis = savedState.elapsedMillis,
                    isPaused = true
                )
            }
            else -> null
        }
    }
}

/**
 * Use case for recovering a timer from saved state.
 */
class RecoverTimerUseCase @Inject constructor(
    private val timerPreferences: TimerPreferences,
    private val timerManager: TimerManager
) {
    /**
     * Recover the timer from saved state.
     * @return true if recovery was successful, false otherwise.
     */
    operator fun invoke(): Boolean {
        val savedState = timerPreferences.restoreState()

        return when (savedState) {
            is TimerState.Running, is TimerState.Paused -> {
                timerManager.restore(savedState)
                true
            }
            else -> false
        }
    }
}

/**
 * Use case for discarding a recoverable timer.
 */
class DiscardRecoverableTimerUseCase @Inject constructor(
    private val timerPreferences: TimerPreferences,
    private val timerManager: TimerManager
) {
    /**
     * Discard the recoverable timer without saving.
     */
    operator fun invoke() {
        timerManager.discard()
        timerPreferences.clear()
    }
}
