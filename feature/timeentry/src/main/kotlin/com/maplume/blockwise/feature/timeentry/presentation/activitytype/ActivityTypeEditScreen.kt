package com.maplume.blockwise.feature.timeentry.presentation.activitytype

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maplume.blockwise.core.designsystem.component.BlockwiseLoadingOverlay
import com.maplume.blockwise.core.designsystem.component.BlockwisePrimaryButton
import com.maplume.blockwise.core.designsystem.component.BlockwiseTopAppBarWithBack
import com.maplume.blockwise.core.designsystem.component.LoadingIndicator
import com.maplume.blockwise.core.designsystem.theme.Spacing

/**
 * Activity type edit screen.
 * Supports both create and edit modes.
 */
@Composable
fun ActivityTypeEditScreen(
    onNavigateBack: () -> Unit,
    viewModel: ActivityTypeEditViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    // Handle one-time events
    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is ActivityTypeEditEvent.SaveSuccess -> {
                    Toast.makeText(context, "保存成功", Toast.LENGTH_SHORT).show()
                }
                is ActivityTypeEditEvent.NavigateBack -> {
                    onNavigateBack()
                }
                is ActivityTypeEditEvent.Error -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        topBar = {
            BlockwiseTopAppBarWithBack(
                title = if (viewModel.isEditMode) "编辑活动类型" else "新建活动类型",
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize()) {
            if (uiState.isLoading) {
                LoadingIndicator(modifier = Modifier.padding(padding))
            } else {
                ActivityTypeEditContent(
                    uiState = uiState,
                    onNameChange = viewModel::onNameChange,
                    onColorChange = viewModel::onColorChange,
                    onIconChange = viewModel::onIconChange,
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

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActivityTypeEditContent(
    uiState: ActivityTypeEditUiState,
    onNameChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onIconChange: (String) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.medium)
    ) {
        // Name field
        OutlinedTextField(
            value = uiState.name,
            onValueChange = onNameChange,
            label = { Text("名称") },
            placeholder = { Text("例如：工作、学习、运动") },
            isError = uiState.nameError != null,
            supportingText = uiState.nameError?.let { { Text(it) } },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(Spacing.large))

        // Color picker section
        Text(
            text = "选择颜色",
            style = MaterialTheme.typography.titleMedium
        )

        if (uiState.colorError != null) {
            Text(
                text = uiState.colorError,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error
            )
        }

        Spacer(modifier = Modifier.height(Spacing.small))

        // Color preview
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(parseColorSafe(uiState.colorHex))
                .border(
                    width = 2.dp,
                    color = MaterialTheme.colorScheme.outline,
                    shape = CircleShape
                )
        )

        Spacer(modifier = Modifier.height(Spacing.medium))

        // Preset colors
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(Spacing.small),
            verticalArrangement = Arrangement.spacedBy(Spacing.small)
        ) {
            PresetColors.forEach { color ->
                ColorOption(
                    colorHex = color,
                    isSelected = uiState.colorHex.equals(color, ignoreCase = true),
                    onClick = { onColorChange(color) }
                )
            }
        }

        Spacer(modifier = Modifier.height(Spacing.large))

        // Icon field (optional)
        OutlinedTextField(
            value = uiState.icon,
            onValueChange = onIconChange,
            label = { Text("图标（可选）") },
            placeholder = { Text("输入 emoji 或图标名称") },
            singleLine = true,
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
            .size(48.dp)
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
                modifier = Modifier.size(24.dp)
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
 * Preset color palette for activity types.
 */
private val PresetColors = listOf(
    // Primary colors
    "#F44336", // Red
    "#E91E63", // Pink
    "#9C27B0", // Purple
    "#673AB7", // Deep Purple
    "#3F51B5", // Indigo
    "#2196F3", // Blue
    "#03A9F4", // Light Blue
    "#00BCD4", // Cyan
    "#009688", // Teal
    "#4CAF50", // Green
    "#8BC34A", // Light Green
    "#CDDC39", // Lime
    "#FFEB3B", // Yellow
    "#FFC107", // Amber
    "#FF9800", // Orange
    "#FF5722", // Deep Orange
    "#795548", // Brown
    "#607D8B", // Blue Grey
    "#9E9E9E", // Grey
    "#000000"  // Black
)
