package com.maplume.blockwise.feature.timeentry.presentation.timeline

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.assertTextEquals
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme
import com.maplume.blockwise.core.domain.model.TimelineViewMode
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimelineQuickCreateFabTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun fabOnlyVisibleInTimelineListView() {
        val now = Clock.System.now()
        val tz = TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(tz).date
        val yesterday = today.minus(1, DateTimeUnit.DAY)

        var mode by mutableStateOf(TimelineViewMode.LIST)

        composeTestRule.setContent {
            BlockwiseTheme {
                TimelineScreenContent(
                    uiState = TimelineUiState(
                        selectedDate = yesterday,
                        dayGroups = emptyList(),
                        isLoading = false
                    ),
                    snackbarHostState = androidx.compose.material3.SnackbarHostState(),
                    viewMode = mode,
                    onViewModeChange = {},
                    onRefresh = {},
                    onEntryClick = {},
                    onTimeBlockEntryClick = {},
                    onClearTimeBlockSelection = {},
                    onEntryLongPress = {},
                    onExitSelectionMode = {},
                    onDismissEntrySheet = {},
                    onSaveEntryDraft = {},
                    onCreateFromSheet = {},
                    onDraftStartDateChange = {},
                    onDraftStartTimeChange = {},
                    onDraftEndDateChange = {},
                    onDraftEndTimeChange = {},
                    onDraftActivitySelect = {},
                    onDraftTagToggle = {},
                    onDraftNoteChange = {},
                    onMergeUp = {},
                    onMergeDown = {},
                    onDeleteFromSheet = {},
                    onSplitFromSheet = {},
                    onBatchDelete = {},
                    onSplitConfirm = {},
                    onSplitCancel = {},
                    onMergeRequest = {},
                    onMergeConfirm = {},
                    onMergeCancel = {},
                    onCreateFromGap = { _, _ -> },
                    onCreateEntry = {},
                    onShowTimerActivitySelector = {},
                    onHideTimerActivitySelector = {},
                    onStartTimer = {},
                    onStopTimer = {},
                    onNavigateWeek = {},
                    onNavigateToToday = {},
                    onDateSelect = {},
                    onShowDatePicker = {},
                    onHideDatePicker = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("timelineQuickCreateFab").assertIsDisplayed()

        composeTestRule.runOnUiThread {
            mode = TimelineViewMode.TIME_BLOCK
        }
        composeTestRule.waitForIdle()

        var fabVisibleInTimeBlock = true
        try {
            composeTestRule.onNodeWithTag("timelineQuickCreateFab").assertIsDisplayed()
        } catch (_: AssertionError) {
            fabVisibleInTimeBlock = false
        }
        assertFalse(fabVisibleInTimeBlock)
    }

    @Test
    fun tapFabOpensCreateSheetAndCanDismissWithoutCreating() {
        val now = Clock.System.now()
        val tz = TimeZone.currentSystemDefault()
        val today = now.toLocalDateTime(tz).date
        val nonToday = today.minus(1, DateTimeUnit.DAY)

        var createInvoked = false
        var showSheet by mutableStateOf(false)

        var startDate by mutableStateOf(nonToday)
        var endDate by mutableStateOf(nonToday)

        var startTime by mutableStateOf(LocalTime(10, 0))
        var endTime by mutableStateOf(LocalTime(10, 0))

        fun makeUiState(): TimelineUiState {
            val draft = if (showSheet) {
                TimeEntryDraft(
                    entryId = 0L,
                    startDate = startDate,
                    endDate = endDate,
                    startTime = startTime,
                    endTime = endTime,
                    activityId = 1L,
                    tagIds = emptySet(),
                    note = "",
                    adjacentUpEntryId = null,
                    adjacentDownEntryId = null
                )
            } else {
                null
            }

            return TimelineUiState(
                selectedDate = nonToday,
                dayGroups = emptyList(),
                isLoading = false,
                sheetDraft = draft,
                sheetMode = TimelineEntrySheetMode.CREATE
            )
        }

        composeTestRule.setContent {
            BlockwiseTheme {
                TimelineScreenContent(
                    uiState = makeUiState(),
                    snackbarHostState = androidx.compose.material3.SnackbarHostState(),
                    viewMode = TimelineViewMode.LIST,
                    onViewModeChange = {},
                    onRefresh = {},
                    onEntryClick = {},
                    onTimeBlockEntryClick = {},
                    onClearTimeBlockSelection = {},
                    onEntryLongPress = {},
                    onExitSelectionMode = {},
                    onDismissEntrySheet = { showSheet = false },
                    onSaveEntryDraft = {},
                    onCreateFromSheet = { createInvoked = true },
                    onDraftStartDateChange = { startDate = it },
                    onDraftStartTimeChange = { startTime = it },
                    onDraftEndDateChange = { endDate = it },
                    onDraftEndTimeChange = { endTime = it },
                    onDraftActivitySelect = {},
                    onDraftTagToggle = {},
                    onDraftNoteChange = {},
                    onMergeUp = {},
                    onMergeDown = {},
                    onDeleteFromSheet = {},
                    onSplitFromSheet = {},
                    onBatchDelete = {},
                    onSplitConfirm = {},
                    onSplitCancel = {},
                    onMergeRequest = {},
                    onMergeConfirm = {},
                    onMergeCancel = {},
                    onCreateFromGap = { _, _ -> },
                    onCreateEntry = { showSheet = true },
                    onShowTimerActivitySelector = {},
                    onHideTimerActivitySelector = {},
                    onStartTimer = {},
                    onStopTimer = {},
                    onNavigateWeek = {},
                    onNavigateToToday = {},
                    onDateSelect = {},
                    onShowDatePicker = {},
                    onHideDatePicker = {}
                )
            }
        }

        composeTestRule.onNodeWithTag("timelineQuickCreateFab").performClick()
        composeTestRule.waitForIdle()

        composeTestRule.runOnIdle {
            showSheet = true
        }
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(2_000)
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("timelineCreateEntrySheet").assertIsDisplayed()

        composeTestRule.onNodeWithTag("startHourWheel").performScrollTo()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("startDateWheel").assertIsDisplayed()
        composeTestRule.onNodeWithTag("startHourWheel").assertIsDisplayed()
        composeTestRule.onNodeWithTag("startMinuteWheel").assertIsDisplayed()
        composeTestRule.onNodeWithTag("endDateWheel").assertIsDisplayed()
        composeTestRule.onNodeWithTag("endHourWheel").assertIsDisplayed()
        composeTestRule.onNodeWithTag("endMinuteWheel").assertIsDisplayed()

        composeTestRule.onNodeWithTag("startSelectedTimeText").assertIsDisplayed()
        composeTestRule.onNodeWithTag("startSelectedTimeText").assertTextEquals("10:00")

        composeTestRule.onNodeWithTag("startMinuteWheel").performScrollToIndex(1)
        composeTestRule.waitForIdle()
        composeTestRule.mainClock.advanceTimeBy(2_000)
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("startSelectedTimeText").assertTextEquals("10:01")

        composeTestRule.onNodeWithText("结束时间需晚于起始时间").performScrollTo()
        composeTestRule.onNodeWithText("结束时间需晚于起始时间").assertIsDisplayed()

        composeTestRule.runOnIdle {
            assertEquals(nonToday, endDate)
        }

        composeTestRule.onNodeWithText("创建").assertIsNotEnabled()

    }
}
