package com.maplume.blockwise.feature.goal.presentation.detail

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.maplume.blockwise.core.designsystem.component.BlockwiseAlertDialog
import com.maplume.blockwise.core.designsystem.component.BlockwiseCard
import com.maplume.blockwise.core.designsystem.component.BlockwiseCircularProgress
import com.maplume.blockwise.core.designsystem.component.BlockwiseLinearProgress
import com.maplume.blockwise.core.designsystem.component.LoadingIndicator
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme
import com.maplume.blockwise.core.designsystem.theme.Spacing
import com.maplume.blockwise.core.domain.model.DailyTrend
import com.maplume.blockwise.core.domain.model.Goal
import com.maplume.blockwise.core.domain.model.GoalPeriod
import com.maplume.blockwise.core.domain.model.GoalProgress
import com.maplume.blockwise.core.domain.model.GoalType
import com.maplume.blockwise.core.domain.model.Tag
import com.maplume.blockwise.feature.goal.presentation.component.formatMinutes
import com.maplume.blockwise.feature.goal.presentation.component.parseColor

/**
 * Goal detail screen.
 * Displays detailed progress and trends for a goal.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalDetailScreen(
    onNavigateBack: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    viewModel: GoalDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GoalDetailEvent.NavigateBack -> onNavigateBack()
                is GoalDetailEvent.NavigateToEdit -> onNavigateToEdit(event.goalId)
                is GoalDetailEvent.Error -> snackbarHostState.showSnackbar(event.message)
                is GoalDetailEvent.DeleteSuccess -> snackbarHostState.showSnackbar("操作成功")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("目标详情") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                actions = {
                    if (uiState.goal?.isActive == true) {
                        IconButton(onClick = { viewModel.onEditClick() }) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = "编辑"
                            )
                        }
                    }
                    IconButton(onClick = { viewModel.onDeleteClick() }) {
                        Icon(
                            imageVector = Icons.Outlined.Delete,
                            contentDescription = "删除",
                            tint = MaterialTheme.colorScheme.error
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        when {
            uiState.isLoading -> {
                LoadingIndicator()
            }
            uiState.error != null -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = uiState.error ?: "加载失败",
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
            uiState.goal != null && uiState.progress != null -> {
                GoalDetailContent(
                    goal = uiState.goal!!,
                    progress = uiState.progress!!,
                    dailyTrends = uiState.dailyTrends,
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                )
            }
        }

        // Delete confirmation dialog
        if (uiState.showDeleteDialog) {
            val goal = uiState.goal
            BlockwiseAlertDialog(
                visible = true,
                onDismiss = { viewModel.onDeleteCancel() },
                title = if (goal?.isActive == true) "归档目标" else "删除目标",
                message = if (goal?.isActive == true) {
                    "确定要归档此目标吗？归档后可以恢复。"
                } else {
                    "确定要永久删除此目标吗？此操作不可撤销。"
                },
                confirmText = if (goal?.isActive == true) "归档" else "删除",
                onConfirm = {
                    if (goal?.isActive == true) {
                        viewModel.onArchiveConfirm()
                    } else {
                        viewModel.onDeleteConfirm()
                    }
                }
            )
        }
    }
}

@Composable
private fun GoalDetailContent(
    goal: Goal,
    progress: GoalProgress,
    dailyTrends: List<DailyTrend>,
    modifier: Modifier = Modifier
) {
    val tagColor = parseColor(goal.tag.colorHex)

    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(Spacing.medium)
    ) {
        // Progress overview card
        BlockwiseCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(Spacing.large),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Tag info
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .background(tagColor, CircleShape)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = goal.tag.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(Spacing.large))

                // Large progress circle
                BlockwiseCircularProgress(
                    progress = progress.progress,
                    size = 160.dp,
                    strokeWidth = 12.dp,
                    color = tagColor
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "${progress.progressPercentage}%",
                            style = MaterialTheme.typography.headlineLarge,
                            fontWeight = FontWeight.Bold,
                            color = tagColor
                        )
                        Text(
                            text = if (progress.isCompleted) "已完成" else "进行中",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(Spacing.large))

                // Progress details
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    ProgressStat(
                        label = "已完成",
                        value = formatMinutes(progress.currentMinutes),
                        color = tagColor
                    )
                    ProgressStat(
                        label = "目标",
                        value = formatMinutes(progress.targetMinutes),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    ProgressStat(
                        label = "剩余",
                        value = formatMinutes(progress.remainingMinutes),
                        color = if (progress.isCompleted) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.medium))

        // Goal info card
        BlockwiseCard(
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(Spacing.medium)
            ) {
                Text(
                    text = "目标设置",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(Spacing.medium))

                InfoRow(label = "目标类型", value = goal.goalTypeLabel)
                InfoRow(label = "周期", value = goal.periodLabel)
                InfoRow(label = "目标时长", value = goal.formattedTarget)

                if (goal.period == GoalPeriod.CUSTOM) {
                    goal.startDate?.let { start ->
                        InfoRow(label = "开始日期", value = start.toString())
                    }
                    goal.endDate?.let { end ->
                        InfoRow(label = "结束日期", value = end.toString())
                    }
                }

                InfoRow(
                    label = "状态",
                    value = if (goal.isActive) "活动中" else "已归档"
                )
            }
        }

        // Daily trends card (if available)
        if (dailyTrends.isNotEmpty()) {
            Spacer(modifier = Modifier.height(Spacing.medium))

            BlockwiseCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(Spacing.medium)
                ) {
                    Text(
                        text = "本周期每日进度",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(Spacing.medium))

                    dailyTrends.forEach { trend ->
                        DailyTrendItem(
                            trend = trend,
                            targetMinutes = progress.targetMinutes,
                            color = tagColor
                        )
                        Spacer(modifier = Modifier.height(Spacing.small))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(Spacing.large))
    }
}

@Composable
private fun ProgressStat(
    label: String,
    value: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = color
        )
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun DailyTrendItem(
    trend: DailyTrend,
    targetMinutes: Int,
    color: androidx.compose.ui.graphics.Color
) {
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = trend.date.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = formatMinutes(trend.totalMinutes),
                style = MaterialTheme.typography.bodySmall,
                fontWeight = FontWeight.Medium
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        BlockwiseLinearProgress(
            progress = if (targetMinutes > 0) {
                (trend.totalMinutes.toFloat() / targetMinutes).coerceIn(0f, 1f)
            } else 0f,
            color = color,
            showPercentage = false,
            label = null
        )
    }
}

// =============================================================================
// Previews
// =============================================================================

@Preview(showBackground = true)
@Composable
private fun GoalDetailContentPreview() {
    val sampleGoal = Goal(
        id = 1,
        tag = Tag(id = 1, name = "学习", colorHex = "#4CAF50"),
        targetMinutes = 1200,
        goalType = GoalType.MIN,
        period = GoalPeriod.WEEKLY
    )
    val sampleProgress = GoalProgress(
        goal = sampleGoal,
        currentMinutes = 900,
        targetMinutes = 1200
    )

    BlockwiseTheme {
        GoalDetailContent(
            goal = sampleGoal,
            progress = sampleProgress,
            dailyTrends = emptyList()
        )
    }
}
