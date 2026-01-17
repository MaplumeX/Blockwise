package com.maplume.blockwise.core.designsystem.testing

import androidx.compose.runtime.Composable
import androidx.compose.ui.test.junit4.ComposeContentTestRule
import androidx.compose.ui.test.junit4.createComposeRule
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme
import org.junit.Rule

/**
 * Base class for Compose UI tests.
 * Provides common setup and utilities for testing Compose components.
 */
abstract class BaseComposeTest {

    @get:Rule
    val composeTestRule: ComposeContentTestRule = createComposeRule()

    /**
     * Set content with BlockwiseTheme wrapper.
     * Use this to ensure all components are tested with the app theme.
     */
    protected fun setThemedContent(content: @Composable () -> Unit) {
        composeTestRule.setContent {
            BlockwiseTheme {
                content()
            }
        }
    }

    /**
     * Set content with BlockwiseTheme in dark mode.
     */
    protected fun setDarkThemedContent(content: @Composable () -> Unit) {
        composeTestRule.setContent {
            BlockwiseTheme(darkTheme = true) {
                content()
            }
        }
    }

    /**
     * Wait for idle to ensure all recompositions are complete.
     */
    protected fun waitForIdle() {
        composeTestRule.waitForIdle()
    }

    /**
     * Main clock advance helper.
     */
    protected fun advanceTimeBy(milliseconds: Long) {
        composeTestRule.mainClock.advanceTimeBy(milliseconds)
    }
}
