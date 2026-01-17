package com.maplume.blockwise.feature.timeentry.presentation.timeline

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MergeType
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
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
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maplume.blockwise.core.designsystem.component.BlockwiseEmptyState
import com.maplume.blockwise.core.designsystem.component.LoadingIndicator
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme
import com.maplume.blockwise.core.domain.model.ActivityType
import com.maplume.blockwise.core.domain.model.TimeEntry
import com.maplume.blockwise.feature.timeentry.domain.usecase.timeline.DayGroup
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours

/**
 * Timeline screen showing time entries grouped by date.
 */
@Composable
fun TimelineScreen(
    onNavigateToEdit: (Long) -> Unit,
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
        onLoadMore = viewModel::loadMore,
        onEntryClick = viewModel::onEntryClick,
        onEntryLongPress = viewModel::onEntryLongPress,
        onExitSelectionMode = viewModel::exitSelectionMode,
        onDeleteRequest = viewModel::onDeleteRequest,
        onDeleteConfirm = viewModel::onDeleteConfirm,
        onDeleteCancel = viewModel::onDeleteCancel,
        onSplitRequest = viewModel::onSplitRequest,
        onSplitConfirm = viewModel::onSplitConfirm,
        onSplitCancel = viewModel::onSplitCancel,
        onMergeRequest = viewModel::onMergeRequest,
        onMergeConfirm = viewModel::onMergeConfirm,
        onMergeCancel = viewModel::onMergeCancel,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun TimelineScreenContent(
    uiState: TimelineUiState,
    snackbarHostState: SnackbarHostState,
    onRefresh: () -> Unit,
    onLoadMore: () -> Unit,
    onEntryClick: (TimeEntry) -> Unit,
    onEntryLongPress: (TimeEntry) -> Unit,
    onExitSelectionMode: () -> Unit,
    onDeleteRequest: (TimeEntry) -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
    onSplitRequest: (TimeEntry) -> Unit,
    onSplitConfirm: (Instant) -> Unit,
    onSplitCancel: () -> Unit,
    onMergeRequest: () -> Unit,
    onMergeConfirm: () -> Unit,
    onMergeCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Detect when to load more
    val shouldLoadMore by remember {
        derivedStateOf {
            val lastVisibleItem = listState.layoutInfo.visibleItemsInfo.lastOrNull()
            lastVisibleItem != null &&
                lastVisibleItem.index >= listState.layoutInfo.totalItemsCount - 5
        }
    }

    LaunchedEffect(shouldLoadMore) {
        if (shouldLoadMore && !uiState.isLoadingMore && uiState.hasMore) {
            onLoadMore()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            if (uiState.isSelectionMode) {
                SelectionModeTopBar(
                    selectedCount = uiState.selectedEntryIds.size,
                    onClose = onExitSelectionMode,
                    onMerge = onMergeRequest
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
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                LoadingIndicator()
            }
        } else if (uiState.dayGroups.isEmpty()) {
            EmptyTimelineContent(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            )
        } else {
            PullToRefreshBox(
                isRefreshing = uiState.isLoading,
                onRefresh = onRefresh,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                LazyColumn(
                    state = listState,
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    uiState.dayGroups.forEach { dayGroup ->
                        // Sticky header for date
                        stickyHeader(key = "header_${dayGroup.date}") {
                            StickyDateGroupHeader(
                                date = dayGroup.date,
                                totalMinutes = dayGroup.totalMinutes
                            )
                        }

                        // Entries for this day
                        items(
                            items = dayGroup.entries,
                            key = { it.id }
                        ) { entry ->
                            TimeEntryItem(
                                entry = entry,
                                isSelected = entry.id in uiState.selectedEntryIds,
                                isSelectionMode = uiState.isSelectionMode,
                                onClick = { onEntryClick(entry) },
                                onLongClick = { onEntryLongPress(entry) },
                                modifier = Modifier.animateItem()
                            )
                        }
                    }

                    // Loading more indicator
                    if (uiState.isLoadingMore) {
                        item {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.padding(8.dp)
                                )
                            }
                        }
                    }
                }
            }
        }

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
                    DayGroup(
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
                        ),
                        totalMinutes = 240
                    )
                ),
                isLoading = false
            ),
            snackbarHostState = SnackbarHostState(),
            onRefresh = {},
            onLoadMore = {},
            onEntryClick = {},
            onEntryLongPress = {},
            onExitSelectionMode = {},
            onDeleteRequest = {},
            onDeleteConfirm = {},
            onDeleteCancel = {},
            onSplitRequest = {},
            onSplitConfirm = {},
            onSplitCancel = {},
            onMergeRequest = {},
            onMergeConfirm = {},
            onMergeCancel = {}
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
            onLoadMore = {},
            onEntryClick = {},
            onEntryLongPress = {},
            onExitSelectionMode = {},
            onDeleteRequest = {},
            onDeleteConfirm = {},
            onDeleteCancel = {},
            onSplitRequest = {},
            onSplitConfirm = {},
            onSplitCancel = {},
            onMergeRequest = {},
            onMergeConfirm = {},
            onMergeCancel = {}
        )
    }
}
