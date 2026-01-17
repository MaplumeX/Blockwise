package com.maplume.blockwise.feature.settings.data.repository

import com.maplume.blockwise.feature.settings.data.datastore.SettingsDataStore
import com.maplume.blockwise.feature.settings.domain.model.AppSettings
import com.maplume.blockwise.feature.settings.domain.model.NotificationSettings
import com.maplume.blockwise.feature.settings.domain.model.ThemeMode
import com.maplume.blockwise.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation of SettingsRepository using DataStore.
 */
@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {

    override fun getSettings(): Flow<AppSettings> = combine(
        settingsDataStore.themeMode,
        settingsDataStore.notificationSettings,
        settingsDataStore.autoBackupEnabled,
        settingsDataStore.lastBackupTime
    ) { themeMode, notificationSettings, autoBackupEnabled, lastBackupTime ->
        AppSettings(
            themeMode = themeMode,
            notificationSettings = notificationSettings,
            autoBackupEnabled = autoBackupEnabled,
            lastBackupTime = lastBackupTime
        )
    }

    override fun getThemeMode(): Flow<ThemeMode> = settingsDataStore.themeMode

    override fun getNotificationSettings(): Flow<NotificationSettings> =
        settingsDataStore.notificationSettings

    override suspend fun setThemeMode(themeMode: ThemeMode) {
        settingsDataStore.setThemeMode(themeMode)
    }

    override suspend fun setDailyReminderEnabled(enabled: Boolean) {
        settingsDataStore.setDailyReminderEnabled(enabled)
    }

    override suspend fun setDailyReminderTime(minutesFromMidnight: Int) {
        settingsDataStore.setDailyReminderTime(minutesFromMidnight)
    }

    override suspend fun setGoalProgressEnabled(enabled: Boolean) {
        settingsDataStore.setGoalProgressEnabled(enabled)
    }

    override suspend fun setTimerNotificationEnabled(enabled: Boolean) {
        settingsDataStore.setTimerNotificationEnabled(enabled)
    }

    override suspend fun setAutoBackupEnabled(enabled: Boolean) {
        settingsDataStore.setAutoBackupEnabled(enabled)
    }

    override suspend fun setLastBackupTime(timestamp: Long) {
        settingsDataStore.setLastBackupTime(timestamp)
    }
}
