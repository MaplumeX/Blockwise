package com.maplume.blockwise.feature.timeentry.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.maplume.blockwise.feature.timeentry.data.local.TimerPreferences
import com.maplume.blockwise.feature.timeentry.domain.model.TimerManager
import com.maplume.blockwise.feature.timeentry.domain.model.TimerResult
import com.maplume.blockwise.feature.timeentry.domain.model.TimerState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service that keeps the timer running even when the app is in the background.
 * Displays a persistent notification with timer controls.
 */
@AndroidEntryPoint
class TimerService : Service() {

    @Inject
    lateinit var timerManager: TimerManager

    @Inject
    lateinit var timerPreferences: TimerPreferences

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var isServiceRunning = false

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val activityId = intent.getLongExtra(EXTRA_ACTIVITY_ID, -1)
                val activityName = intent.getStringExtra(EXTRA_ACTIVITY_NAME) ?: ""
                val activityColor = intent.getStringExtra(EXTRA_ACTIVITY_COLOR) ?: "#4CAF50"
                val tagIds = intent.getLongArrayExtra(EXTRA_TAG_IDS)?.toList() ?: emptyList()

                if (activityId != -1L) {
                    timerManager.start(activityId, activityName, activityColor, tagIds)
                    timerPreferences.saveState(timerManager.state.value)
                    startForegroundService()
                }
            }
            ACTION_PAUSE -> {
                timerManager.pause()
                timerPreferences.saveState(timerManager.state.value)
                updateNotification()
            }
            ACTION_RESUME -> {
                timerManager.resume()
                timerPreferences.saveState(timerManager.state.value)
                updateNotification()
            }
            ACTION_STOP -> {
                val result = timerManager.stop()
                timerPreferences.clear()
                stopForegroundService()
                // Broadcast the result so the UI can create the time entry
                result?.let { broadcastTimerResult(it) }
            }
            ACTION_DISCARD -> {
                timerManager.discard()
                timerPreferences.clear()
                stopForegroundService()
            }
            ACTION_RESTORE -> {
                // Restore timer state from preferences
                val savedState = timerPreferences.restoreState()
                if (savedState is TimerState.Running || savedState is TimerState.Paused) {
                    timerManager.restore(savedState)
                    startForegroundService()
                }
            }
        }

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun startForegroundService() {
        if (!isServiceRunning) {
            isServiceRunning = true
            startForeground(NOTIFICATION_ID, createNotification())
            observeTimerUpdates()
        }
    }

    private fun stopForegroundService() {
        isServiceRunning = false
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun observeTimerUpdates() {
        serviceScope.launch {
            combine(
                timerManager.state,
                timerManager.elapsedMillis
            ) { state, elapsed ->
                state to elapsed
            }.collect { (state, _) ->
                if (state is TimerState.Idle) {
                    stopForegroundService()
                } else {
                    updateNotification()
                    // Periodically save state for crash recovery
                    timerPreferences.saveState(state)
                }
            }
        }
    }

    private fun updateNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, createNotification())
    }

    private fun createNotification(): Notification {
        val state = timerManager.state.value
        val elapsed = timerManager.elapsedMillis.value

        val title = when (state) {
            is TimerState.Running -> "正在计时: ${state.runningActivityName}"
            is TimerState.Paused -> "已暂停: ${state.pausedActivityName}"
            else -> "计时器"
        }

        val content = formatDuration(elapsed)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(android.R.drawable.ic_menu_recent_history)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setCategory(NotificationCompat.CATEGORY_STOPWATCH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentIntent(createContentIntent())

        // Add action buttons based on state
        when (state) {
            is TimerState.Running -> {
                builder.addAction(
                    android.R.drawable.ic_media_pause,
                    "暂停",
                    createActionIntent(ACTION_PAUSE)
                )
                builder.addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "停止",
                    createActionIntent(ACTION_STOP)
                )
            }
            is TimerState.Paused -> {
                builder.addAction(
                    android.R.drawable.ic_media_play,
                    "继续",
                    createActionIntent(ACTION_RESUME)
                )
                builder.addAction(
                    android.R.drawable.ic_menu_close_clear_cancel,
                    "停止",
                    createActionIntent(ACTION_STOP)
                )
            }
            else -> {}
        }

        return builder.build()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "计时器",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "显示计时器状态"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun createContentIntent(): PendingIntent {
        // Create an intent to open the main activity
        val intent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        return PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun createActionIntent(action: String): PendingIntent {
        val intent = Intent(this, TimerService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun broadcastTimerResult(result: TimerResult) {
        val intent = Intent(ACTION_TIMER_RESULT).apply {
            putExtra(EXTRA_ACTIVITY_ID, result.activityId)
            putExtra(EXTRA_START_TIME, result.startTime.toEpochMilliseconds())
            putExtra(EXTRA_END_TIME, result.endTime.toEpochMilliseconds())
            putExtra(EXTRA_TAG_IDS, result.tagIds.toLongArray())
            setPackage(packageName)
        }
        sendBroadcast(intent)
    }

    private fun formatDuration(millis: Long): String {
        val totalSeconds = millis / 1000
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            String.format("%d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    companion object {
        const val CHANNEL_ID = "timer_channel"
        const val NOTIFICATION_ID = 1001

        const val ACTION_START = "com.maplume.blockwise.timer.START"
        const val ACTION_PAUSE = "com.maplume.blockwise.timer.PAUSE"
        const val ACTION_RESUME = "com.maplume.blockwise.timer.RESUME"
        const val ACTION_STOP = "com.maplume.blockwise.timer.STOP"
        const val ACTION_DISCARD = "com.maplume.blockwise.timer.DISCARD"
        const val ACTION_RESTORE = "com.maplume.blockwise.timer.RESTORE"
        const val ACTION_TIMER_RESULT = "com.maplume.blockwise.timer.RESULT"

        const val EXTRA_ACTIVITY_ID = "activity_id"
        const val EXTRA_ACTIVITY_NAME = "activity_name"
        const val EXTRA_ACTIVITY_COLOR = "activity_color"
        const val EXTRA_TAG_IDS = "tag_ids"
        const val EXTRA_START_TIME = "start_time"
        const val EXTRA_END_TIME = "end_time"

        /**
         * Create an intent to start the timer service.
         */
        fun startIntent(
            context: Context,
            activityId: Long,
            activityName: String,
            activityColor: String,
            tagIds: List<Long> = emptyList()
        ): Intent {
            return Intent(context, TimerService::class.java).apply {
                action = ACTION_START
                putExtra(EXTRA_ACTIVITY_ID, activityId)
                putExtra(EXTRA_ACTIVITY_NAME, activityName)
                putExtra(EXTRA_ACTIVITY_COLOR, activityColor)
                putExtra(EXTRA_TAG_IDS, tagIds.toLongArray())
            }
        }

        /**
         * Create an intent to pause the timer.
         */
        fun pauseIntent(context: Context): Intent {
            return Intent(context, TimerService::class.java).apply {
                action = ACTION_PAUSE
            }
        }

        /**
         * Create an intent to resume the timer.
         */
        fun resumeIntent(context: Context): Intent {
            return Intent(context, TimerService::class.java).apply {
                action = ACTION_RESUME
            }
        }

        /**
         * Create an intent to stop the timer and save the entry.
         */
        fun stopIntent(context: Context): Intent {
            return Intent(context, TimerService::class.java).apply {
                action = ACTION_STOP
            }
        }

        /**
         * Create an intent to discard the timer without saving.
         */
        fun discardIntent(context: Context): Intent {
            return Intent(context, TimerService::class.java).apply {
                action = ACTION_DISCARD
            }
        }

        /**
         * Create an intent to restore the timer from saved state.
         */
        fun restoreIntent(context: Context): Intent {
            return Intent(context, TimerService::class.java).apply {
                action = ACTION_RESTORE
            }
        }
    }
}
