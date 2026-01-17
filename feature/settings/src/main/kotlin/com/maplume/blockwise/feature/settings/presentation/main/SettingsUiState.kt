package com.maplume.blockwise.feature.settings.presentation.main

import com.maplume.blockwise.feature.settings.domain.model.AppSettings
import com.maplume.blockwise.feature.settings.domain.model.ThemeMode

/**
 * UI state for the Settings screen.
 */
data class SettingsUiState(
    val settings: AppSettings = AppSettings(),
    val isLoading: Boolean = true
) {
    val themeDisplayName: String
        get() = when (settings.themeMode) {
            ThemeMode.LIGHT -> "浅色"
            ThemeMode.DARK -> "深色"
            ThemeMode.SYSTEM -> "跟随系统"
        }
}

/**
 * Navigation events from the Settings screen.
 */
sealed class SettingsNavigationEvent {
    data object NavigateToTheme : SettingsNavigationEvent()
    data object NavigateToNotification : SettingsNavigationEvent()
    data object NavigateToDataManagement : SettingsNavigationEvent()
    data object NavigateToAbout : SettingsNavigationEvent()
    data object NavigateToActivityTypes : SettingsNavigationEvent()
    data object NavigateToTags : SettingsNavigationEvent()
}
