package com.maplume.blockwise.core.designsystem.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.maplume.blockwise.core.designsystem.theme.BlockwiseTheme

/**
 * 顶部应用栏颜色配置 - 玻璃态效果
 * 半透明背景 + 细腻边框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun topAppBarColors(): TopAppBarColors {
    return TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
        scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

/**
 * 基础顶部应用栏 - 现代极简风格
 * 玻璃态效果 + 细腻边框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockwiseTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {}
) {
    Box(modifier = modifier) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            actions = { actions() },
            colors = topAppBarColors()
        )
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    }
}

/**
 * 带返回按钮的顶部应用栏 - 现代极简风格
 * 玻璃态效果 + 细腻边框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockwiseTopAppBarWithBack(
    title: String,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier,
    actions: @Composable () -> Unit = {}
) {
    Box(modifier = modifier) {
        TopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            navigationIcon = {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "返回"
                    )
                }
            },
            actions = { actions() },
            colors = topAppBarColors()
        )
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    }
}

/**
 * 居中对齐的顶部应用栏 - 现代极简风格
 * 玻璃态效果 + 细腻边框
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BlockwiseCenterTopAppBar(
    title: String,
    modifier: Modifier = Modifier,
    navigationIcon: @Composable () -> Unit = {},
    actions: @Composable () -> Unit = {}
) {
    Box(modifier = modifier) {
        CenterAlignedTopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge
                )
            },
            navigationIcon = navigationIcon,
            actions = { actions() },
            colors = topAppBarColors()
        )
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    }
}

/**
 * 大型顶部应用栏 - 现代极简风格
 * 可折叠行为 + 玻璃态效果
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
    Box(modifier = modifier) {
        LargeTopAppBar(
            title = {
                Text(
                    text = title,
                    style = MaterialTheme.typography.headlineMedium
                )
            },
            scrollBehavior = scrollBehavior,
            navigationIcon = navigationIcon,
            actions = { actions() },
            colors = TopAppBarDefaults.largeTopAppBarColors(
                containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f),
                scrolledContainerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.95f),
                navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
                titleContentColor = MaterialTheme.colorScheme.onSurface,
                actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.15f)
        )
    }
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
