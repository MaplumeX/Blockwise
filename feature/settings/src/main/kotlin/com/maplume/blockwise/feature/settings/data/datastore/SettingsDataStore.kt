package com.maplume.blockwise.feature.settings.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.maplume.blockwise.core.domain.model.TimelineViewMode
import com.maplume.blockwise.feature.settings.domain.model.NotificationSettings
import com.maplume.blockwise.feature.settings.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "blockwise_settings"
)

/**
 * DataStore for persisting application settings.
 */
@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val dataStore = context.settingsDataStore

    // Theme
    private val themeModeKey = stringPreferencesKey("theme_mode")

    private val timelineViewModeKey = stringPreferencesKey("timeline_view_mode")

    // Notifications
    private val dailyReminderEnabledKey = booleanPreferencesKey("daily_reminder_enabled")
    private val dailyReminderTimeKey = intPreferencesKey("daily_reminder_time")
    private val goalProgressEnabledKey = booleanPreferencesKey("goal_progress_enabled")
    private val timerNotificationEnabledKey = booleanPreferencesKey("timer_notification_enabled")

    // Backup
    private val autoBackupEnabledKey = booleanPreferencesKey("auto_backup_enabled")
    private val lastBackupTimeKey = longPreferencesKey("last_backup_time")

    // Onboarding
    private val onboardingCompletedKey = booleanPreferencesKey("onboarding_completed")

    // Tooltips
    private val shownTooltipsKey = stringSetPreferencesKey("shown_tooltips")

    /**
     * Get the current theme mode as a Flow.
     */
    val themeMode: Flow<ThemeMode> = dataStore.data.map { preferences ->
        val value = preferences[themeModeKey] ?: ThemeMode.SYSTEM.name
        ThemeMode.valueOf(value)
    }

    val timelineViewMode: Flow<TimelineViewMode> = dataStore.data.map { preferences ->
        val value = preferences[timelineViewModeKey] ?: TimelineViewMode.LIST.name
        TimelineViewMode.valueOf(value)
    }

    /**
     * Get notification settings as a Flow.
     */
    val notificationSettings: Flow<NotificationSettings> = dataStore.data.map { preferences ->
        NotificationSettings(
            dailyReminderEnabled = preferences[dailyReminderEnabledKey] ?: false,
            dailyReminderTime = preferences[dailyReminderTimeKey] ?: 480,
            goalProgressEnabled = preferences[goalProgressEnabledKey] ?: true,
            timerNotificationEnabled = preferences[timerNotificationEnabledKey] ?: true
        )
    }

    /**
     * Get auto backup enabled state as a Flow.
     */
    val autoBackupEnabled: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[autoBackupEnabledKey] ?: false
    }

    /**
     * Get last backup time as a Flow.
     */
    val lastBackupTime: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[lastBackupTimeKey]
    }

    /**
     * Get onboarding completed state as a Flow.
     */
    val onboardingCompleted: Flow<Boolean> = dataStore.data.map { preferences ->
        preferences[onboardingCompletedKey] ?: false
    }

    /**
     * Get shown tooltips as a Flow.
     */
    val shownTooltips: Flow<Set<String>> = dataStore.data.map { preferences ->
        preferences[shownTooltipsKey] ?: emptySet()
    }

    /**
     * Update theme mode.
     */
    suspend fun setThemeMode(themeMode: ThemeMode) {
        dataStore.edit { preferences ->
            preferences[themeModeKey] = themeMode.name
        }
    }

    suspend fun setTimelineViewMode(viewMode: TimelineViewMode) {
        dataStore.edit { preferences ->
            preferences[timelineViewModeKey] = viewMode.name
        }
    }

    /**
     * Update daily reminder enabled state.
     */
    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[dailyReminderEnabledKey] = enabled
        }
    }

    /**
     * Update daily reminder time.
     */
    suspend fun setDailyReminderTime(minutesFromMidnight: Int) {
        dataStore.edit { preferences ->
            preferences[dailyReminderTimeKey] = minutesFromMidnight
        }
    }

    /**
     * Update goal progress notification enabled state.
     */
    suspend fun setGoalProgressEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[goalProgressEnabledKey] = enabled
        }
    }

    /**
     * Update timer notification enabled state.
     */
    suspend fun setTimerNotificationEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[timerNotificationEnabledKey] = enabled
        }
    }

    /**
     * Update auto backup enabled state.
     */
    suspend fun setAutoBackupEnabled(enabled: Boolean) {
        dataStore.edit { preferences ->
            preferences[autoBackupEnabledKey] = enabled
        }
    }

    /**
     * Update last backup time.
     */
    suspend fun setLastBackupTime(timestamp: Long) {
        dataStore.edit { preferences ->
            preferences[lastBackupTimeKey] = timestamp
        }
    }

    /**
     * Update onboarding completed state.
     */
    suspend fun setOnboardingCompleted(completed: Boolean) {
        dataStore.edit { preferences ->
            preferences[onboardingCompletedKey] = completed
        }
    }

    /**
     * Mark a tooltip as shown.
     */
    suspend fun markTooltipShown(tooltipId: String) {
        dataStore.edit { preferences ->
            val current = preferences[shownTooltipsKey] ?: emptySet()
            preferences[shownTooltipsKey] = current + tooltipId
        }
    }

    /**
     * Reset all shown tooltips.
     */
    suspend fun resetTooltips() {
        dataStore.edit { preferences ->
            preferences[shownTooltipsKey] = emptySet()
        }
    }
}
