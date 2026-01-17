package com.maplume.blockwise.feature.settings.domain.repository

import com.maplume.blockwise.feature.settings.domain.model.AppSettings
import com.maplume.blockwise.feature.settings.domain.model.NotificationSettings
import com.maplume.blockwise.feature.settings.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface for application settings.
 */
interface SettingsRepository {
    /**
     * Get all settings as a Flow.
     */
    fun getSettings(): Flow<AppSettings>

    /**
     * Get current theme mode as a Flow.
     */
    fun getThemeMode(): Flow<ThemeMode>

    /**
     * Get notification settings as a Flow.
     */
    fun getNotificationSettings(): Flow<NotificationSettings>

    /**
     * Update theme mode.
     */
    suspend fun setThemeMode(themeMode: ThemeMode)

    /**
     * Update daily reminder enabled state.
     */
    suspend fun setDailyReminderEnabled(enabled: Boolean)

    /**
     * Update daily reminder time.
     */
    suspend fun setDailyReminderTime(minutesFromMidnight: Int)

    /**
     * Update goal progress notification enabled state.
     */
    suspend fun setGoalProgressEnabled(enabled: Boolean)

    /**
     * Update timer notification enabled state.
     */
    suspend fun setTimerNotificationEnabled(enabled: Boolean)

    /**
     * Update auto backup enabled state.
     */
    suspend fun setAutoBackupEnabled(enabled: Boolean)

    /**
     * Update last backup time.
     */
    suspend fun setLastBackupTime(timestamp: Long)
}
