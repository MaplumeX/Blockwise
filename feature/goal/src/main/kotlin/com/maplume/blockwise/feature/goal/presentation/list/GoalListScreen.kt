package com.maplume.blockwise.feature.goal.presentation.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import com.maplume.blockwise.core.designsystem.component.BlockwiseAlertDialog
import com.maplume.blockwise.core.designsystem.component.BlockwiseEmptyState
import com.maplume.blockwise.core.designsystem.component.LoadingIndicator
import com.maplume.blockwise.core.designsystem.theme.Spacing
import com.maplume.blockwise.feature.goal.presentation.component.ArchivedGoalCard
import com.maplume.blockwise.feature.goal.presentation.component.GoalProgressCard

/**
 * Goal list screen.
 * Displays all goals with progress indicators.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GoalListScreen(
    onNavigateToAdd: () -> Unit,
    onNavigateToEdit: (Long) -> Unit,
    onNavigateToDetail: (Long) -> Unit,
    viewModel: GoalListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }

    // Handle events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is GoalListEvent.NavigateToAdd -> onNavigateToAdd()
                is GoalListEvent.NavigateToEdit -> onNavigateToEdit(event.goalId)
                is GoalListEvent.NavigateToDetail -> onNavigateToDetail(event.goalId)
                is GoalListEvent.Error -> snackbarHostState.showSnackbar(event.message)
                is GoalListEvent.DeleteSuccess -> snackbarHostState.showSnackbar("目标已删除")
                is GoalListEvent.ArchiveSuccess -> snackbarHostState.showSnackbar("目标已归档")
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("目标") },
                actions = {
                    TextButton(onClick = { viewModel.toggleShowArchived() }) {
                        Text(
                            text = if (uiState.showArchived) "隐藏归档" else "显示归档",
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { viewModel.onAddClick() },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "添加目标",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading -> {
                    LoadingIndicator()
                }
                uiState.goalProgressList.isEmpty() -> {
                    BlockwiseEmptyState(
                        title = "暂无目标",
                        description = "设定时间目标，追踪你的进度",
                        icon = Icons.Outlined.Flag
                    )
                }
                else -> {
                    GoalList(
                        uiState = uiState,
                        onGoalClick = { viewModel.onGoalClick(it) },
                        onEditClick = { viewModel.onEditClick(it) },
                        onDeleteClick = { viewModel.onDeleteRequest(it) },
                        onRestoreClick = { viewModel.onRestoreClick(it) }
                    )
                }
            }
        }

        // Delete confirmation dialog
        uiState.goalToDelete?.let { goal ->
            BlockwiseAlertDialog(
                visible = true,
                onDismiss = { viewModel.onDeleteCancel() },
                title = if (goal.isActive) "归档目标" else "删除目标",
                message = if (goal.isActive) {
                    "确定要归档「${goal.tag.name}」的目标吗？归档后可以恢复。"
                } else {
                    "确定要永久删除「${goal.tag.name}」的目标吗？此操作不可撤销。"
                },
                confirmText = if (goal.isActive) "归档" else "删除",
                onConfirm = {
                    if (goal.isActive) {
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
private fun GoalList(
    uiState: GoalListUiState,
    onGoalClick: (com.maplume.blockwise.core.domain.model.Goal) -> Unit,
    onEditClick: (com.maplume.blockwise.core.domain.model.Goal) -> Unit,
    onDeleteClick: (com.maplume.blockwise.core.domain.model.Goal) -> Unit,
    onRestoreClick: (com.maplume.blockwise.core.domain.model.Goal) -> Unit
) {
    // Separate active and archived goals
    val activeGoals = uiState.goalProgressList.filter { it.goal.isActive }
    val archivedGoals = uiState.goalProgressList.filter { !it.goal.isActive }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(Spacing.medium),
        verticalArrangement = Arrangement.spacedBy(Spacing.small)
    ) {
        // Active goals
        items(
            items = activeGoals,
            key = { it.goal.id }
        ) { progress ->
            GoalProgressCard(
                progress = progress,
                onClick = { onGoalClick(progress.goal) },
                onEditClick = { onEditClick(progress.goal) },
                onDeleteClick = { onDeleteClick(progress.goal) }
            )
        }

        // Archived goals section
        if (uiState.showArchived && archivedGoals.isNotEmpty()) {
            item {
                Text(
                    text = "已归档",
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(
                        top = Spacing.medium,
                        bottom = Spacing.small
                    )
                )
            }

            items(
                items = archivedGoals,
                key = { it.goal.id }
            ) { progress ->
                ArchivedGoalCard(
                    goal = progress.goal,
                    onClick = { onGoalClick(progress.goal) },
                    onRestoreClick = { onRestoreClick(progress.goal) },
                    onDeleteClick = { onDeleteClick(progress.goal) }
                )
            }
        }
    }
}
