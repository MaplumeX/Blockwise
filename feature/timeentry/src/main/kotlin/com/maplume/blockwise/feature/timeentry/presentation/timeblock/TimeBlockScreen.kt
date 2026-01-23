package com.maplume.blockwise.feature.timeentry.presentation.timeblock

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.CalendarViewDay
import androidx.compose.material.icons.filled.CalendarViewWeek
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maplume.blockwise.core.designsystem.component.BlockwiseDatePickerDialog
import com.maplume.blockwise.core.designsystem.component.LoadingIndicator
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme
import com.maplume.blockwise.core.domain.model.ActivityType
import com.maplume.blockwise.core.domain.model.TimeEntry
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import java.time.format.TextStyle
import java.util.Locale
import kotlin.time.Duration.Companion.hours

/**
 * Time block screen showing day/week view of time entries.
 */
@Composable
fun TimeBlockScreen(
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToCreate: (LocalDate, LocalTime?) -> Unit,
    onNavigateToCreateFromRange: (startTimeMillis: Long, endTimeMillis: Long) -> Unit = { _, _ -> },
    modifier: Modifier = Modifier,
    viewModel: TimeBlockViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TimeBlockEvent.NavigateToEdit -> onNavigateToEdit(event.entryId)
                is TimeBlockEvent.NavigateToCreate -> onNavigateToCreate(event.date, event.time)
                is TimeBlockEvent.NavigateToCreateRange -> {
                    onNavigateToCreateFromRange(event.startTimeMillis, event.endTimeMillis)
                }
                is TimeBlockEvent.Error -> snackbarHostState.showSnackbar(event.message)
                is TimeBlockEvent.DeleteSuccess -> snackbarHostState.showSnackbar("删除成功")
            }
        }
    }

    TimeBlockScreenContent(
        uiState = uiState,
        snackbarHostState = snackbarHostState,
        onNavigatePrevious = viewModel::navigatePrevious,
        onNavigateNext = viewModel::navigateNext,
        onNavigateToToday = viewModel::navigateToToday,
        onDateSelect = viewModel::selectDate,
        onShowDatePicker = viewModel::showDatePicker,
        onHideDatePicker = viewModel::hideDatePicker,
        onViewModeChange = viewModel::setViewMode,
        onEntryClick = viewModel::onEntryClick,
        onEntryLongClick = viewModel::onDeleteRequest,
        onEmptySlotClick = viewModel::onEmptySlotClick,
        onEmptyRangeCreate = viewModel::onEmptyRangeCreate,
        onDeleteConfirm = viewModel::onDeleteConfirm,
        onDeleteCancel = viewModel::onDeleteCancel,
        onEditEntry = viewModel::onSelectedEntryEdit,
        onClearSelection = viewModel::clearSelection,
        onDeleteSelected = viewModel::onSelectedEntryDeleteRequest,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeBlockScreenContent(
    uiState: TimeBlockUiState,
    snackbarHostState: SnackbarHostState,
    onNavigatePrevious: () -> Unit,
    onNavigateNext: () -> Unit,
    onNavigateToToday: () -> Unit,
    onDateSelect: (LocalDate) -> Unit,
    onShowDatePicker: () -> Unit,
    onHideDatePicker: () -> Unit,
    onViewModeChange: (TimeBlockViewMode) -> Unit,
    onEntryClick: (TimeEntry) -> Unit,
    onEntryLongClick: (TimeEntry) -> Unit,
    onEmptySlotClick: (LocalDate, LocalTime) -> Unit,
    onEmptyRangeCreate: (LocalDate, LocalTime, LocalTime) -> Unit,
    onDeleteConfirm: () -> Unit,
    onDeleteCancel: () -> Unit,
    onEditEntry: () -> Unit,
    onClearSelection: () -> Unit,
    onDeleteSelected: () -> Unit,
    modifier: Modifier = Modifier
) {
    val selectedEntry = remember(uiState.selectedEntryId, uiState.entriesByDay) {
        uiState.entriesByDay.values.flatten().find { it.id == uiState.selectedEntryId }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TimeBlockTopBar(
                selectedDate = uiState.selectedDate,
                viewMode = uiState.viewMode,
                weekStartDate = uiState.weekStartDate,
                onNavigatePrevious = onNavigatePrevious,
                onNavigateNext = onNavigateNext,
                onNavigateToToday = onNavigateToToday,
                onShowDatePicker = onShowDatePicker,
                onViewModeChange = onViewModeChange
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                // Statistics summary
                StatisticsSummary(
                    totalMinutes = uiState.totalMinutes,
                    entryCount = uiState.entryCount,
                    viewMode = uiState.viewMode,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // Main content
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        LoadingIndicator()
                    }
                } else {
                    AnimatedContent(
                        targetState = uiState.viewMode,
                        transitionSpec = {
                            if (targetState == TimeBlockViewMode.WEEK) {
                                slideInHorizontally { it } + fadeIn() togetherWith
                                    slideOutHorizontally { -it } + fadeOut()
                            } else {
                                slideInHorizontally { -it } + fadeIn() togetherWith
                                    slideOutHorizontally { it } + fadeOut()
                            }
                        },
                        label = "ViewModeTransition"
                    ) { viewMode ->
                        when (viewMode) {
                            TimeBlockViewMode.DAY -> {
                                TimeBlockDayView(
                                    date = uiState.selectedDate,
                                    entries = uiState.selectedDayEntries,
                                    selectedEntryId = uiState.selectedEntryId,
                                    onEntryClick = onEntryClick,
                                    onEntryLongClick = onEntryLongClick,
                                    onEmptySlotClick = { time ->
                                        onEmptySlotClick(uiState.selectedDate, time)
                                    },
                                    onEmptyRangeCreate = { startTime, endTime ->
                                        onEmptyRangeCreate(uiState.selectedDate, startTime, endTime)
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                            TimeBlockViewMode.WEEK -> {
                                TimeBlockWeekView(
                                    weekStart = uiState.weekStartDate,
                                    entriesByDay = uiState.entriesByDay,
                                    onEntryClick = onEntryClick,
                                    onEntryLongClick = onEntryLongClick,
                                    onEmptySlotClick = onEmptySlotClick,
                                    onDayHeaderClick = { date ->
                                        onDateSelect(date)
                                        onViewModeChange(TimeBlockViewMode.DAY)
                                    },
                                    modifier = Modifier.fillMaxSize()
                                )
                            }
                        }
                    }
                }
            }

            if (selectedEntry != null) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.3f))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null
                        ) {
                            onClearSelection()
                        }
                )
            }

            AnimatedVisibility(
                visible = selectedEntry != null,
                enter = slideInVertically { it } + fadeIn(),
                exit = slideOutVertically { it } + fadeOut(),
                modifier = Modifier.align(Alignment.BottomCenter)
            ) {
                if (selectedEntry != null) {
                    TimeEntryDetailPanel(
                        entry = selectedEntry,
                        onEditClick = onEditEntry,
                        onDeleteClick = onDeleteSelected,
                        onDismiss = onClearSelection
                    )
                }
            }
        }

        // Date picker dialog
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
    }
}

/**
 * Top app bar with navigation and view mode controls.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimeBlockTopBar(
    selectedDate: LocalDate,
    viewMode: TimeBlockViewMode,
    weekStartDate: LocalDate,
    onNavigatePrevious: () -> Unit,
    onNavigateNext: () -> Unit,
    onNavigateToToday: () -> Unit,
    onShowDatePicker: () -> Unit,
    onViewModeChange: (TimeBlockViewMode) -> Unit
) {
    TopAppBar(
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Navigation buttons
                IconButton(onClick = onNavigatePrevious) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "上一${if (viewMode == TimeBlockViewMode.DAY) "天" else "周"}"
                    )
                }

                // Date display
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = formatDateTitle(selectedDate, viewMode, weekStartDate),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    if (viewMode == TimeBlockViewMode.DAY) {
                        Text(
                            text = formatDayOfWeek(selectedDate),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                IconButton(onClick = onNavigateNext) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "下一${if (viewMode == TimeBlockViewMode.DAY) "天" else "周"}"
                    )
                }
            }
        },
        actions = {
            // Today button
            IconButton(onClick = onNavigateToToday) {
                Icon(
                    imageVector = Icons.Default.Today,
                    contentDescription = "今天"
                )
            }

            // Calendar button
            IconButton(onClick = onShowDatePicker) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = "选择日期"
                )
            }

            // View mode toggle
            IconButton(
                onClick = {
                    val newMode = if (viewMode == TimeBlockViewMode.DAY) {
                        TimeBlockViewMode.WEEK
                    } else {
                        TimeBlockViewMode.DAY
                    }
                    onViewModeChange(newMode)
                }
            ) {
                Icon(
                    imageVector = if (viewMode == TimeBlockViewMode.DAY) {
                        Icons.Default.CalendarViewWeek
                    } else {
                        Icons.Default.CalendarViewDay
                    },
                    contentDescription = if (viewMode == TimeBlockViewMode.DAY) "周视图" else "日视图"
                )
            }
        }
    )
}

/**
 * Statistics summary bar.
 */
@Composable
private fun StatisticsSummary(
    totalMinutes: Int,
    entryCount: Int,
    viewMode: TimeBlockViewMode,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Total duration
        Column {
            Text(
                text = if (viewMode == TimeBlockViewMode.DAY) "今日时长" else "本周时长",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatDuration(totalMinutes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        // Entry count
        Column(
            horizontalAlignment = Alignment.End
        ) {
            Text(
                text = "记录数",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = "$entryCount 条",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
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
 * Format date title based on view mode.
 */
private fun formatDateTitle(
    selectedDate: LocalDate,
    viewMode: TimeBlockViewMode,
    weekStartDate: LocalDate
): String {
    return when (viewMode) {
        TimeBlockViewMode.DAY -> {
            "${selectedDate.year}年${selectedDate.monthNumber}月${selectedDate.dayOfMonth}日"
        }
        TimeBlockViewMode.WEEK -> {
            val weekEnd = weekStartDate.plus(6, DateTimeUnit.DAY)
            if (weekStartDate.monthNumber == weekEnd.monthNumber) {
                "${weekStartDate.year}年${weekStartDate.monthNumber}月${weekStartDate.dayOfMonth}-${weekEnd.dayOfMonth}日"
            } else {
                "${weekStartDate.monthNumber}月${weekStartDate.dayOfMonth}日-${weekEnd.monthNumber}月${weekEnd.dayOfMonth}日"
            }
        }
    }
}

/**
 * Format day of week.
 */
private fun formatDayOfWeek(date: LocalDate): String {
    return date.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.getDefault())
}

/**
 * Format duration in hours and minutes.
 */
private fun formatDuration(totalMinutes: Int): String {
    val hours = totalMinutes / 60
    val minutes = totalMinutes % 60
    return when {
        hours > 0 && minutes > 0 -> "${hours}小时${minutes}分钟"
        hours > 0 -> "${hours}小时"
        minutes > 0 -> "${minutes}分钟"
        else -> "0分钟"
    }
}

/**
 * Detail panel for selected time entry.
 */
@Composable
private fun TimeEntryDetailPanel(
    entry: TimeEntry,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onDismiss: () -> Unit
) {
    Surface(
        shape = androidx.compose.foundation.shape.RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 8.dp,
        shadowElevation = 8.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header: Activity Name + Close
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(parseColor(entry.activity.colorHex), CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = entry.activity.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Time & Duration
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Schedule,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${formatTimeRange(entry)} (${formatDuration(entry.durationMinutes)})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // Note
            if (!entry.note.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(verticalAlignment = Alignment.Top) {
                    Icon(
                        imageVector = Icons.Default.Notes,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = entry.note!!,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Tags
            if (entry.tags.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    entry.tags.forEach { tag ->
                        SuggestionChip(
                            onClick = {},
                            label = { Text(tag.name) },
                            icon = {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .background(parseColor(tag.colorHex), CircleShape)
                                )
                            },
                            colors = androidx.compose.material3.SuggestionChipDefaults.suggestionChipColors(
                                containerColor = parseColor(tag.colorHex).copy(alpha = 0.1f),
                                labelColor = MaterialTheme.colorScheme.onSurface
                            ),
                            border = null
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Actions
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                OutlinedButton(
                    onClick = onDeleteClick,
                    modifier = Modifier.weight(1f),
                    colors = androidx.compose.material3.ButtonDefaults.outlinedButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("删除")
                }
                Button(
                    onClick = onEditClick,
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("编辑")
                }
            }
        }
    }
}

private fun formatTimeRange(entry: TimeEntry): String {
    val tz = TimeZone.currentSystemDefault()
    val startLocal = entry.startTime.toLocalDateTime(tz)
    val endLocal = entry.endTime.toLocalDateTime(tz)

    val isCrossDay = startLocal.date != endLocal.date

    val startTimeStr = String.format("%02d:%02d", startLocal.hour, startLocal.minute)
    val startStr = if (isCrossDay) {
        "${startLocal.date.monthNumber}/${startLocal.date.dayOfMonth} $startTimeStr"
    } else {
        startTimeStr
    }

    val endTimeStr = if (
        endLocal.hour == 0 &&
            endLocal.minute == 0 &&
            endLocal.date.toEpochDays() == startLocal.date.toEpochDays() + 1
    ) {
        "24:00"
    } else {
        String.format("%02d:%02d", endLocal.hour, endLocal.minute)
    }

    val endStr = if (isCrossDay) {
        "${endLocal.date.monthNumber}/${endLocal.date.dayOfMonth} $endTimeStr"
    } else {
        endTimeStr
    }

    return "$startStr - $endStr"
}

private fun parseColor(hex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex))
    } catch (e: Exception) {
        Color.Gray
    }
}

// =============================================================================
// Previews
// =============================================================================

@Preview(showBackground = true)
@Composable
private fun TimeBlockScreenDayViewPreview() {
    val now = Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date

    BlockwiseTheme {
        TimeBlockScreenContent(
            uiState = TimeBlockUiState(
                selectedDate = today,
                viewMode = TimeBlockViewMode.DAY,
                entriesByDay = mapOf(
                    today to listOf(
                        TimeEntry(
                            id = 1,
                            activity = ActivityType(1, "工作", "#4CAF50", null, null, 0, false),
                            startTime = now - 4.hours,
                            endTime = now - 2.hours,
                            durationMinutes = 120,
                            note = null,
                            tags = emptyList()
                        ),
                        TimeEntry(
                            id = 2,
                            activity = ActivityType(2, "学习", "#2196F3", null, null, 1, false),
                            startTime = now - 2.hours,
                            endTime = now,
                            durationMinutes = 120,
                            note = null,
                            tags = emptyList()
                        )
                    )
                ),
                isLoading = false,
                totalMinutes = 240,
                entryCount = 2
            ),
            snackbarHostState = SnackbarHostState(),
            onNavigatePrevious = {},
            onNavigateNext = {},
            onNavigateToToday = {},
            onDateSelect = {},
            onShowDatePicker = {},
            onHideDatePicker = {},
            onViewModeChange = {},
            onEntryClick = {},
            onEntryLongClick = {},
            onEmptySlotClick = { _, _ -> },
            onEmptyRangeCreate = { _, _, _ -> },
            onDeleteConfirm = {},
            onDeleteCancel = {},
            onEditEntry = {},
            onClearSelection = {},
            onDeleteSelected = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TimeBlockScreenWeekViewPreview() {
    val now = Clock.System.now()
    val today = now.toLocalDateTime(TimeZone.currentSystemDefault()).date

    BlockwiseTheme {
        TimeBlockScreenContent(
            uiState = TimeBlockUiState(
                selectedDate = today,
                viewMode = TimeBlockViewMode.WEEK,
                entriesByDay = mapOf(
                    today to listOf(
                        TimeEntry(
                            id = 1,
                            activity = ActivityType(1, "工作", "#4CAF50", null, null, 0, false),
                            startTime = now - 4.hours,
                            endTime = now - 2.hours,
                            durationMinutes = 120,
                            note = null,
                            tags = emptyList()
                        )
                    )
                ),
                isLoading = false,
                totalMinutes = 120,
                entryCount = 1
            ),
            snackbarHostState = SnackbarHostState(),
            onNavigatePrevious = {},
            onNavigateNext = {},
            onNavigateToToday = {},
            onDateSelect = {},
            onShowDatePicker = {},
            onHideDatePicker = {},
            onViewModeChange = {},
            onEntryClick = {},
            onEntryLongClick = {},
            onEmptySlotClick = { _, _ -> },
            onEmptyRangeCreate = { _, _, _ -> },
            onDeleteConfirm = {},
            onDeleteCancel = {},
            onEditEntry = {},
            onClearSelection = {},
            onDeleteSelected = {}
        )
    }
}
