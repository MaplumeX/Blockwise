package com.maplume.blockwise.feature.timeentry.presentation.timeentry

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maplume.blockwise.core.designsystem.component.BlockwiseCard
import com.maplume.blockwise.core.designsystem.component.BlockwiseDatePickerDialog
import com.maplume.blockwise.core.designsystem.component.BlockwiseLoadingOverlay
import com.maplume.blockwise.core.designsystem.component.BlockwisePrimaryButton
import com.maplume.blockwise.core.designsystem.component.BlockwiseTimePickerDialog
import com.maplume.blockwise.core.designsystem.component.BlockwiseTopAppBarWithBack
import com.maplume.blockwise.core.designsystem.component.LoadingIndicator
import com.maplume.blockwise.core.designsystem.theme.Spacing
import com.maplume.blockwise.core.domain.model.ActivityType
import com.maplume.blockwise.core.domain.model.Tag
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime

/**
 * Time entry edit screen.
 * Supports both create and edit modes.
 */
@Composable
fun TimeEntryEditScreen(
    onNavigateBack: () -> Unit,
    entryId: Long? = null,
    prefilledDate: LocalDate? = null,
    prefilledTime: LocalTime? = null,
    viewModel: TimeEntryEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Apply prefilled values if provided
    LaunchedEffect(prefilledDate, prefilledTime) {
        if (prefilledDate != null) {
            viewModel.onDateChange(prefilledDate)
        }
        if (prefilledTime != null) {
            viewModel.onStartTimeChange(prefilledTime)
            // Set end time to 1 hour after start time by default
            val endHour = (prefilledTime.hour + 1) % 24
            viewModel.onEndTimeChange(LocalTime(endHour, prefilledTime.minute))
        }
    }

    // Handle one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TimeEntryEditEvent.SaveSuccess -> {
                    Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                }
                is TimeEntryEditEvent.NavigateBack -> {
                    onNavigateBack()
                }
                is TimeEntryEditEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            BlockwiseTopAppBarWithBack(
                title = if (viewModel.isEditMode) "编辑时间记录" else "新建时间记录",
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                LoadingIndicator(modifier = Modifier.padding(padding))
            } else {
                TimeEntryEditContent(
                    uiState = uiState,
                    onActivityTypeSelect = viewModel::onActivityTypeSelect,
                    onDateChange = viewModel::onDateChange,
                    onStartTimeChange = viewModel::onStartTimeChange,
                    onEndTimeChange = viewModel::onEndTimeChange,
                    onNoteChange = viewModel::onNoteChange,
                    onTagToggle = viewModel::onTagToggle,
                    onSave = viewModel::save,
                    modifier = Modifier.padding(padding)
                )
            }

            // Saving overlay
            BlockwiseLoadingOverlay(
                visible = uiState.isSaving,
                message = "保存中..."
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class, ExperimentalMaterial3Api::class)
@Composable
private fun TimeEntryEditContent(
    uiState: TimeEntryEditUiState,
    onActivityTypeSelect: (ActivityType) -> Unit,
    onDateChange: (LocalDate) -> Unit,
    onStartTimeChange: (LocalTime) -> Unit,
    onEndTimeChange: (LocalTime) -> Unit,
    onNoteChange: (String) -> Unit,
    onTagToggle: (Long) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showStartTimePicker by remember { mutableStateOf(false) }
    var showEndTimePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.medium)
    ) {
        // Activity type section
        Text(
            text = "活动类型",
            style = MaterialTheme.typography.titleMedium
        )

        if (uiState.activityTypeError != null) {
            Text(
                text = uiState.activityTypeError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(Spacing.small))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            uiState.activityTypes.forEach { activityType ->
                ActivityTypeChip(
                    activityType = activityType,
                    isSelected = activityType.id == uiState.selectedActivityId,
                    onClick = { onActivityTypeSelect(activityType) }
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.large))

        // Date and time section
        Text(
            text = "日期和时间",
            style = MaterialTheme.typography.titleMedium
        )

        if (uiState.timeError != null) {
            Text(
                text = uiState.timeError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(Spacing.small))

        // Date picker
        BlockwiseCard(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { showDatePicker = true }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.width(Spacing.medium))
                Column {
                    Text(
                        text = "日期",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = uiState.selectedDate?.let { formatDate(it) } ?: "选择日期",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.small))

        // Time pickers
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            // Start time
            BlockwiseCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showStartTimePicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Column {
                        Text(
                            text = "开始",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = uiState.startTime?.let { formatTime(it) } ?: "--:--",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }

            // End time
            BlockwiseCard(
                modifier = Modifier
                    .weight(1f)
                    .clickable { showEndTimePicker = true }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.medium),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.AccessTime,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    Column {
                        Text(
                            text = "结束",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = uiState.endTime?.let { formatTime(it) } ?: "--:--",
                            style = MaterialTheme.typography.bodyLarge
                        )
                    }
                }
            }
        }

        // Duration display
        uiState.formattedDuration?.let { duration ->
            Spacer(modifier = Modifier.height(Spacing.small))
            Text(
                text = "时长：$duration",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(Spacing.large))

        // Tags section
        if (uiState.availableTags.isNotEmpty()) {
            Text(
                text = "标签（可选）",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(Spacing.small))

            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                uiState.availableTags.forEach { tag ->
                    TagChip(
                        tag = tag,
                        isSelected = tag.id in uiState.selectedTagIds,
                        onClick = { onTagToggle(tag.id) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(Spacing.large))
        }

        // Note section
        Text(
            text = "备注（可选）",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(Spacing.small))

        OutlinedTextField(
            value = uiState.note,
            onValueChange = onNoteChange,
            placeholder = { Text("添加备注...") },
            minLines = 3,
            maxLines = 5,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(Spacing.extraLarge))

        // Save button
        BlockwisePrimaryButton(
            text = "保存",
            onClick = onSave,
            enabled = uiState.canSave,
            modifier = Modifier.fillMaxWidth()
        )
    }

    // Date picker dialog
    BlockwiseDatePickerDialog(
        visible = showDatePicker,
        onDismiss = { showDatePicker = false },
        onDateSelected = { date ->
            onDateChange(date)
            showDatePicker = false
        },
        initialDate = uiState.selectedDate
    )

    // Start time picker dialog
    BlockwiseTimePickerDialog(
        visible = showStartTimePicker,
        onDismiss = { showStartTimePicker = false },
        onTimeSelected = { time ->
            onStartTimeChange(time)
            showStartTimePicker = false
        },
        initialTime = uiState.startTime
    )

    // End time picker dialog
    BlockwiseTimePickerDialog(
        visible = showEndTimePicker,
        onDismiss = { showEndTimePicker = false },
        onTimeSelected = { time ->
            onEndTimeChange(time)
            showEndTimePicker = false
        },
        initialTime = uiState.endTime
    )
}

@Composable
private fun ActivityTypeChip(
    activityType: ActivityType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) {
        parseColorSafe(activityType.colorHex)
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }

    val contentColor = if (isSelected) {
        getContrastColor(activityType.colorHex)
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .clickable(onClick = onClick)
            .padding(horizontal = Spacing.medium, vertical = Spacing.small)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            val iconText = activityType.icon
            if (iconText != null) {
                Text(
                    text = iconText,
                    style = MaterialTheme.typography.bodyMedium
                )
                Spacer(modifier = Modifier.width(4.dp))
            }
            Text(
                text = activityType.name,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (isSelected) {
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TagChip(
    tag: Tag,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        label = { Text(tag.name) },
        leadingIcon = {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(parseColorSafe(tag.colorHex))
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = parseColorSafe(tag.colorHex).copy(alpha = 0.2f)
        ),
        modifier = modifier
    )
}

/**
 * Format LocalDate for display.
 */
private fun formatDate(date: LocalDate): String {
    return "${date.year}年${date.monthNumber}月${date.dayOfMonth}日"
}

/**
 * Format LocalTime for display.
 */
private fun formatTime(time: LocalTime): String {
    return "%02d:%02d".format(time.hour, time.minute)
}

/**
 * Parse hex color string to Color safely.
 */
private fun parseColorSafe(colorHex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color.Gray
    }
}

/**
 * Get contrasting color (black or white) for text/icon visibility.
 */
private fun getContrastColor(colorHex: String): Color {
    return try {
        val color = android.graphics.Color.parseColor(colorHex)
        val luminance = (0.299 * android.graphics.Color.red(color) +
                0.587 * android.graphics.Color.green(color) +
                0.114 * android.graphics.Color.blue(color)) / 255
        if (luminance > 0.5) Color.Black else Color.White
    } catch (e: Exception) {
        Color.White
    }
}
