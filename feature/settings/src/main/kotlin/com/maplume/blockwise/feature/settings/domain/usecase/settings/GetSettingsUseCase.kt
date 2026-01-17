package com.maplume.blockwise.feature.settings.domain.usecase.settings

import com.maplume.blockwise.feature.settings.domain.model.AppSettings
import com.maplume.blockwise.feature.settings.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for getting all application settings.
 */
class GetSettingsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    operator fun invoke(): Flow<AppSettings> = settingsRepository.getSettings()
}
