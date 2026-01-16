package com.maplume.blockwise.feature.timeentry.presentation.activitytype

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maplume.blockwise.core.designsystem.component.BlockwiseAlertDialog
import com.maplume.blockwise.core.designsystem.component.BlockwiseCard
import com.maplume.blockwise.core.designsystem.component.BlockwiseEmptyState
import com.maplume.blockwise.core.designsystem.component.BlockwisePrimaryButton
import com.maplume.blockwise.core.designsystem.component.BlockwiseTopAppBarWithBack
import com.maplume.blockwise.core.designsystem.component.LoadingIndicator
import com.maplume.blockwise.core.designsystem.theme.Spacing
import com.maplume.blockwise.core.domain.model.ActivityType

/**
 * Activity type list screen.
 * Displays all activity types with options to add, edit, and delete.
 */
@Composable
fun ActivityTypeListScreen(
    onNavigateToEdit: (Long?) -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: ActivityTypeListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Handle one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ActivityTypeListEvent.DeleteSuccess -> {
                    Toast.makeText(context, "已删除「${event.name}」", Toast.LENGTH_SHORT).show()
                }
                is ActivityTypeListEvent.RestoreSuccess -> {
                    Toast.makeText(context, "已恢复", Toast.LENGTH_SHORT).show()
                }
                is ActivityTypeListEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            BlockwiseTopAppBarWithBack(
                title = "活动类型",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = { onNavigateToEdit(null) }) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加活动类型"
                        )
                    }
                }
            )
        }
    ) { padding ->
        ActivityTypeListContent(
            uiState = uiState,
            onItemClick = { onNavigateToEdit(it.id) },
            onDeleteRequest = viewModel::onDeleteRequest,
            onAddClick = { onNavigateToEdit(null) },
            modifier = Modifier.padding(padding)
        )

        // Delete confirmation dialog
        BlockwiseAlertDialog(
            visible = uiState.showDeleteDialog,
            onDismiss = viewModel::onDeleteCancel,
            title = "确认删除",
            message = "确定要删除「${uiState.activityTypeToDelete?.name}」吗？\n删除后可在设置中恢复。",
            confirmText = "删除",
            dismissText = "取消",
            onConfirm = viewModel::onDeleteConfirm
        )
    }
}

@Composable
private fun ActivityTypeListContent(
    uiState: ActivityTypeListUiState,
    onItemClick: (ActivityType) -> Unit,
    onDeleteRequest: (ActivityType) -> Unit,
    onAddClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when {
        uiState.isLoading -> {
            LoadingIndicator(modifier = modifier)
        }
        uiState.isEmpty -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                BlockwiseEmptyState(
                    title = "暂无活动类型",
                    description = "点击下方按钮创建第一个活动类型",
                    actionButton = {
                        BlockwisePrimaryButton(
                            text = "添加活动类型",
                            onClick = onAddClick
                        )
                    }
                )
            }
        }
        else -> {
            LazyColumn(
                modifier = modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.medium),
                verticalArrangement = Arrangement.spacedBy(Spacing.small)
            ) {
                items(
                    items = uiState.activityTypes,
                    key = { it.id }
                ) { activityType ->
                    SwipeToDeleteActivityTypeItem(
                        activityType = activityType,
                        onClick = { onItemClick(activityType) },
                        onDelete = { onDeleteRequest(activityType) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeToDeleteActivityTypeItem(
    activityType: ActivityType,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                false // Don't actually dismiss, let the dialog handle it
            } else {
                false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.error)
                    .padding(horizontal = Spacing.medium),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.onError
                )
            }
        },
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true
    ) {
        ActivityTypeListItem(
            activityType = activityType,
            onClick = onClick
        )
    }
}

@Composable
private fun ActivityTypeListItem(
    activityType: ActivityType,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    BlockwiseCard(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.medium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Color indicator
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(parseColor(activityType.colorHex))
            )

            Spacer(modifier = Modifier.width(Spacing.medium))

            // Name and info
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Text(
                    text = activityType.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                val iconText = activityType.icon
                if (iconText != null) {
                    Text(
                        text = iconText,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // Chevron
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

/**
 * Parse hex color string to Color.
 */
private fun parseColor(colorHex: String): Color {
    return try {
        Color(android.graphics.Color.parseColor(colorHex))
    } catch (e: Exception) {
        Color.Gray
    }
}
