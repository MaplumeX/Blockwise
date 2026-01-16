package com.maplume.blockwise.feature.timeentry.presentation.tag

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.maplume.blockwise.core.designsystem.component.BlockwiseTextButton
import com.maplume.blockwise.core.designsystem.component.BlockwiseTopAppBarWithBack
import com.maplume.blockwise.core.designsystem.component.LoadingIndicator
import com.maplume.blockwise.core.designsystem.theme.CornerRadius
import com.maplume.blockwise.core.designsystem.theme.Spacing
import com.maplume.blockwise.core.domain.model.Tag

/**
 * Tag management screen.
 * Displays all tags with options to add, edit, and delete.
 */
@Composable
fun TagManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: TagManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Handle one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is TagManagementEvent.CreateSuccess -> {
                    Toast.makeText(context, "标签已创建", Toast.LENGTH_SHORT).show()
                }
                is TagManagementEvent.UpdateSuccess -> {
                    Toast.makeText(context, "标签已更新", Toast.LENGTH_SHORT).show()
                }
                is TagManagementEvent.DeleteSuccess -> {
                    Toast.makeText(context, "已删除「${event.name}」", Toast.LENGTH_SHORT).show()
                }
                is TagManagementEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            BlockwiseTopAppBarWithBack(
                title = "标签管理",
                onBackClick = onNavigateBack,
                actions = {
                    IconButton(onClick = viewModel::onAddClick) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "添加标签"
                        )
                    }
                }
            )
        }
    ) { padding ->
        TagManagementContent(
            uiState = uiState,
            onItemClick = viewModel::onEditClick,
            onDeleteRequest = viewModel::onDeleteRequest,
            onAddClick = viewModel::onAddClick,
            modifier = Modifier.padding(padding)
        )

        // Edit dialog
        if (uiState.showEditDialog) {
            TagEditDialog(
                isEditMode = uiState.isEditMode,
                name = uiState.dialogName,
                colorHex = uiState.dialogColorHex,
                nameError = uiState.dialogNameError,
                isSaving = uiState.isSaving,
                onNameChange = viewModel::onDialogNameChange,
                onColorChange = viewModel::onDialogColorChange,
                onDismiss = viewModel::onDialogDismiss,
                onSave = viewModel::onDialogSave
            )
        }

        // Delete confirmation dialog
        BlockwiseAlertDialog(
            visible = uiState.showDeleteDialog,
            onDismiss = viewModel::onDeleteCancel,
            title = "确认删除",
            message = "确定要删除「${uiState.tagToDelete?.name}」吗？\n删除后可在设置中恢复。",
            confirmText = "删除",
            dismissText = "取消",
            onConfirm = viewModel::onDeleteConfirm
        )
    }
}

@Composable
private fun TagManagementContent(
    uiState: TagManagementUiState,
    onItemClick: (Tag) -> Unit,
    onDeleteRequest: (Tag) -> Unit,
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
                    title = "暂无标签",
                    description = "点击下方按钮创建第一个标签",
                    actionButton = {
                        BlockwisePrimaryButton(
                            text = "添加标签",
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
                    items = uiState.tags,
                    key = { it.id }
                ) { tag ->
                    SwipeToDeleteTagItem(
                        tag = tag,
                        onClick = { onItemClick(tag) },
                        onDelete = { onDeleteRequest(tag) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SwipeToDeleteTagItem(
    tag: Tag,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onDelete()
                false
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
        TagListItem(
            tag = tag,
            onClick = onClick
        )
    }
}

@Composable
private fun TagListItem(
    tag: Tag,
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
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(parseColorSafe(tag.colorHex))
            )

            Spacer(modifier = Modifier.width(Spacing.medium))

            // Name
            Text(
                text = tag.name,
                style = MaterialTheme.typography.titleMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier.weight(1f)
            )

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
 * Dialog for creating/editing a tag.
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun TagEditDialog(
    isEditMode: Boolean,
    name: String,
    colorHex: String,
    nameError: String?,
    isSaving: Boolean,
    onNameChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onDismiss: () -> Unit,
    onSave: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (isEditMode) "编辑标签" else "新建标签",
                style = MaterialTheme.typography.titleLarge
            )
        },
        text = {
            Column {
                // Name field
                OutlinedTextField(
                    value = name,
                    onValueChange = onNameChange,
                    label = { Text("名称") },
                    placeholder = { Text("例如：重要、紧急") },
                    isError = nameError != null,
                    supportingText = nameError?.let { { Text(it) } },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(Spacing.medium))

                // Color picker
                Text(
                    text = "选择颜色",
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(modifier = Modifier.height(Spacing.small))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(Spacing.small),
                    verticalArrangement = Arrangement.spacedBy(Spacing.small)
                ) {
                    TagPresetColors.forEach { color ->
                        ColorOption(
                            colorHex = color,
                            isSelected = colorHex.equals(color, ignoreCase = true),
                            onClick = { onColorChange(color) }
                        )
                    }
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = Spacing.small),
                horizontalArrangement = Arrangement.End
            ) {
                BlockwiseTextButton(
                    text = "取消",
                    onClick = onDismiss
                )
                Spacer(modifier = Modifier.width(Spacing.small))
                BlockwisePrimaryButton(
                    text = if (isSaving) "保存中..." else "保存",
                    onClick = onSave,
                    enabled = name.isNotBlank() && !isSaving
                )
            }
        },
        shape = RoundedCornerShape(CornerRadius.extraLarge),
        containerColor = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp
    )
}

@Composable
private fun ColorOption(
    colorHex: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(parseColorSafe(colorHex))
            .then(
                if (isSelected) {
                    Modifier.border(
                        width = 3.dp,
                        color = MaterialTheme.colorScheme.primary,
                        shape = CircleShape
                    )
                } else {
                    Modifier.border(
                        width = 1.dp,
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                        shape = CircleShape
                    )
                }
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isSelected) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = "已选择",
                tint = getContrastColor(colorHex),
                modifier = Modifier.size(20.dp)
            )
        }
    }
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

/**
 * Preset color palette for tags.
 */
private val TagPresetColors = listOf(
    "#F44336", // Red
    "#E91E63", // Pink
    "#9C27B0", // Purple
    "#3F51B5", // Indigo
    "#2196F3", // Blue
    "#00BCD4", // Cyan
    "#009688", // Teal
    "#4CAF50", // Green
    "#8BC34A", // Light Green
    "#FFEB3B", // Yellow
    "#FF9800", // Orange
    "#795548"  // Brown
)
