package com.maplume.blockwise.feature.settings.domain.usecase.settings

import com.maplume.blockwise.feature.settings.domain.model.ThemeMode
import com.maplume.blockwise.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for updating theme mode.
 */
class UpdateThemeModeUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    suspend operator fun invoke(themeMode: ThemeMode) {
        settingsRepository.setThemeMode(themeMode)
    }

    fun getThemeMode(): Flow<ThemeMode> = settingsRepository.getThemeMode()
}
