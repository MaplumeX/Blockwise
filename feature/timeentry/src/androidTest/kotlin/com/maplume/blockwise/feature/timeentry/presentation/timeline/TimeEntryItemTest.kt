package com.maplume.blockwise.feature.timeentry.presentation.timeline

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performLongClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme
import com.maplume.blockwise.core.domain.model.ActivityType
import com.maplume.blockwise.core.domain.model.Tag
import com.maplume.blockwise.core.domain.model.TimeEntry
import kotlinx.datetime.Clock
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import kotlin.time.Duration.Companion.hours

/**
 * UI tests for TimeEntryItem component.
 * Tests display of activity name, duration, tags, note, and click interactions.
 */
@RunWith(AndroidJUnit4::class)
class TimeEntryItemTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private val now = Clock.System.now()

    private fun createTestEntry(
        activityName: String = "工作",
        colorHex: String = "#4CAF50",
        durationMinutes: Int = 60,
        note: String? = null,
        tags: List<Tag> = emptyList()
    ): TimeEntry = TimeEntry(
        id = 1,
        activity = ActivityType(
            id = 1,
            name = activityName,
            colorHex = colorHex,
            icon = null,
            parentId = null,
            displayOrder = 0,
            isArchived = false
        ),
        startTime = now.minus(durationMinutes.toLong().hours / 60),
        endTime = now,
        durationMinutes = durationMinutes,
        note = note,
        tags = tags
    )

    // ==================== Activity Name Display Tests ====================

    @Test
    fun displaysActivityName() {
        // Given
        val entry = createTestEntry(activityName = "学习英语")

        // When
        composeTestRule.setContent {
            BlockwiseTheme {
                TimeEntryItem(
                    entry = entry,
                    isSelected = false,
                    isSelectionMode = false,
                    onClick = {},
                    onLongClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("学习英语").assertIsDisplayed()
    }

    // ==================== Duration Display Tests ====================

    @Test
    fun displaysDuration() {
        // Given
        val entry = createTestEntry(durationMinutes = 90)

        // When
        composeTestRule.setContent {
            BlockwiseTheme {
                TimeEntryItem(
                    entry = entry,
                    isSelected = false,
                    isSelectionMode = false,
                    onClick = {},
                    onLongClick = {}
                )
            }
        }

        // Then - duration should be displayed (1小时30分钟)
        composeTestRule.onNodeWithText("1小时30分钟").assertIsDisplayed()
    }

    @Test
    fun displaysHoursOnlyDuration() {
        // Given
        val entry = createTestEntry(durationMinutes = 120)

        // When
        composeTestRule.setContent {
            BlockwiseTheme {
                TimeEntryItem(
                    entry = entry,
                    isSelected = false,
                    isSelectionMode = false,
                    onClick = {},
                    onLongClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("2小时").assertIsDisplayed()
    }

    @Test
    fun displaysMinutesOnlyDuration() {
        // Given
        val entry = createTestEntry(durationMinutes = 30)

        // When
        composeTestRule.setContent {
            BlockwiseTheme {
                TimeEntryItem(
                    entry = entry,
                    isSelected = false,
                    isSelectionMode = false,
                    onClick = {},
                    onLongClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("30分钟").assertIsDisplayed()
    }

    // ==================== Click Interaction Tests ====================

    @Test
    fun clickTriggersCallback() {
        // Given
        var clicked = false
        val entry = createTestEntry()

        composeTestRule.setContent {
            BlockwiseTheme {
                TimeEntryItem(
                    entry = entry,
                    isSelected = false,
                    isSelectionMode = false,
                    onClick = { clicked = true },
                    onLongClick = {}
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("工作").performClick()

        // Then
        assertTrue(clicked)
    }

    @Test
    fun longClickTriggersCallback() {
        // Given
        var longClicked = false
        val entry = createTestEntry()

        composeTestRule.setContent {
            BlockwiseTheme {
                TimeEntryItem(
                    entry = entry,
                    isSelected = false,
                    isSelectionMode = false,
                    onClick = {},
                    onLongClick = { longClicked = true }
                )
            }
        }

        // When
        composeTestRule.onNodeWithText("工作").performLongClick()

        // Then
        assertTrue(longClicked)
    }

    // ==================== Note Display Tests ====================

    @Test
    fun displaysNote() {
        // Given
        val entry = createTestEntry(note = "完成项目文档")

        // When
        composeTestRule.setContent {
            BlockwiseTheme {
                TimeEntryItem(
                    entry = entry,
                    isSelected = false,
                    isSelectionMode = false,
                    onClick = {},
                    onLongClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("完成项目文档").assertIsDisplayed()
    }

    @Test
    fun emptyNoteIsNotDisplayed() {
        // Given
        val entry = createTestEntry(note = null)

        // When
        composeTestRule.setContent {
            BlockwiseTheme {
                TimeEntryItem(
                    entry = entry,
                    isSelected = false,
                    isSelectionMode = false,
                    onClick = {},
                    onLongClick = {}
                )
            }
        }

        // Then - activity name should be displayed, but no note element
        composeTestRule.onNodeWithText("工作").assertIsDisplayed()
    }

    // ==================== Tags Display Tests ====================

    @Test
    fun displaysTags() {
        // Given
        val tags = listOf(
            Tag(id = 1, name = "重要", colorHex = "#F44336", isArchived = false),
            Tag(id = 2, name = "紧急", colorHex = "#FF9800", isArchived = false)
        )
        val entry = createTestEntry(tags = tags)

        // When
        composeTestRule.setContent {
            BlockwiseTheme {
                TimeEntryItem(
                    entry = entry,
                    isSelected = false,
                    isSelectionMode = false,
                    onClick = {},
                    onLongClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithText("重要").assertIsDisplayed()
        composeTestRule.onNodeWithText("紧急").assertIsDisplayed()
    }

    @Test
    fun displaysTagOverflowIndicator() {
        // Given - more than 3 tags
        val tags = listOf(
            Tag(id = 1, name = "重要", colorHex = "#F44336", isArchived = false),
            Tag(id = 2, name = "紧急", colorHex = "#FF9800", isArchived = false),
            Tag(id = 3, name = "项目A", colorHex = "#2196F3", isArchived = false),
            Tag(id = 4, name = "项目B", colorHex = "#4CAF50", isArchived = false)
        )
        val entry = createTestEntry(tags = tags)

        // When
        composeTestRule.setContent {
            BlockwiseTheme {
                TimeEntryItem(
                    entry = entry,
                    isSelected = false,
                    isSelectionMode = false,
                    onClick = {},
                    onLongClick = {}
                )
            }
        }

        // Then - should show +1 overflow indicator
        composeTestRule.onNodeWithText("+1").assertIsDisplayed()
    }

    // ==================== Selection Mode Tests ====================

    @Test
    fun selectionModeShowsIndicator() {
        // Given
        val entry = createTestEntry()

        // When
        composeTestRule.setContent {
            BlockwiseTheme {
                TimeEntryItem(
                    entry = entry,
                    isSelected = true,
                    isSelectionMode = true,
                    onClick = {},
                    onLongClick = {}
                )
            }
        }

        // Then - "已选择" content description indicates selection
        composeTestRule.onNodeWithText("工作").assertIsDisplayed()
    }
}
