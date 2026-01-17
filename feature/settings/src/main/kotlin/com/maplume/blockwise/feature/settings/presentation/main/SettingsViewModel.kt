package com.maplume.blockwise.feature.settings.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maplume.blockwise.feature.settings.domain.usecase.settings.GetSettingsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the Settings screen.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettingsUseCase: GetSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _navigationEvent = MutableSharedFlow<SettingsNavigationEvent>()
    val navigationEvent: SharedFlow<SettingsNavigationEvent> = _navigationEvent.asSharedFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            getSettingsUseCase().collect { settings ->
                _uiState.update {
                    it.copy(settings = settings, isLoading = false)
                }
            }
        }
    }

    fun onThemeClick() {
        viewModelScope.launch {
            _navigationEvent.emit(SettingsNavigationEvent.NavigateToTheme)
        }
    }

    fun onNotificationClick() {
        viewModelScope.launch {
            _navigationEvent.emit(SettingsNavigationEvent.NavigateToNotification)
        }
    }

    fun onDataManagementClick() {
        viewModelScope.launch {
            _navigationEvent.emit(SettingsNavigationEvent.NavigateToDataManagement)
        }
    }

    fun onAboutClick() {
        viewModelScope.launch {
            _navigationEvent.emit(SettingsNavigationEvent.NavigateToAbout)
        }
    }

    fun onActivityTypesClick() {
        viewModelScope.launch {
            _navigationEvent.emit(SettingsNavigationEvent.NavigateToActivityTypes)
        }
    }

    fun onTagsClick() {
        viewModelScope.launch {
            _navigationEvent.emit(SettingsNavigationEvent.NavigateToTags)
        }
    }
}
