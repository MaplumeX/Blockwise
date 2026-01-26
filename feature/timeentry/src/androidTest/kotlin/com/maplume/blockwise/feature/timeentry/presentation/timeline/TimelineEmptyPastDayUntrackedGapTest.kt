package com.maplume.blockwise.feature.timeentry.presentation.timeline

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme
import com.maplume.blockwise.core.domain.model.TimelineViewMode
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeline.DayGroup
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeline.TimelineItem
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TimelineEmptyPastDayUntrackedGapTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyPastDayShowsFullDayGapCardAndNoEmptyState() {
        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        val pastDate = today.minus(1, DateTimeUnit.DAY)

        val start = pastDate.atTime(LocalTime(0, 0)).toInstant(tz)
        val end = pastDate.plus(1, DateTimeUnit.DAY).atTime(LocalTime(0, 0)).toInstant(tz)

        var capturedStart: Instant? = null
        var capturedEnd: Instant? = null

        val dayGroup = DayGroup(
            date = pastDate,
            items = listOf(TimelineItem.UntrackedGap(startTime = start, endTime = end)),
            totalMinutes = 0
        )

        composeTestRule.setContent {
            BlockwiseTheme {
                TimelineScreenContent(
                    uiState = TimelineUiState(
                        selectedDate = pastDate,
                        dayGroups = listOf(dayGroup),
                        isLoading = false
                    ),
                    snackbarHostState = androidx.compose.material3.SnackbarHostState(),
                    viewMode = TimelineViewMode.LIST,
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
                    onCreateFromGap = { s, e ->
                        capturedStart = s
                        capturedEnd = e
                    },
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

        var emptyStateVisible = true
        try {
            composeTestRule.onNodeWithText("暂无时间记录").assertIsDisplayed()
        } catch (_: AssertionError) {
            emptyStateVisible = false
        }
        assertFalse(emptyStateVisible)

        composeTestRule.onNodeWithTag("untrackedGapCard-${start.toEpochMilliseconds()}-${end.toEpochMilliseconds()}")
            .assertIsDisplayed()
            .performClick()

        composeTestRule.runOnIdle {
            assertEquals(start, capturedStart)
            assertEquals(end, capturedEnd)
        }
    }
}
