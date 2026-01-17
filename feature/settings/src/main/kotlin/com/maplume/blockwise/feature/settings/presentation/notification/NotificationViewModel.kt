package com.maplume.blockwise.feature.settings.presentation.notification

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maplume.blockwise.feature.settings.domain.model.NotificationSettings
import com.maplume.blockwise.feature.settings.domain.usecase.settings.UpdateNotificationSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the Notification settings screen.
 */
data class NotificationUiState(
    val settings: NotificationSettings = NotificationSettings(),
    val isLoading: Boolean = true
) {
    val dailyReminderTimeFormatted: String
        get() {
            val hours = settings.dailyReminderTime / 60
            val minutes = settings.dailyReminderTime % 60
            return String.format("%02d:%02d", hours, minutes)
        }
}

/**
 * ViewModel for the Notification settings screen.
 */
@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val updateNotificationSettingsUseCase: UpdateNotificationSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(NotificationUiState())
    val uiState: StateFlow<NotificationUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            updateNotificationSettingsUseCase.getNotificationSettings().collect { settings ->
                _uiState.update {
                    it.copy(settings = settings, isLoading = false)
                }
            }
        }
    }

    fun onDailyReminderEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            updateNotificationSettingsUseCase.setDailyReminderEnabled(enabled)
        }
    }

    fun onDailyReminderTimeChanged(minutesFromMidnight: Int) {
        viewModelScope.launch {
            updateNotificationSettingsUseCase.setDailyReminderTime(minutesFromMidnight)
        }
    }

    fun onGoalProgressEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            updateNotificationSettingsUseCase.setGoalProgressEnabled(enabled)
        }
    }

    fun onTimerNotificationEnabledChanged(enabled: Boolean) {
        viewModelScope.launch {
            updateNotificationSettingsUseCase.setTimerNotificationEnabled(enabled)
        }
    }
}
