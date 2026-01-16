package com.maplume.blockwise.feature.timeentry.presentation.timer

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maplume.blockwise.core.designsystem.component.LoadingIndicator
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme
import com.maplume.blockwise.core.domain.model.ActivityType
import com.maplume.blockwise.feature.timeentry.domain.model.TimerState
import com.maplume.blockwise.feature.timeentry.domain.usecase.timer.RecoverableTimer

/**
 * Timer screen with timer widget and activity selection.
 */
@Composable
fun TimerScreen(
    onNavigateToTimeEntry: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: TimerViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val timerState by viewModel.timerState.collectAsStateWithLifecycle()
    val elapsedMillis by viewModel.elapsedMillis.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TimerEvent.TimerStopped -> {
                    if (event.entryId != null) {
                        snackbarHostState.showSnackbar("时间记录已保存")
                    }
                }
                is TimerEvent.Error -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is TimerEvent.TimerDiscarded -> {
                    snackbarHostState.showSnackbar("计时已取消")
                }
            }
        }
    }

    TimerScreenContent(
        uiState = uiState,
        timerState = timerState,
        elapsedMillis = elapsedMillis,
        snackbarHostState = snackbarHostState,
        onStartTimer = viewModel::onStartTimer,
        onPauseTimer = viewModel::onPauseTimer,
        onResumeTimer = viewModel::onResumeTimer,
        onStopTimer = viewModel::onStopTimer,
        onDiscardTimer = viewModel::onDiscardTimer,
        onActivitySelect = viewModel::onActivitySelect,
        onQuickStart = viewModel::onQuickStart,
        onShowActivitySelector = viewModel::showActivitySelector,
        onHideActivitySelector = viewModel::hideActivitySelector,
        onRecoverTimer = viewModel::onRecoverTimer,
        onDiscardRecovery = viewModel::onDiscardRecovery,
        onDismissRecoveryDialog = viewModel::dismissRecoveryDialog,
        modifier = modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun TimerScreenContent(
    uiState: TimerUiState,
    timerState: TimerState,
    elapsedMillis: Long,
    snackbarHostState: SnackbarHostState,
    onStartTimer: () -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onStopTimer: () -> Unit,
    onDiscardTimer: () -> Unit,
    onActivitySelect: (ActivityType) -> Unit,
    onQuickStart: (ActivityType) -> Unit,
    onShowActivitySelector: () -> Unit,
    onHideActivitySelector: () -> Unit,
    onRecoverTimer: () -> Unit,
    onDiscardRecovery: () -> Unit,
    onDismissRecoveryDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sheetState = rememberModalBottomSheetState()

    Scaffold(
        modifier = modifier,
        snackbarHost = { SnackbarHost(snackbarHostState) }
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
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Timer widget
                TimerWidget(
                    state = timerState,
                    elapsedMillis = elapsedMillis,
                    onStart = {
                        if (uiState.selectedActivityId != null) {
                            onStartTimer()
                        } else {
                            onShowActivitySelector()
                        }
                    },
                    onPause = onPauseTimer,
                    onResume = onResumeTimer,
                    onStop = onStopTimer
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Quick start section (only show when timer is idle)
                if (timerState is TimerState.Idle) {
                    Text(
                        text = "快速开始",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        uiState.activityTypes.take(6).forEach { activityType ->
                            QuickStartChip(
                                activityType = activityType,
                                isSelected = activityType.id == uiState.selectedActivityId,
                                onClick = { onQuickStart(activityType) }
                            )
                        }
                    }

                    if (uiState.activityTypes.size > 6) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = onShowActivitySelector) {
                            Text("查看全部活动类型")
                        }
                    }
                }
            }
        }

        // Activity selector bottom sheet
        if (uiState.showActivitySelector) {
            ModalBottomSheet(
                onDismissRequest = onHideActivitySelector,
                sheetState = sheetState
            ) {
                ActivitySelectorContent(
                    activityTypes = uiState.activityTypes,
                    selectedActivityId = uiState.selectedActivityId,
                    onActivitySelect = { activityType ->
                        onActivitySelect(activityType)
                        onStartTimer()
                    }
                )
            }
        }

        // Recovery dialog
        if (uiState.showRecoveryDialog && uiState.recoverableTimer != null) {
            TimerRecoveryDialog(
                recoverableTimer = uiState.recoverableTimer,
                onRecover = onRecoverTimer,
                onDiscard = onDiscardRecovery,
                onDismiss = onDismissRecoveryDialog
            )
        }
    }
}

/**
 * Quick start chip for activity selection.
 */
@Composable
private fun QuickStartChip(
    activityType: ActivityType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surfaceVariant
            }
        )
    ) {
        Box(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center
        ) {
            androidx.compose.foundation.layout.Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(parseColor(activityType.colorHex))
                )
                Text(
                    text = activityType.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isSelected) {
                        MaterialTheme.colorScheme.onPrimaryContainer
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
        }
    }
}

/**
 * Activity selector content for bottom sheet.
 */
@Composable
private fun ActivitySelectorContent(
    activityTypes: List<ActivityType>,
    selectedActivityId: Long?,
    onActivitySelect: (ActivityType) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(bottom = 32.dp)
    ) {
        Text(
            text = "选择活动类型",
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyColumn(
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(activityTypes) { activityType ->
                ActivityTypeItem(
                    activityType = activityType,
                    isSelected = activityType.id == selectedActivityId,
                    onClick = { onActivitySelect(activityType) }
                )
            }
        }
    }
}

/**
 * Activity type item in the selector.
 */
@Composable
private fun ActivityTypeItem(
    activityType: ActivityType,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                MaterialTheme.colorScheme.surface
            }
        )
    ) {
        androidx.compose.foundation.layout.Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(parseColor(activityType.colorHex))
            )

            Spacer(modifier = Modifier.weight(1f).padding(start = 12.dp))

            Text(
                text = activityType.name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f).padding(start = 12.dp)
            )

            if (isSelected) {
                Icon(
                    imageVector = Icons.Default.Check,
                    contentDescription = "已选择",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

/**
 * Timer recovery dialog.
 */
@Composable
private fun TimerRecoveryDialog(
    recoverableTimer: RecoverableTimer,
    onRecover: () -> Unit,
    onDiscard: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("恢复计时") },
        text = {
            Column {
                Text("检测到未完成的计时：")
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = recoverableTimer.activityName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "已计时: ${formatDurationForDialog(recoverableTimer.elapsedMillis)}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (recoverableTimer.isPaused) {
                    Text(
                        text = "(已暂停)",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onRecover) {
                Text("继续计时")
            }
        },
        dismissButton = {
            TextButton(onClick = onDiscard) {
                Text("放弃")
            }
        }
    )
}

private fun formatDurationForDialog(millis: Long): String {
    val totalSeconds = millis / 1000
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60

    return when {
        hours > 0 -> "${hours}小时${minutes}分钟"
        minutes > 0 -> "${minutes}分钟"
        else -> "不足1分钟"
    }
}

private fun parseColor(colorHex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color(0xFF4CAF50)
    }
}

// =============================================================================
// Previews
// =============================================================================

@Preview(showBackground = true)
@Composable
private fun TimerScreenIdlePreview() {
    BlockwiseTheme {
        TimerScreenContent(
            uiState = TimerUiState(
                activityTypes = listOf(
                    ActivityType(1, "工作", "#4CAF50", null, null, 0, false),
                    ActivityType(2, "学习", "#2196F3", null, null, 1, false),
                    ActivityType(3, "运动", "#FF9800", null, null, 2, false)
                ),
                isLoading = false
            ),
            timerState = TimerState.Idle,
            elapsedMillis = 0,
            snackbarHostState = SnackbarHostState(),
            onStartTimer = {},
            onPauseTimer = {},
            onResumeTimer = {},
            onStopTimer = {},
            onDiscardTimer = {},
            onActivitySelect = {},
            onQuickStart = {},
            onShowActivitySelector = {},
            onHideActivitySelector = {},
            onRecoverTimer = {},
            onDiscardRecovery = {},
            onDismissRecoveryDialog = {}
        )
    }
}
