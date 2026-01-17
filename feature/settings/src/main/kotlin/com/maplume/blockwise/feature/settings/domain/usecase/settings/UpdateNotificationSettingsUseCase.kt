package com.maplume.blockwise.feature.settings.domain.usecase.settings

import com.maplume.blockwise.feature.settings.domain.model.NotificationSettings
import com.maplume.blockwise.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for updating notification settings.
 */
class UpdateNotificationSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    fun getNotificationSettings(): Flow<NotificationSettings> =
        settingsRepository.getNotificationSettings()

    suspend fun setDailyReminderEnabled(enabled: Boolean) {
        settingsRepository.setDailyReminderEnabled(enabled)
    }

    suspend fun setDailyReminderTime(minutesFromMidnight: Int) {
        settingsRepository.setDailyReminderTime(minutesFromMidnight)
    }

    suspend fun setGoalProgressEnabled(enabled: Boolean) {
        settingsRepository.setGoalProgressEnabled(enabled)
    }

    suspend fun setTimerNotificationEnabled(enabled: Boolean) {
        settingsRepository.setTimerNotificationEnabled(enabled)
    }
}
