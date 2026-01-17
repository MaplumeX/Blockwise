package com.maplume.blockwise.feature.settings.presentation.data

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.CloudDownload
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.maplume.blockwise.feature.settings.domain.model.BackupInfo
import com.maplume.blockwise.feature.settings.domain.model.ExportFormat
import com.maplume.blockwise.feature.settings.domain.model.ImportStrategy
import com.maplume.blockwise.feature.settings.presentation.component.SettingsSection
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Data management screen for export, import, and backup operations.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataManagementScreen(
    onNavigateBack: () -> Unit,
    viewModel: DataManagementViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    var showRestoreConfirmDialog by remember { mutableStateOf<BackupInfo?>(null) }
    var showImportConfirmDialog by remember { mutableStateOf(false) }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let { viewModel.onFileSelected(it) }
    }

    LaunchedEffect(Unit) {
        viewModel.events.collect { event ->
            when (event) {
                is DataManagementEvent.ShowSnackbar -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                DataManagementEvent.RequestFileSelection -> {
                    filePickerLauncher.launch(arrayOf("application/json"))
                }
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据管理") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "返回"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(top = 8.dp)
        ) {
            // Export Section
            SettingsSection(title = "导出数据") {
                ExportSection(
                    format = uiState.exportFormat,
                    onFormatSelected = viewModel::onExportFormatSelected,
                    isExporting = uiState.isExporting,
                    onExportClick = viewModel::onExportClick
                )
            }

            // Import Section
            SettingsSection(title = "导入数据") {
                ImportSection(
                    strategy = uiState.importStrategy,
                    onStrategySelected = viewModel::onImportStrategySelected,
                    isImporting = uiState.isImporting,
                    onImportClick = {
                        if (uiState.importStrategy == ImportStrategy.REPLACE) {
                            showImportConfirmDialog = true
                        } else {
                            viewModel.onImportClick()
                        }
                    }
                )
            }

            // Backup Section
            SettingsSection(title = "备份与恢复") {
                BackupSection(
                    backups = uiState.backupList,
                    isLoadingBackups = uiState.isLoadingBackups,
                    isCreatingBackup = uiState.isCreatingBackup,
                    isRestoring = uiState.isRestoring,
                    onCreateBackupClick = viewModel::onCreateBackupClick,
                    onRestoreClick = { showRestoreConfirmDialog = it }
                )
            }
        }
    }

    // Restore Confirmation Dialog
    showRestoreConfirmDialog?.let { backup ->
        AlertDialog(
            onDismissRequest = { showRestoreConfirmDialog = null },
            title = { Text("确认恢复") },
            text = {
                Text("恢复将清除当前所有数据并替换为备份数据，此操作不可撤销。确定要继续吗？")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onRestoreBackupClick(backup)
                        showRestoreConfirmDialog = null
                    }
                ) {
                    Text("确认恢复")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRestoreConfirmDialog = null }) {
                    Text("取消")
                }
            }
        )
    }

    // Import Confirmation Dialog (for REPLACE strategy)
    if (showImportConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showImportConfirmDialog = false },
            title = { Text("确认导入") },
            text = {
                Text("替换模式将清除当前所有数据并替换为导入的数据。系统会自动创建备份。确定要继续吗？")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.onImportClick()
                        showImportConfirmDialog = false
                    }
                ) {
                    Text("确认导入")
                }
            },
            dismissButton = {
                TextButton(onClick = { showImportConfirmDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun ExportSection(
    format: ExportFormat,
    onFormatSelected: (ExportFormat) -> Unit,
    isExporting: Boolean,
    onExportClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "导出格式",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = format == ExportFormat.JSON,
                onClick = { onFormatSelected(ExportFormat.JSON) }
            )
            Text(
                text = "JSON (完整数据，支持导入)",
                modifier = Modifier.clickable { onFormatSelected(ExportFormat.JSON) }
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = format == ExportFormat.CSV,
                onClick = { onFormatSelected(ExportFormat.CSV) }
            )
            Text(
                text = "CSV (仅时间记录，可用Excel查看)",
                modifier = Modifier.clickable { onFormatSelected(ExportFormat.CSV) }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onExportClick,
            enabled = !isExporting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isExporting) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .height(16.dp)
                        .width(16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.CloudUpload,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text(if (isExporting) "导出中..." else "导出到下载目录")
        }
    }
}

@Composable
private fun ImportSection(
    strategy: ImportStrategy,
    onStrategySelected: (ImportStrategy) -> Unit,
    isImporting: Boolean,
    onImportClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = "导入策略",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = strategy == ImportStrategy.MERGE,
                onClick = { onStrategySelected(ImportStrategy.MERGE) }
            )
            Column(modifier = Modifier.clickable { onStrategySelected(ImportStrategy.MERGE) }) {
                Text(text = "合并")
                Text(
                    text = "保留现有数据，添加新数据",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = strategy == ImportStrategy.REPLACE,
                onClick = { onStrategySelected(ImportStrategy.REPLACE) }
            )
            Column(modifier = Modifier.clickable { onStrategySelected(ImportStrategy.REPLACE) }) {
                Text(text = "替换")
                Text(
                    text = "清除现有数据，完全替换（会自动备份）",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onImportClick,
            enabled = !isImporting,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isImporting) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .height(16.dp)
                        .width(16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.CloudDownload,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text(if (isImporting) "导入中..." else "选择 JSON 文件导入")
        }
    }
}

@Composable
private fun BackupSection(
    backups: List<BackupInfo>,
    isLoadingBackups: Boolean,
    isCreatingBackup: Boolean,
    isRestoring: Boolean,
    onCreateBackupClick: () -> Unit,
    onRestoreClick: (BackupInfo) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Button(
            onClick = onCreateBackupClick,
            enabled = !isCreatingBackup && !isRestoring,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isCreatingBackup) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .height(16.dp)
                        .width(16.dp),
                    strokeWidth = 2.dp
                )
            } else {
                Icon(
                    imageVector = Icons.Outlined.Backup,
                    contentDescription = null,
                    modifier = Modifier.padding(end = 8.dp)
                )
            }
            Text(if (isCreatingBackup) "创建中..." else "创建备份")
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "备份历史（最多保留5个）",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoadingBackups) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        } else if (backups.isEmpty()) {
            Text(
                text = "暂无备份",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            backups.forEach { backup ->
                BackupItem(
                    backup = backup,
                    isRestoring = isRestoring,
                    onRestoreClick = { onRestoreClick(backup) }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun BackupItem(
    backup: BackupInfo,
    isRestoring: Boolean,
    onRestoreClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                val dateTime = backup.createdAt.toLocalDateTime(TimeZone.currentSystemDefault())
                Text(
                    text = "${dateTime.date} ${dateTime.hour.toString().padStart(2, '0')}:${dateTime.minute.toString().padStart(2, '0')}",
                    style = MaterialTheme.typography.bodyMedium
                )
                Text(
                    text = backup.formattedSize,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            IconButton(
                onClick = onRestoreClick,
                enabled = !isRestoring
            ) {
                Icon(
                    imageVector = Icons.Outlined.Restore,
                    contentDescription = "恢复",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}
