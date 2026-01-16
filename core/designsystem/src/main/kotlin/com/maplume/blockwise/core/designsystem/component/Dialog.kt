package com.maplume.blockwise.core.designsystem.component

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme

/**
 * Confirmation dialog with confirm and dismiss buttons
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
            title = { Text(text = title) },
            text = { Text(text = message) },
            confirmButton = {
                TextButton(onClick = {
                    onConfirm()
                    onDismiss()
                }) {
                    Text(confirmText)
                }
            },
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text(dismissText)
                }
            }
        )
    }
}

/**
 * Information dialog with single button
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
            title = { Text(text = title) },
            text = { Text(text = message) },
            confirmButton = {
                TextButton(onClick = onDismiss) {
                    Text(buttonText)
                }
            }
        )
    }
}

/**
 * Dialog state holder
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
 * Remember dialog state
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
