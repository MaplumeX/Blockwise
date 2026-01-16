package com.maplume.blockwise.core.designsystem.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme

/**
 * Basic top app bar with title
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockwiseTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = { Text(title) },
        modifier = modifier,
        actions = { actions() }
    )
}

/**
 * Top app bar with back navigation
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockwiseTopAppBarWithBack(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {}
) {
    TopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "返回"
                )
            }
        },
        actions = { actions() }
    )
}

/**
 * Center aligned top app bar
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockwiseCenterTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {}
) {
    CenterAlignedTopAppBar(
        title = { Text(title) },
        modifier = modifier,
        navigationIcon = navigationIcon,
        actions = { actions() }
    )
}

/**
 * Large top app bar with collapsing behavior
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockwiseLargeTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    scrollBehavior: TopAppBarScrollBehavior? = null,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {}
) {
    LargeTopAppBar(
        title = { Text(title) },
        modifier = modifier,
        scrollBehavior = scrollBehavior,
        navigationIcon = navigationIcon,
        actions = { actions() }
    )
}

// =============================================================================
// Previews
// =============================================================================

@Preview(showBackground = true)
@Composable
private fun BlockwiseTopAppBarPreview() {
    BlockwiseTheme {
        BlockwiseTopAppBar(title = "标题")
    }
}

@Preview(showBackground = true)
@Composable
private fun BlockwiseTopAppBarWithBackPreview() {
    BlockwiseTheme {
        BlockwiseTopAppBarWithBack(
            title = "详情",
            onBackClick = {}
        )
    }
}
