package com.maplume.blockwise.feature.timeentry.data.local

import android.content.Context
import androidx.core.content.edit
import com.maplume.blockwise.feature.timeentry.domain.model.TimerState
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.datetime.Instant
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Handles persistence of timer state using SharedPreferences.
 * Enables crash recovery and app restart scenarios.
 */
@Singleton
class TimerPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    /**
     * Save the current timer state to SharedPreferences.
     */
    fun saveState(state: TimerState) {
        prefs.edit {
            when (state) {
                is TimerState.Idle -> {
                    putString(KEY_STATE, STATE_IDLE)
                    remove(KEY_ACTIVITY_ID)
                    remove(KEY_ACTIVITY_NAME)
                    remove(KEY_ACTIVITY_COLOR)
                    remove(KEY_START_TIME)
                    remove(KEY_ELAPSED_MILLIS)
                    remove(KEY_TAG_IDS)
                }
                is TimerState.Running -> {
                    putString(KEY_STATE, STATE_RUNNING)
                    putLong(KEY_ACTIVITY_ID, state.runningActivityId)
                    putString(KEY_ACTIVITY_NAME, state.runningActivityName)
                    putString(KEY_ACTIVITY_COLOR, state.activityColorHex)
                    putLong(KEY_START_TIME, state.startTime.toEpochMilliseconds())
                    putString(KEY_TAG_IDS, state.tagIds.joinToString(","))
                    remove(KEY_ELAPSED_MILLIS)
                }
                is TimerState.Paused -> {
                    putString(KEY_STATE, STATE_PAUSED)
                    putLong(KEY_ACTIVITY_ID, state.pausedActivityId)
                    putString(KEY_ACTIVITY_NAME, state.pausedActivityName)
                    putString(KEY_ACTIVITY_COLOR, state.activityColorHex)
                    putLong(KEY_START_TIME, state.startTime.toEpochMilliseconds())
                    putLong(KEY_ELAPSED_MILLIS, state.elapsedMillis)
                    putString(KEY_TAG_IDS, state.tagIds.joinToString(","))
                }
            }
        }
    }

    /**
     * Restore the timer state from SharedPreferences.
     * @return The restored TimerState, or TimerState.Idle if no state was saved.
     */
    fun restoreState(): TimerState {
        val stateString = prefs.getString(KEY_STATE, STATE_IDLE)

        return when (stateString) {
            STATE_RUNNING -> {
                val activityId = prefs.getLong(KEY_ACTIVITY_ID, -1)
                val activityName = prefs.getString(KEY_ACTIVITY_NAME, "") ?: ""
                val activityColor = prefs.getString(KEY_ACTIVITY_COLOR, "#4CAF50") ?: "#4CAF50"
                val startTime = prefs.getLong(KEY_START_TIME, 0)
                val tagIds = parseTagIds(prefs.getString(KEY_TAG_IDS, "") ?: "")

                if (activityId == -1L || startTime == 0L) {
                    TimerState.Idle
                } else {
                    TimerState.Running(
                        runningActivityId = activityId,
                        runningActivityName = activityName,
                        activityColorHex = activityColor,
                        startTime = Instant.fromEpochMilliseconds(startTime),
                        tagIds = tagIds
                    )
                }
            }
            STATE_PAUSED -> {
                val activityId = prefs.getLong(KEY_ACTIVITY_ID, -1)
                val activityName = prefs.getString(KEY_ACTIVITY_NAME, "") ?: ""
                val activityColor = prefs.getString(KEY_ACTIVITY_COLOR, "#4CAF50") ?: "#4CAF50"
                val startTime = prefs.getLong(KEY_START_TIME, 0)
                val elapsedMillis = prefs.getLong(KEY_ELAPSED_MILLIS, 0)
                val tagIds = parseTagIds(prefs.getString(KEY_TAG_IDS, "") ?: "")

                if (activityId == -1L || startTime == 0L) {
                    TimerState.Idle
                } else {
                    TimerState.Paused(
                        pausedActivityId = activityId,
                        pausedActivityName = activityName,
                        activityColorHex = activityColor,
                        startTime = Instant.fromEpochMilliseconds(startTime),
                        elapsedMillis = elapsedMillis,
                        tagIds = tagIds
                    )
                }
            }
            else -> TimerState.Idle
        }
    }

    /**
     * Check if there is a saved timer state that needs recovery.
     */
    fun hasActiveTimer(): Boolean {
        val stateString = prefs.getString(KEY_STATE, STATE_IDLE)
        return stateString == STATE_RUNNING || stateString == STATE_PAUSED
    }

    /**
     * Clear all saved timer state.
     */
    fun clear() {
        prefs.edit { clear() }
    }

    private fun parseTagIds(tagIdsString: String): List<Long> {
        if (tagIdsString.isBlank()) return emptyList()
        return tagIdsString.split(",").mapNotNull { it.toLongOrNull() }
    }

    companion object {
        private const val PREFS_NAME = "timer_prefs"

        private const val KEY_STATE = "state"
        private const val KEY_ACTIVITY_ID = "activity_id"
        private const val KEY_ACTIVITY_NAME = "activity_name"
        private const val KEY_ACTIVITY_COLOR = "activity_color"
        private const val KEY_START_TIME = "start_time"
        private const val KEY_ELAPSED_MILLIS = "elapsed_millis"
        private const val KEY_TAG_IDS = "tag_ids"

        private const val STATE_IDLE = "idle"
        private const val STATE_RUNNING = "running"
        private const val STATE_PAUSED = "paused"
    }
}
