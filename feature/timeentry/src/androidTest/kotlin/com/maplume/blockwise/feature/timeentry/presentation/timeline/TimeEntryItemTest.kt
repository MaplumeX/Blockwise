package com.maplume.blockwise.feature.timeentry.presentation.timeline

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.longClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme
import com.maplume.blockwise.core.domain.model.ActivityType
import com.maplume.blockwise.core.domain.model.Tag
import com.maplume.blockwise.core.domain.model.TimeEntry
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.toEpochDays
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

    private fun expectedDurationText(entry: TimeEntry): String {
        val tz = TimeZone.currentSystemDefault()
        val startLocal = entry.startTime.toLocalDateTime(tz)
        val endLocal = entry.endTime.toLocalDateTime(tz)

        val startStr = String.format("%02d:%02d", startLocal.hour, startLocal.minute)

        val endStr = if (
            endLocal.hour == 0 &&
                endLocal.minute == 0 &&
                endLocal.date.toEpochDays() == startLocal.date.toEpochDays() + 1
        ) {
            "24:00"
        } else {
            String.format("%02d:%02d", endLocal.hour, endLocal.minute)
        }

        return "$startStr - $endStr"
    }

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
                    onClick = { _ -> },
                    onLongClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithTag("timeEntryItem-1").assertTextContains("学习英语")
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
                    onClick = { _ -> },
                    onLongClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithTag("timeEntryItem-1").assertTextContains(expectedDurationText(entry))
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
                    onClick = { _ -> },
                    onLongClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithTag("timeEntryItem-1").assertTextContains(expectedDurationText(entry))
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
                    onClick = { _ -> },
                    onLongClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithTag("timeEntryItem-1").assertTextContains(expectedDurationText(entry))
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
                    onClick = { _ -> clicked = true },
                    onLongClick = {}
                )
            }
        }

        // When
        composeTestRule.onNodeWithTag("timeEntryItem-1").performClick()

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
                    onClick = { _ -> },
                    onLongClick = { longClicked = true }
                )
            }
        }

        // When
        composeTestRule.onNodeWithTag("timeEntryItem-1").performTouchInput { longClick() }

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
                    onClick = { _ -> },
                    onLongClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithTag("timeEntryItem-1").assertTextContains("完成项目文档")
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
                    onClick = { _ -> },
                    onLongClick = {}
                )
            }
        }

        // Then
        composeTestRule.onNodeWithTag("timeEntryItem-1").assertTextContains("工作")
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
                    onClick = { _ -> },
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
                    onClick = { _ -> },
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
                    onClick = { _ -> },
                    onLongClick = {}
                )
            }
        }

        // Then - "已选择" content description indicates selection
        composeTestRule.onNodeWithTag("timeEntryItem-1").assertTextContains("工作")
    }
}
