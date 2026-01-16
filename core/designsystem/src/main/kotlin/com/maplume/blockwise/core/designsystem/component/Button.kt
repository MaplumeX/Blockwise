package com.maplume.blockwise.core.designsystem.component

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme
import com.maplume.blockwise.core.designsystem.theme.ComponentSize

/**
 * Primary button with loading state support
 */
@Composable
fun BlockwisePrimaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    loading: Boolean = false
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(ComponentSize.buttonHeight),
        enabled = enabled && !loading
    ) {
        if (loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                color = MaterialTheme.colorScheme.onPrimary,
                strokeWidth = 2.dp
            )
            Spacer(modifier = Modifier.width(8.dp))
        }
        Text(text = text)
    }
}

/**
 * Secondary outlined button
 */
@Composable
fun BlockwiseSecondaryButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(ComponentSize.buttonHeight),
        enabled = enabled
    ) {
        Text(text = text)
    }
}

/**
 * Text button for less prominent actions
 */
@Composable
fun BlockwiseTextButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Text(text = text)
    }
}

/**
 * Icon button with optional content description
 */
@Composable
fun BlockwiseIconButton(
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    contentDescription: String? = null,
    enabled: Boolean = true
) {
    IconButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription
        )
    }
}

// =============================================================================
// Previews
// =============================================================================

@Preview(showBackground = true)
@Composable
private fun BlockwisePrimaryButtonPreview() {
    BlockwiseTheme {
        BlockwisePrimaryButton(
            text = "Primary Button",
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BlockwisePrimaryButtonLoadingPreview() {
    BlockwiseTheme {
        BlockwisePrimaryButton(
            text = "Loading...",
            onClick = {},
            loading = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BlockwiseSecondaryButtonPreview() {
    BlockwiseTheme {
        BlockwiseSecondaryButton(
            text = "Secondary Button",
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun BlockwiseTextButtonPreview() {
    BlockwiseTheme {
        BlockwiseTextButton(
            text = "Text Button",
            onClick = {}
        )
    }
}
