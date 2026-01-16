package com.maplume.blockwise.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme
import com.maplume.blockwise.core.designsystem.theme.CornerRadius
import com.maplume.blockwise.core.designsystem.theme.Spacing

/**
 * 确认对话框 - 现代极简风格
 * 大圆角 + 简化按钮布局
 */
@Composable
fun BlockwiseAlertDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    confirmText: String = "确认",
    dismissText: String = "取消",
    onConfirm: () -> Unit
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.small),
                    horizontalArrangement = Arrangement.End
                ) {
                    BlockwiseTextButton(
                        text = dismissText,
                        onClick = onDismiss
                    )
                    Spacer(modifier = Modifier.width(Spacing.small))
                    BlockwisePrimaryButton(
                        text = confirmText,
                        onClick = {
                            onConfirm()
                            onDismiss()
                        }
                    )
                }
            },
            shape = RoundedCornerShape(CornerRadius.extraLarge),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        )
    }
}

/**
 * 信息对话框 - 现代极简风格
 * 单按钮 + 大圆角
 */
@Composable
fun BlockwiseInfoDialog(
    visible: Boolean,
    onDismiss: () -> Unit,
    title: String,
    message: String,
    buttonText: String = "知道了"
) {
    if (visible) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            text = {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = Spacing.small),
                    horizontalArrangement = Arrangement.End
                ) {
                    BlockwisePrimaryButton(
                        text = buttonText,
                        onClick = onDismiss
                    )
                }
            },
            shape = RoundedCornerShape(CornerRadius.extraLarge),
            containerColor = MaterialTheme.colorScheme.surface,
            tonalElevation = 0.dp
        )
    }
}

/**
 * 对话框状态管理器
 */
class DialogState(initialVisible: Boolean = false) {
    var visible by mutableStateOf(initialVisible)
        private set

    fun show() {
        visible = true
    }

    fun hide() {
        visible = false
    }
}

/**
 * 记住对话框状态
 */
@Composable
fun rememberDialogState(initialVisible: Boolean = false): DialogState {
    return remember { DialogState(initialVisible) }
}

// =============================================================================
// Previews
// =============================================================================

@Preview(showBackground = true)
@Composable
private fun BlockwiseAlertDialogPreview() {
    BlockwiseTheme {
        BlockwiseAlertDialog(
            visible = true,
            onDismiss = {},
            title = "确认删除",
            message = "确定要删除这条记录吗？",
            onConfirm = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BlockwiseInfoDialogPreview() {
    BlockwiseTheme {
        BlockwiseInfoDialog(
            visible = true,
            onDismiss = {},
            title = "提示",
            message = "操作已完成"
        )
    }
}
