package com.maplume.blockwise.feature.timeentry.domain.usecase.timer

import android.content.Context
import android.content.Intent
import android.os.Build
import com.maplume.blockwise.core.domain.model.TimeEntryInput
import com.maplume.blockwise.core.domain.repository.TimeEntryRepository
import com.maplume.blockwise.feature.timeentry.data.local.TimerPreferences
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import com.maplume.blockwise.feature.timeentry.domain.model.TimerManager
import com.maplume.blockwise.feature.timeentry.domain.model.TimerResult
import com.maplume.blockwise.feature.timeentry.service.TimerService
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

/**
 * Use case for starting the timer.
 */
class StartTimerUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val timerManager: TimerManager,
    private val timerPreferences: TimerPreferences
) {
    /**
     * Start the timer with the specified activity.
     * @param activityId The ID of the activity being timed.
     * @param activityName The name of the activity for display.
     * @param activityColorHex The color of the activity for display.
     * @param tagIds Optional list of tag IDs associated with this timer session.
     */
    operator fun invoke(
        activityId: Long,
        activityName: String,
        activityColorHex: String,
        tagIds: List<Long> = emptyList()
    ) {
        // Start the foreground service
        val intent = TimerService.startIntent(
            context = context,
            activityId = activityId,
            activityName = activityName,
            activityColor = activityColorHex,
            tagIds = tagIds
        )

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
    }
}

/**
 * Use case for pausing the timer.
 */
class PauseTimerUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Pause the currently running timer.
     */
    operator fun invoke() {
        val intent = TimerService.pauseIntent(context)
        context.startService(intent)
    }
}

/**
 * Use case for resuming the timer.
 */
class ResumeTimerUseCase @Inject constructor(
    @ApplicationContext private val context: Context
) {
    /**
     * Resume a paused timer.
     */
    operator fun invoke() {
        val intent = TimerService.resumeIntent(context)
        context.startService(intent)
    }
}

/**
 * Use case for stopping the timer and creating a time entry.
 */
class StopTimerUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val timerManager: TimerManager,
    private val timerPreferences: TimerPreferences,
    private val timeEntryRepository: TimeEntryRepository
) {
    /**
     * Stop the timer and optionally create a time entry.
     * @param createEntry Whether to create a time entry from the timer result.
     * @return Result containing the created entry ID, or null if no entry was created.
     */
    suspend operator fun invoke(createEntry: Boolean = true): Result<Long?> {
        // Get the current timer result before stopping
        val result = timerManager.stop()
        timerPreferences.clear()

        // Stop the service
        val intent = TimerService.stopIntent(context)
        context.startService(intent)

        if (!createEntry || result == null) {
            return Result.success(null)
        }

        // Create time entry from timer result
        return try {
            val entries = buildTimerEntries(
                activityId = result.activityId,
                startTime = result.startTime,
                endTime = result.endTime,
                tagIds = result.tagIds
            )

            val ids = entries.map { input -> timeEntryRepository.create(input) }
            Result.success(ids.firstOrNull())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

internal fun buildTimerEntries(
    activityId: Long,
    startTime: kotlinx.datetime.Instant,
    endTime: kotlinx.datetime.Instant,
    tagIds: List<Long>
): List<TimeEntryInput> {
    val startMillis = startTime.toEpochMilliseconds()
    val endMillis = endTime.toEpochMilliseconds()
    val resolvedEnd = if (endMillis <= startMillis) {
        kotlinx.datetime.Instant.fromEpochMilliseconds(startMillis + 1000)
    } else {
        endTime
    }

    val tz = kotlinx.datetime.TimeZone.currentSystemDefault()
    val startLocal = startTime.toLocalDateTime(tz)
    val endLocal = resolvedEnd.toLocalDateTime(tz)

    return if (startLocal.date == endLocal.date) {
        listOf(
            TimeEntryInput(
                activityId = activityId,
                startTime = startTime,
                endTime = resolvedEnd,
                note = null,
                tagIds = tagIds
            )
        )
    } else {
        val nextDate = kotlinx.datetime.LocalDate.fromEpochDays(startLocal.date.toEpochDays() + 1)
        val midnight = nextDate.atTime(kotlinx.datetime.LocalTime(0, 0)).toInstant(tz)

        listOf(
            TimeEntryInput(
                activityId = activityId,
                startTime = startTime,
                endTime = midnight,
                note = null,
                tagIds = tagIds
            ),
            TimeEntryInput(
                activityId = activityId,
                startTime = midnight,
                endTime = resolvedEnd,
                note = null,
                tagIds = tagIds
            )
        )
    }
}

/**
 * Use case for discarding the timer without saving.
 */
class DiscardTimerUseCase @Inject constructor(
    @ApplicationContext private val context: Context,
    private val timerManager: TimerManager,
    private val timerPreferences: TimerPreferences
) {
    /**
     * Discard the current timer without creating a time entry.
     */
    operator fun invoke() {
        timerManager.discard()
        timerPreferences.clear()

        // Stop the service
        val intent = TimerService.discardIntent(context)
        context.startService(intent)
    }
}

/**
 * Use case for getting the current timer state.
 */
class GetTimerStateUseCase @Inject constructor(
    private val timerManager: TimerManager
) {
    /**
     * Get the current timer state as a Flow.
     */
    val state get() = timerManager.state

    /**
     * Get the elapsed time as a Flow.
     */
    val elapsedMillis get() = timerManager.elapsedMillis
}
