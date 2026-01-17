package com.maplume.blockwise.core.designsystem.component

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import com.maplume.blockwise.core.designsystem.testing.BaseComposeTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * UI tests for Button components.
 * Tests visual rendering and interaction behavior.
 */
class ButtonTest : BaseComposeTest() {

    // ==================== BlockwisePrimaryButton Tests ====================

    @Test
    fun primaryButton_displaysText() {
        // Given & When
        setThemedContent {
            BlockwisePrimaryButton(
                text = "Click Me",
                onClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Click Me").assertIsDisplayed()
    }

    @Test
    fun primaryButton_isClickable() {
        // Given
        var clicked = false

        setThemedContent {
            BlockwisePrimaryButton(
                text = "Click Me",
                onClick = { clicked = true }
            )
        }

        // When
        composeTestRule.onNodeWithText("Click Me").performClick()

        // Then
        assertTrue(clicked)
    }

    @Test
    fun primaryButton_disabledState() {
        // Given
        var clicked = false

        setThemedContent {
            BlockwisePrimaryButton(
                text = "Disabled",
                onClick = { clicked = true },
                enabled = false
            )
        }

        // Then - button should be displayed but disabled
        composeTestRule.onNodeWithText("Disabled")
            .assertIsDisplayed()
            .assertIsNotEnabled()

        // Click should not trigger callback
        composeTestRule.onNodeWithText("Disabled").performClick()
        assertEquals(false, clicked)
    }

    @Test
    fun primaryButton_loadingState_displaysText() {
        // Given & When
        setThemedContent {
            BlockwisePrimaryButton(
                text = "Loading",
                onClick = {},
                loading = true
            )
        }

        // Then - text should still be displayed during loading
        composeTestRule.onNodeWithText("Loading").assertIsDisplayed()
    }

    @Test
    fun primaryButton_loadingState_isNotClickable() {
        // Given
        var clicked = false

        setThemedContent {
            BlockwisePrimaryButton(
                text = "Loading",
                onClick = { clicked = true },
                loading = true
            )
        }

        // Then - button should be displayed but not enabled
        composeTestRule.onNodeWithText("Loading")
            .assertIsDisplayed()
            .assertIsNotEnabled()

        composeTestRule.onNodeWithText("Loading").performClick()
        assertEquals(false, clicked)
    }

    // ==================== BlockwiseSecondaryButton Tests ====================

    @Test
    fun secondaryButton_displaysText() {
        // Given & When
        setThemedContent {
            BlockwiseSecondaryButton(
                text = "Secondary",
                onClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Secondary").assertIsDisplayed()
    }

    @Test
    fun secondaryButton_isClickable() {
        // Given
        var clicked = false

        setThemedContent {
            BlockwiseSecondaryButton(
                text = "Click",
                onClick = { clicked = true }
            )
        }

        // When
        composeTestRule.onNodeWithText("Click").performClick()

        // Then
        assertTrue(clicked)
    }

    @Test
    fun secondaryButton_disabledState() {
        // Given
        setThemedContent {
            BlockwiseSecondaryButton(
                text = "Disabled",
                onClick = {},
                enabled = false
            )
        }

        // Then
        composeTestRule.onNodeWithText("Disabled")
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    // ==================== BlockwiseTextButton Tests ====================

    @Test
    fun textButton_displaysText() {
        // Given & When
        setThemedContent {
            BlockwiseTextButton(
                text = "Text Button",
                onClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Text Button").assertIsDisplayed()
    }

    @Test
    fun textButton_isClickable() {
        // Given
        var clickCount = 0

        setThemedContent {
            BlockwiseTextButton(
                text = "Increment",
                onClick = { clickCount++ }
            )
        }

        // When
        composeTestRule.onNodeWithText("Increment").performClick()
        composeTestRule.onNodeWithText("Increment").performClick()

        // Then
        assertEquals(2, clickCount)
    }

    @Test
    fun textButton_disabledState() {
        // Given
        setThemedContent {
            BlockwiseTextButton(
                text = "Disabled",
                onClick = {},
                enabled = false
            )
        }

        // Then
        composeTestRule.onNodeWithText("Disabled")
            .assertIsDisplayed()
            .assertIsNotEnabled()
    }

    // ==================== Theme Tests ====================

    @Test
    fun primaryButton_rendersInDarkTheme() {
        // Given & When
        setDarkThemedContent {
            BlockwisePrimaryButton(
                text = "Dark Theme",
                onClick = {}
            )
        }

        // Then
        composeTestRule.onNodeWithText("Dark Theme").assertIsDisplayed()
    }
}
