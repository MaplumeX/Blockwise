package com.maplume.blockwise.feature.onboarding.presentation

import com.maplume.blockwise.feature.onboarding.domain.model.OnboardingContent
import com.maplume.blockwise.feature.onboarding.domain.model.OnboardingPage

/**
 * UI state for the onboarding screen.
 */
data class OnboardingUiState(
    val pages: List<OnboardingPage> = OnboardingContent.pages,
    val currentPage: Int = 0
) {
    val isFirstPage: Boolean get() = currentPage == 0
    val isLastPage: Boolean get() = currentPage == pages.lastIndex
    val pageCount: Int get() = pages.size
}

/**
 * Events emitted by the onboarding screen.
 */
sealed class OnboardingEvent {
    /**
     * Navigate to the main screen after onboarding is complete.
     */
    data object NavigateToMain : OnboardingEvent()
}
