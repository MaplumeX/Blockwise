package com.maplume.blockwise.feature.timeentry.presentation.timeline

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.Timeline
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.Slider
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.maplume.blockwise.core.domain.model.TimelineViewMode
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maplume.blockwise.core.designsystem.component.BlockwiseDatePickerDialog
import com.maplume.blockwise.core.designsystem.component.BlockwiseEmptyState
import com.maplume.blockwise.core.designsystem.component.LoadingIndicator
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme
import com.maplume.blockwise.core.domain.model.ActivityType
import com.maplume.blockwise.core.domain.model.TimeEntry
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeline.DayGroup
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeline.TimelineItem
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeline.createDayGroup
import com.maplume.blockwise.feature.timeentry.presentation.timeblock.TimeBlockDayView
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.time.format.TextStyle
import java.util.Locale
import kotlin.time.Duration.Companion.hours

/**
 * Timeline screen showing time entries grouped by date.
 */
@Composable
fun TimelineScreen(
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToCreateFromGap: (startTime: Instant, endTime: Instant) -> Unit,
    viewMode: TimelineViewMode = TimelineViewMode.LIST,
    onViewModeChange: (TimelineViewMode) -> Unit = {},
    modifier: Modifier = Modifier,
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
             when (event) {
                 is TimelineEvent.NavigateToEdit -> onNavigateToEdit(event.entryId)
                 is TimelineEvent.Error -> snackbarHostState.showSnackbar(event.message)
                 is TimelineEvent.SplitSuccess -> snackbarHostState.showSnackbar("拆分成功")
                 is TimelineEvent.MergeSuccess -> snackbarHostState.showSnackbar("合并成功")
                 is TimelineEvent.SaveSuccess -> snackbarHostState.showSnackbar("保存成功")
                 is TimelineEvent.ShowDeleteUndo -> {
                     val result = snackbarHostState.showSnackbar(
                         message = event.message,
                         actionLabel = event.actionLabel
                     )
                     if (result == androidx.compose.material3.SnackbarResult.ActionPerformed) {
                         viewModel.onDeleteUndo(event.token)
                     } else {
                         viewModel.onDeleteCommit(event.token)
                     }
                 }
             }
         }
     }


    TimelineScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        viewMode = viewMode,
        onViewModeChange = onViewModeChange,
        onRefresh = viewModel::refresh,
        onEntryClick = viewModel::onEntryClick,
        onTimeBlockEntryClick = {},
        onClearTimeBlockSelection = {},
        onEntryLongPress = viewModel::onEntryLongPress,
        onExitSelectionMode = viewModel::exitSelectionMode,
        onDismissEntrySheet = viewModel::dismissEntrySheet,
        onSaveEntryDraft = viewModel::onSaveDraft,
        onDraftStartTimeChange = viewModel::onDraftStartTimeChange,
        onDraftEndTimeChange = viewModel::onDraftEndTimeChange,
        onDraftActivitySelect = viewModel::onDraftActivitySelect,
        onDraftTagToggle = viewModel::onDraftTagToggle,
        onDraftNoteChange = viewModel::onDraftNoteChange,
        onMergeUp = viewModel::onMergeUp,
        onMergeDown = viewModel::onMergeDown,
        onDeleteFromSheet = viewModel::onDeleteFromSheet,
        onSplitFromSheet = viewModel::onSplitFromSheet,
        onBatchDelete = viewModel::onBatchDeleteRequest,
        onSplitConfirm = viewModel::onSplitConfirm,
        onSplitCancel = viewModel::onSplitCancel,
        onMergeRequest = viewModel::onMergeRequest,
        onMergeConfirm = viewModel::onMergeConfirm,
        onMergeCancel = viewModel::onMergeCancel,
        onCreateFromGap = onNavigateToCreateFromGap,
        onNavigateWeek = viewModel::navigateWeek,
        onNavigateToToday = viewModel::navigateToToday,
        onDateSelect = viewModel::setSelectedDate,
        onShowDatePicker = viewModel::showDatePicker,
        onHideDatePicker = viewModel::hideDatePicker,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun TimelineScreenContent(
    uiState: TimelineUiState,
    snackbarHostState: SnackbarHostState,
    viewMode: TimelineViewMode,
    onViewModeChange: (TimelineViewMode) -> Unit,
    onRefresh: () -> Unit,
    onEntryClick: (TimeEntry) -> Unit,
    onTimeBlockEntryClick: (TimeEntry) -> Unit,
    onClearTimeBlockSelection: () -> Unit,
    onEntryLongPress: (TimeEntry) -> Unit,
    onExitSelectionMode: () -> Unit,
    onDismissEntrySheet: () -> Unit,
    onSaveEntryDraft: () -> Unit,
    onDraftStartTimeChange: (LocalTime) -> Unit,
    onDraftEndTimeChange: (LocalTime) -> Unit,
    onDraftActivitySelect: (Long) -> Unit,
    onDraftTagToggle: (Long) -> Unit,
    onDraftNoteChange: (String) -> Unit,
    onMergeUp: () -> Unit,
    onMergeDown: () -> Unit,
    onDeleteFromSheet: () -> Unit,
    onSplitFromSheet: () -> Unit,
    onBatchDelete: () -> Unit,
    onSplitConfirm: (Instant) -> Unit,
    onSplitCancel: () -> Unit,
    onMergeRequest: () -> Unit,
    onMergeConfirm: () -> Unit,
    onMergeCancel: () -> Unit,
    onCreateFromGap: (startTime: Instant, endTime: Instant) -> Unit,
    onNavigateWeek: (Int) -> Unit,
    onNavigateToToday: () -> Unit,
    onDateSelect: (LocalDate) -> Unit,
    onShowDatePicker: () -> Unit,
    onHideDatePicker: () -> Unit,
    modifier: Modifier = Modifier
) { 
    val onCreateFromGapSafe = onCreateFromGap
    val listState = rememberLazyListState()

    Scaffold(
        modifier = modifier,
        topBar = {
            if (uiState.isSelectionMode) {
                SelectionModeTopBar(
                    selectedCount = uiState.selectedEntryIds.size,
                    onClose = onExitSelectionMode,
                    onMerge = onMergeRequest
                )
            } else {
                TimelineDateTopBar(
                    weekStartDate = uiState.weekStartDate,
                    viewMode = viewMode,
                    onViewModeChange = onViewModeChange,
                    onTitleClick = onShowDatePicker,
                    onTodayClick = onNavigateToToday,
                    onPreviousWeek = { onNavigateWeek(-1) },
                    onNextWeek = { onNavigateWeek(1) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            if (uiState.isSelectionMode) {
                Surface(tonalElevation = 2.dp) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Button(
                            onClick = onBatchDelete,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
                        ) {
                            Icon(Icons.Default.Delete, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("删除")
                        }

                        Button(
                            onClick = onMergeRequest
                        ) {
                            Icon(Icons.Default.MergeType, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("合并")
                        }
                    }
                }
            }
        }
    ) { paddingValues ->
        Column(modifier = Modifier.padding(paddingValues)) {
            if (!uiState.isSelectionMode) {
                WeekStrip(
                    weekStartDate = uiState.weekStartDate,
                    selectedDate = uiState.selectedDate,
                    onDateSelect = onDateSelect
                )
            }

            AnimatedContent(
                targetState = uiState.weekStartDate,
                transitionSpec = {
                    if (targetState > initialState) {
                        slideInHorizontally { it } + fadeIn() togetherWith
                            slideOutHorizontally { -it } + fadeOut()
                    } else {
                        slideInHorizontally { -it } + fadeIn() togetherWith
                            slideOutHorizontally { it } + fadeOut()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (viewMode == TimelineViewMode.LIST) {
                            Modifier.pointerInput(Unit) {
                                detectHorizontalDragGestures(
                                    onDragEnd = { /* Handled by updates */ }
                                ) { change, dragAmount ->
                                    change.consume()
                                    if (dragAmount > 50) {
                                        onNavigateWeek(-1)
                                    } else if (dragAmount < -50) {
                                        onNavigateWeek(1)
                                    }
                                }
                            }
                        } else {
                            Modifier
                        }
                    )
            ) { _ ->
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator()
                    }
                } else if (uiState.dayGroups.isEmpty()) {
                    EmptyTimelineContent(
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    PullToRefreshBox(
                        isRefreshing = false,
                        onRefresh = onRefresh,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        if (viewMode == TimelineViewMode.LIST) {
                            LazyColumn(
                                state = listState,
                                contentPadding = PaddingValues(16.dp),
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {
                                uiState.dayGroups.forEach { dayGroup ->
                                    stickyHeader(key = "header_${dayGroup.date}") {
                                        StickyDateGroupHeader(
                                            date = dayGroup.date,
                                            totalMinutes = dayGroup.totalMinutes
                                        )
                                    }

                                    items(
                                        items = dayGroup.items,
                                        key = { item ->
                                            when (item) {
                                                is TimelineItem.Entry -> "entry-${item.entry.id}"
                                                is TimelineItem.UntrackedGap -> "gap-${item.startTime.toEpochMilliseconds()}-${item.endTime.toEpochMilliseconds()}"
                                            }
                                        }
                                    ) { item ->
                                        when (item) {
                                            is TimelineItem.Entry -> {
                                                val entry = item.entry
                                                TimeEntryItem(
                                                    entry = entry,
                                                    isSelected = entry.id in uiState.selectedEntryIds,
                                                    isSelectionMode = uiState.isSelectionMode,
                                                    onClick = { _ -> onEntryClick(entry) },
                                                    onLongClick = { onEntryLongPress(entry) },
                                                    modifier = Modifier.animateItem()
                                                )
                                            }
                                            is TimelineItem.UntrackedGap -> {
                                                UntrackedGapItem(
                                                    startTime = item.startTime,
                                                    endTime = item.endTime,
                                                    onClick = {
                                                        onCreateFromGapSafe(item.startTime, item.endTime)
                                                    },
                                                    modifier = Modifier.animateItem()
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        } else {
                            val dayGroup = uiState.dayGroups.find { it.date == uiState.selectedDate }
                            val entries = dayGroup?.items?.mapNotNull { (it as? TimelineItem.Entry)?.entry } ?: emptyList()

                            TimeBlockDayView(
                                date = uiState.selectedDate,
                                entries = entries,
                                selectedEntryId = null,
                                onEntryClick = onTimeBlockEntryClick,
                                onEntryLongClick = onEntryLongPress,
                                onEmptySlotClick = {},
                                onEmptyRangeCreate = { startTime, endTime ->
                                    val tz = TimeZone.currentSystemDefault()
                                    val start = LocalDateTime(uiState.selectedDate, startTime).toInstant(tz)
                                    var end = LocalDateTime(uiState.selectedDate, endTime).toInstant(tz)
                                    if (end <= start) {
                                        end = end.plus(1, DateTimeUnit.DAY, tz)
                                    }
                                    onCreateFromGapSafe(start, end)
                                },
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                }
            }
        }

        // Date Picker
        BlockwiseDatePickerDialog(
            visible = uiState.showDatePicker,
            onDismiss = onHideDatePicker,
            onDateSelected = onDateSelect,
            initialDate = uiState.selectedDate
        )


        // Split dialog
        if (uiState.entryToSplit != null) {
            SplitTimeEntryDialog(
                entry = uiState.entryToSplit,
                onConfirm = onSplitConfirm,
                onDismiss = onSplitCancel
            )
        }

        if (uiState.showMergeConfirmation) {
            MergeConfirmationDialog(
                selectedCount = uiState.selectedEntryIds.size,
                onConfirm = onMergeConfirm,
                onDismiss = onMergeCancel
            )
        }

        val draft = uiState.sheetDraft
        if (draft != null) {
            TimelineEntryBottomSheet(
                draft = draft,
                activityTypes = uiState.activityTypes,
                availableTags = uiState.availableTags,
                canMergeUp = draft.adjacentUpEntryId != null,
                canMergeDown = draft.adjacentDownEntryId != null,
                onDismiss = onDismissEntrySheet,
                onStartTimeChange = onDraftStartTimeChange,
                onEndTimeChange = onDraftEndTimeChange,
                onActivitySelect = onDraftActivitySelect,
                onTagToggle = onDraftTagToggle,
                onNoteChange = onDraftNoteChange,
                onMergeUp = onMergeUp,
                onMergeDown = onMergeDown,
                onDelete = onDeleteFromSheet,
                onSave = onSaveEntryDraft,
                onSplit = onSplitFromSheet
            )
        }

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimelineDateTopBar(
    weekStartDate: LocalDate,
    viewMode: TimelineViewMode,
    onViewModeChange: (TimelineViewMode) -> Unit,
    onTitleClick: () -> Unit,
    onTodayClick: () -> Unit,
    onPreviousWeek: () -> Unit,
    onNextWeek: () -> Unit
) {
    val weekEndDate = weekStartDate.plus(6, DateTimeUnit.DAY)
    val title = if (weekStartDate.monthNumber == weekEndDate.monthNumber) {
        "${weekStartDate.year}年${weekStartDate.monthNumber}月${weekStartDate.dayOfMonth}-${weekEndDate.dayOfMonth}日"
    } else {
        "${weekStartDate.monthNumber}月${weekStartDate.dayOfMonth}日-${weekEndDate.monthNumber}月${weekEndDate.dayOfMonth}日"
    }

    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .clickable(onClick = onTitleClick)
                    .padding(8.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        },
        navigationIcon = {
             IconButton(onClick = onPreviousWeek) {
                 Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "上一周")
             }
        },
        actions = {
            IconButton(onClick = onNextWeek) {
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "下一周")
            }
            IconButton(onClick = onTodayClick) {
                Icon(Icons.Default.Today, contentDescription = "今天")
            }
            IconButton(
                onClick = {
                    val nextMode = if (viewMode == TimelineViewMode.LIST) {
                        TimelineViewMode.TIME_BLOCK
                    } else {
                        TimelineViewMode.LIST
                    }
                    onViewModeChange(nextMode)
                }
            ) {
                Icon(
                    imageVector = if (viewMode == TimelineViewMode.LIST) {
                        Icons.Default.CalendarViewDay
                    } else {
                        Icons.Default.Timeline
                    },
                    contentDescription = if (viewMode == TimelineViewMode.LIST) "时间块" else "时间线"
                )
            }
        }
    )
}

@Composable
private fun WeekStrip(
    weekStartDate: LocalDate,
    selectedDate: LocalDate,
    onDateSelect: (LocalDate) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        (0..6).forEach { dayOffset ->
            val date = weekStartDate.plus(dayOffset, DateTimeUnit.DAY)
            val isSelected = date == selectedDate
            val isToday = date == Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date

            WeekDayItem(
                date = date,
                isSelected = isSelected,
                isToday = isToday,
                onClick = { onDateSelect(date) }
            )
        }
    }
}

@Composable
private fun WeekDayItem(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val backgroundColor = if (isSelected) MaterialTheme.colorScheme.primary else androidx.compose.ui.graphics.Color.Transparent
    val contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
    val todayIndicatorColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.primary

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp)
            .width(40.dp)
    ) {
        Text(
            text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
            style = MaterialTheme.typography.labelSmall,
            color = contentColor.copy(alpha = 0.8f)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = contentColor
        )
        if (isToday) {
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(todayIndicatorColor, CircleShape)
            )
        } else {
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}


/**
 * Top bar for selection mode.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectionModeTopBar(
    selectedCount: Int,
    onClose: () -> Unit,
    onMerge: () -> Unit
) {
    TopAppBar(
        title = { Text("已选择 $selectedCount 项") },
        navigationIcon = {
            IconButton(onClick = onClose) {
                Icon(Icons.Default.Close, contentDescription = "取消选择")
            }
        },
        actions = {
            if (selectedCount >= 2) {
                IconButton(onClick = onMerge) {
                    Icon(Icons.Default.MergeType, contentDescription = "合并")
                }
            }
        }
    )
}

/**
 * Empty state content.
 */
@Composable
private fun EmptyTimelineContent(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        BlockwiseEmptyState(
            title = "暂无时间记录",
            description = "开始计时或手动添加记录",
            icon = Icons.Outlined.Schedule
        )
    }
}


/**
 * Split time entry dialog.
 */
@Composable
private fun SplitTimeEntryDialog(
    entry: TimeEntry,
    onConfirm: (Instant) -> Unit,
    onDismiss: () -> Unit
) {
    val startMillis = entry.startTime.toEpochMilliseconds()
    val endMillis = entry.endTime.toEpochMilliseconds()
    val midMillis = (startMillis + endMillis) / 2

    var sliderPosition by remember { mutableFloatStateOf(0.5f) }

    val splitTime = remember(sliderPosition) {
        Instant.fromEpochMilliseconds(
            startMillis + ((endMillis - startMillis) * sliderPosition).toLong()
        )
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("拆分记录") },
        text = {
            Column {
                Text("选择拆分时间点：")
                Spacer(modifier = Modifier.height(16.dp))

                // Time display
                val tz = TimeZone.currentSystemDefault()
                val splitLocal = splitTime.toLocalDateTime(tz)
                Text(
                    text = String.format("%02d:%02d", splitLocal.hour, splitLocal.minute),
                    style = MaterialTheme.typography.headlineMedium,
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Slider
                Slider(
                    value = sliderPosition,
                    onValueChange = { sliderPosition = it },
                    valueRange = 0.05f..0.95f
                )

                // Time range labels
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val startLocal = entry.startTime.toLocalDateTime(tz)
                    val endLocal = entry.endTime.toLocalDateTime(tz)
                    Text(
                        text = String.format("%02d:%02d", startLocal.hour, startLocal.minute),
                        style = MaterialTheme.typography.bodySmall
                    )
                    Text(
                        text = String.format("%02d:%02d", endLocal.hour, endLocal.minute),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(splitTime) }) {
                Text("拆分")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

/**
 * Merge confirmation dialog.
 */
@Composable
private fun MergeConfirmationDialog(
    selectedCount: Int,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("合并记录") },
        text = {
            Text("确定要将选中的 $selectedCount 条记录合并为一条吗？合并后将保留所有标签。")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("合并")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

// =============================================================================
// Previews
// =============================================================================

@Preview(showBackground = true)
@Composable
private fun TimelineScreenPreview() {
    val now = Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date

    BlockwiseTheme {
        TimelineScreenContent(
            uiState = TimelineUiState(
                dayGroups = listOf(
                    createDayGroup(
                        date = today,
                        entries = listOf(
                            TimeEntry(
                                id = 1,
                                activity = ActivityType(1, "工作", "#4CAF50", null, null, 0, false),
                                startTime = now - 2.hours,
                                endTime = now,
                                durationMinutes = 120,
                                note = "完成项目文档",
                                tags = emptyList()
                            ),
                            TimeEntry(
                                id = 2,
                                activity = ActivityType(2, "学习", "#2196F3", null, null, 1, false),
                                startTime = now - 4.hours,
                                endTime = now - 2.hours,
                                durationMinutes = 120,
                                note = null,
                                tags = emptyList()
                            )
                        )
                    )
                ),
                isLoading = false
            ),
            snackbarHostState = SnackbarHostState(),
            viewMode = TimelineViewMode.LIST,
            onViewModeChange = {},
            onRefresh = {},
            onEntryClick = { _ -> },
            onTimeBlockEntryClick = {},
            onClearTimeBlockSelection = {},
            onEntryLongPress = {},
            onExitSelectionMode = {},
            onDismissEntrySheet = {},
            onSaveEntryDraft = {},
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
            onNavigateWeek = {},
            onNavigateToToday = {},
            onDateSelect = {},
            onShowDatePicker = {},
            onHideDatePicker = {}
        )
    }
}


@Preview(showBackground = true)
@Composable
private fun EmptyTimelinePreview() {
    BlockwiseTheme {
        TimelineScreenContent(
            uiState = TimelineUiState(
                dayGroups = emptyList(),
                isLoading = false
            ),
            snackbarHostState = SnackbarHostState(),
            viewMode = TimelineViewMode.LIST,
            onViewModeChange = {},
            onRefresh = {},
            onEntryClick = { _ -> },
            onTimeBlockEntryClick = {},
            onClearTimeBlockSelection = {},
            onEntryLongPress = {},
            onExitSelectionMode = {},
            onDismissEntrySheet = {},
            onSaveEntryDraft = {},
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
            onNavigateWeek = {},
            onNavigateToToday = {},
            onDateSelect = {},
            onShowDatePicker = {},
            onHideDatePicker = {}
        )
    }
}
