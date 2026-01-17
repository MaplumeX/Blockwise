package com.maplume.blockwise.feature.onboarding.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.maplume.blockwise.feature.settings.data.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the onboarding screen.
 */
@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : ViewModel() {

    private val _uiState = MutableStateFlow(OnboardingUiState())
    val uiState: StateFlow<OnboardingUiState> = _uiState.asStateFlow()

    private val _events = Channel<OnboardingEvent>(Channel.BUFFERED)
    val events = _events.receiveAsFlow()

    /**
     * Called when the user swipes to a different page.
     */
    fun onPageChanged(page: Int) {
        _uiState.update { it.copy(currentPage = page) }
    }

    /**
     * Called when the user clicks the next button.
     */
    fun onNextClick() {
        val currentState = _uiState.value
        if (currentState.isLastPage) {
            completeOnboarding()
        } else {
            _uiState.update { it.copy(currentPage = currentState.currentPage + 1) }
        }
    }

    /**
     * Called when the user clicks the back button.
     */
    fun onBackClick() {
        val currentState = _uiState.value
        if (!currentState.isFirstPage) {
            _uiState.update { it.copy(currentPage = currentState.currentPage - 1) }
        }
    }

    /**
     * Called when the user clicks the skip button.
     */
    fun onSkipClick() {
        completeOnboarding()
    }

    /**
     * Mark onboarding as completed and navigate to main screen.
     */
    private fun completeOnboarding() {
        viewModelScope.launch {
            settingsDataStore.setOnboardingCompleted(true)
            _events.send(OnboardingEvent.NavigateToMain)
        }
    }
}
