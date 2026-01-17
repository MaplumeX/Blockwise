package com.maplume.blockwise.feature.settings.presentation.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maplume.blockwise.feature.settings.domain.model.ThemeMode
import com.maplume.blockwise.feature.settings.domain.usecase.settings.UpdateThemeModeUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the Theme settings screen.
 */
data class ThemeUiState(
    val selectedTheme: ThemeMode = ThemeMode.SYSTEM,
    val isLoading: Boolean = true
)

/**
 * ViewModel for the Theme settings screen.
 */
@HiltViewModel
class ThemeViewModel @Inject constructor(
    private val updateThemeModeUseCase: UpdateThemeModeUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(ThemeUiState())
    val uiState: StateFlow<ThemeUiState> = _uiState.asStateFlow()

    init {
        loadThemeMode()
    }

    private fun loadThemeMode() {
        viewModelScope.launch {
            updateThemeModeUseCase.getThemeMode().collect { themeMode ->
                _uiState.update {
                    it.copy(selectedTheme = themeMode, isLoading = false)
                }
            }
        }
    }

    fun onThemeSelected(themeMode: ThemeMode) {
        viewModelScope.launch {
            updateThemeModeUseCase(themeMode)
        }
    }
}
