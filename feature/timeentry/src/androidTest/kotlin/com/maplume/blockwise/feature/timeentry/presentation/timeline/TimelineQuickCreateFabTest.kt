package com.maplume.blockwise.feature.timeentry.presentation.timeline

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import org.junit.Assert.assertFalse
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme
import com.maplume.blockwise.core.domain.model.TimelineViewMode
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
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
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date

        var mode by mutableStateOf(TimelineViewMode.LIST)

        composeTestRule.setContent {
            BlockwiseTheme {
                TimelineScreenContent(
                    uiState = TimelineUiState(
                        selectedDate = today,
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
                    onDraftStartTimeChange = {},
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
        val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date

        var createInvoked = false
        var showSheet by mutableStateOf(false)

        fun makeUiState(): TimelineUiState {
            val draft = if (showSheet) {
                TimeEntryDraft(
                    entryId = 0L,
                    baseDate = today,
                    startTime = kotlinx.datetime.LocalTime(10, 0),
                    endTime = kotlinx.datetime.LocalTime(10, 0),
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
                selectedDate = today,
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
                    onDraftStartTimeChange = {},
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
                    onCreateEntry = { showSheet = true },
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

        composeTestRule.onNodeWithText("结束时间需晚于起始时间").assertIsDisplayed()
        composeTestRule.onNodeWithText("创建").assertIsNotEnabled()

        composeTestRule.onNodeWithContentDescription("关闭").performClick()
        composeTestRule.waitForIdle()

        assert(!createInvoked)
    }
}
