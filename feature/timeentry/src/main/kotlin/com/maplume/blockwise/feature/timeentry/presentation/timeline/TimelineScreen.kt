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
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.filled.Today
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState

import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
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
                is TimelineEvent.DeleteSuccess -> snackbarHostState.showSnackbar("删除成功")
                is TimelineEvent.SplitSuccess -> snackbarHostState.showSnackbar("拆分成功")
                is TimelineEvent.MergeSuccess -> snackbarHostState.showSnackbar("合并成功")
            }
        }
    }

    TimelineScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onRefresh = viewModel::refresh,
        onEntryClick = viewModel::onEntryClick,
        onEntryLongPress = viewModel::onEntryLongPress,
        onExitSelectionMode = viewModel::exitSelectionMode,
        onDismissContextMenu = viewModel::dismissContextMenu,
        onContextMenuEdit = viewModel::onContextMenuEdit,
        onContextMenuDelete = viewModel::onContextMenuDelete,
        onContextMenuSplit = viewModel::onContextMenuSplit,
        onDeleteConfirm = viewModel::onDeleteConfirm,
        onDeleteCancel = viewModel::onDeleteCancel,
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
    onRefresh: () -> Unit,
    onEntryClick: (TimeEntry, Offset) -> Unit,
    onEntryLongPress: (TimeEntry) -> Unit,
    onExitSelectionMode: () -> Unit,
    onDismissContextMenu: () -> Unit,
    onContextMenuEdit: (Long) -> Unit,
    onContextMenuDelete: (TimeEntry) -> Unit,
    onContextMenuSplit: (TimeEntry) -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
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
                    onTitleClick = onShowDatePicker,
                    onTodayClick = onNavigateToToday,
                    onPreviousWeek = { onNavigateWeek(-1) },
                    onNextWeek = { onNavigateWeek(1) }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            // Show merge FAB when in selection mode with 2+ items
            AnimatedVisibility(
                visible = uiState.isSelectionMode && uiState.selectedEntryIds.size >= 2,
                enter = fadeIn() + slideInVertically { it },
                exit = fadeOut() + slideOutVertically { it }
            ) {
                FloatingActionButton(
                    onClick = onMergeRequest,
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Icon(
                        imageVector = Icons.Default.MergeType,
                        contentDescription = "合并"
                    )
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
                    .pointerInput(Unit) {
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
                                                isContextMenuVisible = uiState.contextMenu?.entryId == entry.id,
                                                onDismissContextMenu = onDismissContextMenu,
                                                onEditClick = { onContextMenuEdit(entry.id) },
                                                onDeleteClick = { onContextMenuDelete(entry) },
                                                onSplitClick = { onContextMenuSplit(entry) },
                                                onClick = { tapOffset -> onEntryClick(entry, tapOffset) },
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

        // Delete confirmation dialog
        if (uiState.entryToDelete != null) {
            DeleteConfirmationDialog(
                entry = uiState.entryToDelete,
                onConfirm = onDeleteConfirm,
                onDismiss = onDeleteCancel
            )
        }

        // Split dialog
        if (uiState.entryToSplit != null) {
            SplitTimeEntryDialog(
                entry = uiState.entryToSplit,
                onConfirm = onSplitConfirm,
                onDismiss = onSplitCancel
            )
        }

        // Merge confirmation dialog
        if (uiState.showMergeConfirmation) {
            MergeConfirmationDialog(
                selectedCount = uiState.selectedEntryIds.size,
                onConfirm = onMergeConfirm,
                onDismiss = onMergeCancel
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimelineDateTopBar(
    weekStartDate: LocalDate,
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
 * Delete confirmation dialog.
 */
@Composable
private fun DeleteConfirmationDialog(
    entry: TimeEntry,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("删除记录") },
        text = {
            Text("确定要删除「${entry.activity.name}」的记录吗？此操作无法撤销。")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("删除", color = MaterialTheme.colorScheme.error)
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
            onRefresh = {},
            onEntryClick = { _, _ -> },
            onEntryLongPress = {},
            onExitSelectionMode = {},
            onDismissContextMenu = {},
            onContextMenuEdit = {},
            onContextMenuDelete = {},
            onContextMenuSplit = {},
            onDeleteConfirm = {},
            onDeleteCancel = {},
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
            onRefresh = {},
            onEntryClick = { _, _ -> },
            onEntryLongPress = {},
            onExitSelectionMode = {},
            onDismissContextMenu = {},
            onContextMenuEdit = {},
            onContextMenuDelete = {},
            onContextMenuSplit = {},
            onDeleteConfirm = {},
            onDeleteCancel = {},
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
