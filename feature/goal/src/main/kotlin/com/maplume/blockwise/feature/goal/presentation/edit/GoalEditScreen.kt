package com.maplume.blockwise.feature.goal.presentation.edit

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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maplume.blockwise.core.designsystem.component.BlockwiseDatePickerDialog
import com.maplume.blockwise.core.designsystem.component.LoadingIndicator
import com.maplume.blockwise.core.designsystem.component.BlockwisePrimaryButton
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme
import com.maplume.blockwise.core.designsystem.theme.CornerRadius
import com.maplume.blockwise.core.designsystem.theme.Spacing
import com.maplume.blockwise.core.domain.model.GoalPeriod
import com.maplume.blockwise.core.domain.model.GoalType
import com.maplume.blockwise.core.domain.model.Tag
import com.maplume.blockwise.feature.goal.presentation.component.parseColor
import kotlinx.datetime.LocalDate

/**
 * Goal edit screen.
 * Used for both creating and editing goals.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: GoalEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GoalEditEvent.SaveSuccess -> onNavigateBack()
                is GoalEditEvent.Error -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(if (uiState.isEditMode) "编辑目标" else "新建目标")
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        if (uiState.isLoading) {
            LoadingIndicator()
        } else {
            GoalEditContent(
                uiState = uiState,
                onTagSelected = viewModel::onTagSelected,
                onTargetHoursChanged = viewModel::onTargetHoursChanged,
                onTargetMinutesChanged = viewModel::onTargetMinutesChanged,
                onGoalTypeSelected = viewModel::onGoalTypeSelected,
                onPeriodSelected = viewModel::onPeriodSelected,
                onStartDateSelected = viewModel::onStartDateSelected,
                onEndDateSelected = viewModel::onEndDateSelected,
                onSave = viewModel::onSave,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            )
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GoalEditContent(
    uiState: GoalEditUiState,
    onTagSelected: (Long) -> Unit,
    onTargetHoursChanged: (Int) -> Unit,
    onTargetMinutesChanged: (Int) -> Unit,
    onGoalTypeSelected: (GoalType) -> Unit,
    onPeriodSelected: (GoalPeriod) -> Unit,
    onStartDateSelected: (LocalDate) -> Unit,
    onEndDateSelected: (LocalDate) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(Spacing.medium)
    ) {
        // Tag selection
        SectionTitle("选择标签")
        Spacer(modifier = Modifier.height(Spacing.small))

        if (uiState.availableTags.isEmpty()) {
            Text(
                text = "暂无可用标签，请先创建标签",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        } else {
            FlowRow(
                horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                uiState.availableTags.forEach { tag ->
                    TagChip(
                        tag = tag,
                        selected = tag.id == uiState.selectedTagId,
                        onClick = { onTagSelected(tag.id) }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.large))

        // Target duration
        SectionTitle("目标时长")
        Spacer(modifier = Modifier.height(Spacing.small))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            OutlinedTextField(
                value = if (uiState.targetHours > 0) uiState.targetHours.toString() else "",
                onValueChange = { value ->
                    val hours = value.toIntOrNull() ?: 0
                    onTargetHoursChanged(hours)
                },
                modifier = Modifier.width(80.dp),
                label = { Text("小时") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )

            Text(
                text = ":",
                style = MaterialTheme.typography.headlineMedium
            )

            OutlinedTextField(
                value = if (uiState.targetMinutes > 0) uiState.targetMinutes.toString() else "",
                onValueChange = { value ->
                    val minutes = value.toIntOrNull() ?: 0
                    onTargetMinutesChanged(minutes)
                },
                modifier = Modifier.width(80.dp),
                label = { Text("分钟") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                singleLine = true
            )
        }

        Spacer(modifier = Modifier.height(Spacing.large))

        // Goal type
        SectionTitle("目标类型")
        Spacer(modifier = Modifier.height(Spacing.small))

        Row(
            horizontalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            GoalType.entries.forEach { type ->
                FilterChip(
                    selected = type == uiState.goalType,
                    onClick = { onGoalTypeSelected(type) },
                    label = {
                        Text(
                            when (type) {
                                GoalType.MIN -> "至少"
                                GoalType.MAX -> "最多"
                                GoalType.EXACT -> "精确"
                            }
                        )
                    },
                    leadingIcon = if (type == uiState.goalType) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null
                )
            }
        }

        Text(
            text = when (uiState.goalType) {
                GoalType.MIN -> "每个周期至少完成指定时长"
                GoalType.MAX -> "每个周期不超过指定时长"
                GoalType.EXACT -> "每个周期精确完成指定时长"
            },
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.extraSmall)
        )

        Spacer(modifier = Modifier.height(Spacing.large))

        // Period
        SectionTitle("周期")
        Spacer(modifier = Modifier.height(Spacing.small))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            GoalPeriod.entries.forEach { period ->
                FilterChip(
                    selected = period == uiState.period,
                    onClick = { onPeriodSelected(period) },
                    label = {
                        Text(
                            when (period) {
                                GoalPeriod.DAILY -> "每天"
                                GoalPeriod.WEEKLY -> "每周"
                                GoalPeriod.MONTHLY -> "每月"
                                GoalPeriod.CUSTOM -> "自定义"
                            }
                        )
                    },
                    leadingIcon = if (period == uiState.period) {
                        {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    } else null
                )
            }
        }

        // Custom period date selection
        if (uiState.period == GoalPeriod.CUSTOM) {
            Spacer(modifier = Modifier.height(Spacing.medium))

            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.medium)
            ) {
                DateSelector(
                    label = "开始日期",
                    date = uiState.startDate,
                    onClick = { showStartDatePicker = true },
                    modifier = Modifier.weight(1f)
                )

                DateSelector(
                    label = "结束日期",
                    date = uiState.endDate,
                    onClick = { showEndDatePicker = true },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Validation error
        uiState.validationError?.let { error ->
            Spacer(modifier = Modifier.height(Spacing.medium))
            Text(
                text = error,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(Spacing.extraLarge))

        // Save button
        BlockwisePrimaryButton(
            text = if (uiState.isEditMode) "保存" else "创建",
            onClick = onSave,
            enabled = uiState.isValid && !uiState.isSaving,
            loading = uiState.isSaving,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(Spacing.large))
    }

    // Date pickers
    BlockwiseDatePickerDialog(
        visible = showStartDatePicker,
        onDismiss = { showStartDatePicker = false },
        onDateSelected = { date ->
            onStartDateSelected(date)
            showStartDatePicker = false
        },
        initialDate = uiState.startDate
    )

    BlockwiseDatePickerDialog(
        visible = showEndDatePicker,
        onDismiss = { showEndDatePicker = false },
        onDateSelected = { date ->
            onEndDateSelected(date)
            showEndDatePicker = false
        },
        initialDate = uiState.endDate
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun TagChip(
    tag: Tag,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val tagColor = parseColor(tag.colorHex)

    Surface(
        modifier = modifier
            .clip(RoundedCornerShape(CornerRadius.medium))
            .clickable(onClick = onClick)
            .then(
                if (selected) {
                    Modifier.border(
                        width = 2.dp,
                        color = tagColor,
                        shape = RoundedCornerShape(CornerRadius.medium)
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = RoundedCornerShape(CornerRadius.medium)
                    )
                }
            ),
        color = if (selected) {
            tagColor.copy(alpha = 0.1f)
        } else {
            MaterialTheme.colorScheme.surface
        }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(tagColor, CircleShape)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = tag.name,
                style = MaterialTheme.typography.bodyMedium,
                color = if (selected) tagColor else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun DateSelector(
    label: String,
    date: LocalDate?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(CornerRadius.small))
                .clickable(onClick = onClick),
            color = MaterialTheme.colorScheme.surfaceVariant
        ) {
            Text(
                text = date?.toString() ?: "选择日期",
                style = MaterialTheme.typography.bodyMedium,
                color = if (date != null) {
                    MaterialTheme.colorScheme.onSurface
                } else {
                    MaterialTheme.colorScheme.onSurfaceVariant
                },
                modifier = Modifier.padding(12.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}

// =============================================================================
// Previews
// =============================================================================

@Preview(showBackground = true)
@Composable
private fun GoalEditContentPreview() {
    val sampleTags = listOf(
        Tag(id = 1, name = "学习", colorHex = "#4CAF50"),
        Tag(id = 2, name = "工作", colorHex = "#2196F3"),
        Tag(id = 3, name = "运动", colorHex = "#FF5722")
    )

    BlockwiseTheme {
        GoalEditContent(
            uiState = GoalEditUiState(
                availableTags = sampleTags,
                selectedTagId = 1,
                targetHours = 2,
                targetMinutes = 30,
                isLoading = false
            ),
            onTagSelected = {},
            onTargetHoursChanged = {},
            onTargetMinutesChanged = {},
            onGoalTypeSelected = {},
            onPeriodSelected = {},
            onStartDateSelected = {},
            onEndDateSelected = {},
            onSave = {}
        )
    }
}
