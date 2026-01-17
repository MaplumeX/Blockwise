package com.maplume.blockwise.feature.onboarding.presentation

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maplume.blockwise.feature.onboarding.presentation.component.OnboardingNavButtons
import com.maplume.blockwise.feature.onboarding.presentation.component.OnboardingPageContent
import com.maplume.blockwise.feature.onboarding.presentation.component.PageIndicator
import kotlinx.coroutines.flow.collectLatest

/**
 * Main onboarding screen composable.
 *
 * @param onComplete Callback when onboarding is completed.
 * @param viewModel The onboarding view model.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val pagerState = rememberPagerState(
        initialPage = uiState.currentPage,
        pageCount = { uiState.pageCount }
    )

    // Sync pager state with view model
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }
            .collectLatest { page ->
                viewModel.onPageChanged(page)
            }
    }

    // Sync view model state with pager
    LaunchedEffect(uiState.currentPage) {
        if (pagerState.currentPage != uiState.currentPage) {
            pagerState.animateScrollToPage(uiState.currentPage)
        }
    }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collectLatest { event ->
            when (event) {
                is OnboardingEvent.NavigateToMain -> onComplete()
            }
        }
    }

    val currentPage = uiState.pages.getOrNull(pagerState.currentPage)
    val backgroundColor = currentPage?.backgroundColor
        ?: uiState.pages.firstOrNull()?.backgroundColor
        ?: androidx.compose.ui.graphics.Color(0xFF1976D2)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Page content with pager
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) { page ->
                OnboardingPageContent(
                    page = uiState.pages[page],
                    isVisible = page == pagerState.currentPage
                )
            }

            // Page indicator
            PageIndicator(
                pageCount = uiState.pageCount,
                currentPage = pagerState.currentPage,
                modifier = Modifier
                    .align(Alignment.CenterHorizontally)
                    .padding(bottom = 24.dp)
            )

            // Navigation buttons
            OnboardingNavButtons(
                isFirstPage = uiState.isFirstPage,
                isLastPage = uiState.isLastPage,
                onBackClick = viewModel::onBackClick,
                onNextClick = viewModel::onNextClick,
                onSkipClick = viewModel::onSkipClick
            )
        }
    }
}
