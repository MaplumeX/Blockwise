package com.maplume.blockwise.feature.settings.presentation.main

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.HelpOutline
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.LocalOffer
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maplume.blockwise.feature.settings.presentation.component.SettingsItem
import com.maplume.blockwise.feature.settings.presentation.component.SettingsSection

/**
 * Settings screen composable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateToTheme: () -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToDataManagement: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToActivityTypes: () -> Unit,
    onNavigateToTags: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navigationEvent.collect { event ->
            when (event) {
                SettingsNavigationEvent.NavigateToTheme -> onNavigateToTheme()
                SettingsNavigationEvent.NavigateToNotification -> onNavigateToNotification()
                SettingsNavigationEvent.NavigateToDataManagement -> onNavigateToDataManagement()
                SettingsNavigationEvent.NavigateToAbout -> onNavigateToAbout()
                SettingsNavigationEvent.NavigateToActivityTypes -> onNavigateToActivityTypes()
                SettingsNavigationEvent.NavigateToTags -> onNavigateToTags()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = "设置")
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(top = 8.dp)
        ) {
            // Appearance Section
            item {
                SettingsSection(title = "外观") {
                    SettingsItem(
                        title = "主题",
                        subtitle = uiState.themeDisplayName,
                        leadingIcon = Icons.Outlined.DarkMode,
                        onClick = viewModel::onThemeClick,
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // Notification Section
            item {
                SettingsSection(title = "通知") {
                    SettingsItem(
                        title = "通知设置",
                        subtitle = "每日提醒、目标进度提醒",
                        leadingIcon = Icons.Outlined.Notifications,
                        onClick = viewModel::onNotificationClick,
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // Data Management Section
            item {
                SettingsSection(title = "数据管理") {
                    SettingsItem(
                        title = "活动类型管理",
                        subtitle = "管理时间记录的活动分类",
                        leadingIcon = Icons.Outlined.Category,
                        onClick = viewModel::onActivityTypesClick,
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    SettingsItem(
                        title = "标签管理",
                        subtitle = "管理时间记录的标签",
                        leadingIcon = Icons.Outlined.LocalOffer,
                        onClick = viewModel::onTagsClick,
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    SettingsItem(
                        title = "数据导入导出",
                        subtitle = "备份、恢复、导出数据",
                        leadingIcon = Icons.Outlined.CloudUpload,
                        onClick = viewModel::onDataManagementClick,
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }

            // About Section
            item {
                SettingsSection(title = "关于") {
                    SettingsItem(
                        title = "关于 Blockwise",
                        subtitle = "版本信息、开源许可",
                        leadingIcon = Icons.Outlined.Info,
                        onClick = viewModel::onAboutClick,
                        trailingContent = {
                            Icon(
                                imageVector = Icons.Outlined.ChevronRight,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }
            }
        }
    }
}
