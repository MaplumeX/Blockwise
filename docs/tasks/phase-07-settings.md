---
文档类型: 阶段任务文档
阶段编号: 07
阶段名称: 设置与数据管理模块
版本: v1.0
创建日期: 2026-01-15
预计工期: 4-5天
前置条件: 阶段三完成
---

# 阶段七：设置与数据管理模块

## 1. 阶段目标

### 1.1 核心目标

实现 Blockwise 应用的设置功能和数据管理功能，包括应用偏好设置、主题切换、通知配置、数据导出导入、备份恢复等。

### 1.2 交付成果

- 设置主页面
- 主题设置（浅色/深色/跟随系统）
- 通知设置
- 数据导出（JSON/CSV）
- 数据导入
- 数据备份与恢复
- 关于页面

### 1.3 功能优先级

| 功能 | 优先级 | 说明 |
|------|--------|------|
| 设置主页面 | P0 | 设置入口 |
| 主题设置 | P0 | 用户体验 |
| 数据导出 | P0 | 数据安全 |
| 数据导入 | P1 | 数据迁移 |
| 备份恢复 | P1 | 数据保护 |
| 通知设置 | P1 | 个性化配置 |
| 关于页面 | P2 | 应用信息 |

---

## 2. 任务列表

### 2.1 设置偏好管理

#### T7.1.1 实现设置数据存储

| 属性 | 值 |
|------|-----|
| **任务ID** | T7.1.1 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T1.3.4 |

**任务描述**：
使用 DataStore Preferences 实现设置数据的持久化存储。

**关键代码**：
```kotlin
// SettingsDataStore.kt
package com.blockwise.core.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private object Keys {
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
        val TIMER_NOTIFICATION = booleanPreferencesKey("timer_notification")
        val GOAL_REMINDER = booleanPreferencesKey("goal_reminder")
        val DAILY_SUMMARY = booleanPreferencesKey("daily_summary")
        val DAILY_SUMMARY_TIME = stringPreferencesKey("daily_summary_time")
        val WEEK_START_DAY = intPreferencesKey("week_start_day")
        val AUTO_BACKUP = booleanPreferencesKey("auto_backup")
        val LAST_BACKUP_TIME = longPreferencesKey("last_backup_time")
    }

    val themeMode: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        ThemeMode.valueOf(prefs[Keys.THEME_MODE] ?: ThemeMode.SYSTEM.name)
    }

    val notificationEnabled: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.NOTIFICATION_ENABLED] ?: true
    }

    val weekStartDay: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.WEEK_START_DAY] ?: 1 // Monday
    }

    suspend fun setThemeMode(mode: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME_MODE] = mode.name
        }
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.NOTIFICATION_ENABLED] = enabled
        }
    }

    suspend fun setWeekStartDay(day: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.WEEK_START_DAY] = day
        }
    }
}

enum class ThemeMode {
    LIGHT, DARK, SYSTEM
}
```

**验收标准**：
- [ ] DataStore 正确配置
- [ ] 主题模式存储/读取
- [ ] 通知设置存储/读取
- [ ] 周起始日存储/读取

---

#### T7.1.2 实现设置 Repository

| 属性 | 值 |
|------|-----|
| **任务ID** | T7.1.2 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T7.1.1 |

**关键代码**：
```kotlin
// SettingsRepository.kt
interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun updateThemeMode(mode: ThemeMode)
    suspend fun updateNotificationEnabled(enabled: Boolean)
    suspend fun updateWeekStartDay(day: Int)
    suspend fun updateTimerNotification(enabled: Boolean)
    suspend fun updateGoalReminder(enabled: Boolean)
    suspend fun updateDailySummary(enabled: Boolean, time: LocalTime?)
}

// SettingsRepositoryImpl.kt
class SettingsRepositoryImpl @Inject constructor(
    private val dataStore: SettingsDataStore
) : SettingsRepository {

    override val settings: Flow<AppSettings> = combine(
        dataStore.themeMode,
        dataStore.notificationEnabled,
        dataStore.weekStartDay,
        dataStore.timerNotification,
        dataStore.goalReminder
    ) { theme, notif, weekStart, timer, goal ->
        AppSettings(
            themeMode = theme,
            notificationEnabled = notif,
            weekStartDay = weekStart,
            timerNotificationEnabled = timer,
            goalReminderEnabled = goal
        )
    }

    override suspend fun updateThemeMode(mode: ThemeMode) {
        dataStore.setThemeMode(mode)
    }

    // ... other implementations
}

data class AppSettings(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationEnabled: Boolean = true,
    val weekStartDay: Int = 1,
    val timerNotificationEnabled: Boolean = true,
    val goalReminderEnabled: Boolean = true,
    val dailySummaryEnabled: Boolean = false,
    val dailySummaryTime: LocalTime? = null
)
```

**验收标准**：
- [ ] Repository 接口定义
- [ ] 实现类完成
- [ ] Flow 组合正确

---

#### T7.1.3 实现设置 Use Cases

| 属性 | 值 |
|------|-----|
| **任务ID** | T7.1.3 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T7.1.2 |

**关键代码**：
```kotlin
// GetSettingsUseCase.kt
class GetSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<AppSettings> = repository.settings
}

// UpdateThemeModeUseCase.kt
class UpdateThemeModeUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(mode: ThemeMode) {
        repository.updateThemeMode(mode)
    }
}

// UpdateNotificationSettingsUseCase.kt
class UpdateNotificationSettingsUseCase @Inject constructor(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(
        enabled: Boolean? = null,
        timerNotification: Boolean? = null,
        goalReminder: Boolean? = null
    ) {
        enabled?.let { repository.updateNotificationEnabled(it) }
        timerNotification?.let { repository.updateTimerNotification(it) }
        goalReminder?.let { repository.updateGoalReminder(it) }
    }
}
```

**验收标准**：
- [ ] GetSettingsUseCase 实现
- [ ] UpdateThemeModeUseCase 实现
- [ ] UpdateNotificationSettingsUseCase 实现

---

#### T7.1.4 实现设置 ViewModel

| 属性 | 值 |
|------|-----|
| **任务ID** | T7.1.4 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T7.1.3 |

**关键代码**：
```kotlin
// SettingsViewModel.kt
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val getSettings: GetSettingsUseCase,
    private val updateThemeMode: UpdateThemeModeUseCase,
    private val updateNotification: UpdateNotificationSettingsUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            getSettings().collect { settings ->
                _uiState.update { it.copy(
                    themeMode = settings.themeMode,
                    notificationEnabled = settings.notificationEnabled,
                    timerNotification = settings.timerNotificationEnabled,
                    goalReminder = settings.goalReminderEnabled,
                    weekStartDay = settings.weekStartDay
                )}
            }
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch {
            updateThemeMode(mode)
        }
    }

    fun setNotificationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            updateNotification(enabled = enabled)
        }
    }
}

data class SettingsUiState(
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val notificationEnabled: Boolean = true,
    val timerNotification: Boolean = true,
    val goalReminder: Boolean = true,
    val weekStartDay: Int = 1
)
```

**验收标准**：
- [ ] 设置状态加载
- [ ] 主题切换功能
- [ ] 通知设置更新

---

#### T7.1.5 实现设置主页面

| 属性 | 值 |
|------|-----|
| **任务ID** | T7.1.5 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T7.1.4 |

**关键代码**：
```kotlin
@Composable
fun SettingsScreen(
    onNavigateToTheme: () -> Unit,
    onNavigateToNotification: () -> Unit,
    onNavigateToDataManagement: () -> Unit,
    onNavigateToAbout: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            BlockwiseTopAppBar(title = "设置")
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // 外观设置
            item {
                SettingsSection(title = "外观") {
                    SettingsItem(
                        icon = Icons.Default.Palette,
                        title = "主题",
                        subtitle = uiState.themeMode.displayName,
                        onClick = onNavigateToTheme
                    )
                }
            }

            // 通知设置
            item {
                SettingsSection(title = "通知") {
                    SettingsItem(
                        icon = Icons.Default.Notifications,
                        title = "通知设置",
                        subtitle = if (uiState.notificationEnabled) "已开启" else "已关闭",
                        onClick = onNavigateToNotification
                    )
                }
            }

            // 数据管理
            item {
                SettingsSection(title = "数据") {
                    SettingsItem(
                        icon = Icons.Default.Storage,
                        title = "数据管理",
                        subtitle = "导出、导入、备份",
                        onClick = onNavigateToDataManagement
                    )
                }
            }

            // 关于
            item {
                SettingsSection(title = "关于") {
                    SettingsItem(
                        icon = Icons.Default.Info,
                        title = "关于 Blockwise",
                        onClick = onNavigateToAbout
                    )
                }
            }
        }
    }
}
```

**验收标准**：
- [ ] 设置分组显示
- [ ] 各设置项入口正确
- [ ] 当前设置值显示

---

#### T7.1.6 实现主题选择页面

| 属性 | 值 |
|------|-----|
| **任务ID** | T7.1.6 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T7.1.5 |

**验收标准**：
- [ ] 三种主题模式选择
- [ ] 当前选中状态标识
- [ ] 切换后立即生效

---

#### T7.1.7 实现通知设置页面

| 属性 | 值 |
|------|-----|
| **任务ID** | T7.1.7 |
| **优先级** | P1 |
| **预计耗时** | 1.5h |
| **依赖任务** | T7.1.5 |

**验收标准**：
- [ ] 总开关控制
- [ ] 计时器通知开关
- [ ] 目标提醒开关
- [ ] 每日总结开关及时间设置

---

### 2.2 数据导出功能

#### T7.2.1 实现数据导出 Use Case

| 属性 | 值 |
|------|-----|
| **任务ID** | T7.2.1 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T3.4.8 |

**关键代码**：
```kotlin
// ExportDataUseCase.kt
class ExportDataUseCase @Inject constructor(
    private val timeEntryRepository: TimeEntryRepository,
    private val activityTypeRepository: ActivityTypeRepository,
    private val tagRepository: TagRepository,
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(
        format: ExportFormat,
        dateRange: ClosedRange<LocalDate>? = null
    ): Result<ExportData> {
        return try {
            val entries = if (dateRange != null) {
                timeEntryRepository.getByDateRange(dateRange.start, dateRange.endInclusive)
            } else {
                timeEntryRepository.getAll()
            }

            val data = ExportData(
                exportTime = Clock.System.now(),
                appVersion = BuildConfig.VERSION_NAME,
                activityTypes = activityTypeRepository.getAll().first(),
                tags = tagRepository.getAll().first(),
                timeEntries = entries,
                goals = goalRepository.getAllGoals().first()
            )

            Result.success(data)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}

data class ExportData(
    val exportTime: Instant,
    val appVersion: String,
    val activityTypes: List<ActivityType>,
    val tags: List<Tag>,
    val timeEntries: List<TimeEntry>,
    val goals: List<Goal>
)

enum class ExportFormat {
    JSON, CSV
}
```

**验收标准**：
- [ ] 支持全量导出
- [ ] 支持按日期范围导出
- [ ] 包含所有数据类型

---

#### T7.2.2 实现 JSON 导出器

| 属性 | 值 |
|------|-----|
| **任务ID** | T7.2.2 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T7.2.1 |

**关键代码**：
```kotlin
// JsonExporter.kt
class JsonExporter @Inject constructor(
    private val json: Json
) {
    fun export(data: ExportData): String {
        return json.encodeToString(data.toExportDto())
    }

    private fun ExportData.toExportDto() = ExportDto(
        metadata = ExportMetadata(
            exportTime = exportTime.toString(),
            appVersion = appVersion,
            format = "json",
            version = 1
        ),
        activityTypes = activityTypes.map { it.toDto() },
        tags = tags.map { it.toDto() },
        timeEntries = timeEntries.map { it.toDto() },
        goals = goals.map { it.toDto() }
    )
}

@Serializable
data class ExportDto(
    val metadata: ExportMetadata,
    val activityTypes: List<ActivityTypeDto>,
    val tags: List<TagDto>,
    val timeEntries: List<TimeEntryDto>,
    val goals: List<GoalDto>
)
```

**验收标准**：
- [ ] JSON 格式正确
- [ ] 包含元数据信息
- [ ] 数据完整性保证

---

#### T7.2.3 实现 CSV 导出器

| 属性 | 值 |
|------|-----|
| **任务ID** | T7.2.3 |
| **优先级** | P1 |
| **预计耗时** | 1.5h |
| **依赖任务** | T7.2.1 |

**验收标准**：
- [ ] CSV 格式正确
- [ ] 支持 UTF-8 BOM
- [ ] 时间记录表导出

---

#### T7.2.4 实现文件保存功能

| 属性 | 值 |
|------|-----|
| **任务ID** | T7.2.4 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T7.2.2 |

**关键代码**：
```kotlin
// FileExportManager.kt
class FileExportManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    suspend fun saveToFile(
        content: String,
        fileName: String,
        mimeType: String
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val values = ContentValues().apply {
                put(MediaStore.Downloads.DISPLAY_NAME, fileName)
                put(MediaStore.Downloads.MIME_TYPE, mimeType)
            }

            val uri = context.contentResolver.insert(
                MediaStore.Downloads.EXTERNAL_CONTENT_URI,
                values
            ) ?: return@withContext Result.failure(Exception("无法创建文件"))

            context.contentResolver.openOutputStream(uri)?.use { stream ->
                stream.write(content.toByteArray(Charsets.UTF_8))
            }

            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
```

**验收标准**：
- [ ] 保存到 Downloads 目录
- [ ] 文件名包含时间戳
- [ ] 返回文件 URI

---

#### T7.2.5 实现导出界面

| 属性 | 值 |
|------|-----|
| **任务ID** | T7.2.5 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T7.2.4 |

**验收标准**：
- [ ] 格式选择（JSON/CSV）
- [ ] 日期范围选择
- [ ] 导出进度显示
- [ ] 导出成功提示

---

### 2.3 数据导入功能

#### T7.3.1 实现数据导入 Use Case

| 属性 | 值 |
|------|-----|
| **任务ID** | T7.3.1 |
| **优先级** | P1 |
| **预计耗时** | 2h |
| **依赖任务** | T7.2.2 |

**关键代码**：
```kotlin
// ImportDataUseCase.kt
class ImportDataUseCase @Inject constructor(
    private val jsonImporter: JsonImporter,
    private val timeEntryRepository: TimeEntryRepository,
    private val activityTypeRepository: ActivityTypeRepository,
    private val tagRepository: TagRepository,
    private val goalRepository: GoalRepository
) {
    suspend operator fun invoke(
        content: String,
        strategy: ImportStrategy
    ): Result<ImportResult> {
        return try {
            val data = jsonImporter.parse(content)
                ?: return Result.failure(Exception("无法解析导入文件"))

            val result = when (strategy) {
                ImportStrategy.MERGE -> mergeData(data)
                ImportStrategy.REPLACE -> replaceData(data)
            }

            Result.success(result)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun mergeData(data: ExportDto): ImportResult {
        var imported = 0
        var skipped = 0

        // Import activity types
        data.activityTypes.forEach { dto ->
            if (!activityTypeRepository.isNameExists(dto.name)) {
                activityTypeRepository.create(dto.name, dto.colorHex, dto.icon, null)
                imported++
            } else {
                skipped++
            }
        }

        // Import tags, time entries, goals...
        return ImportResult(imported, skipped)
    }
}

enum class ImportStrategy {
    MERGE,   // 合并：跳过已存在的数据
    REPLACE  // 替换：清空后导入
}

data class ImportResult(
    val importedCount: Int,
    val skippedCount: Int
)
```

**验收标准**：
- [ ] JSON 解析正确
- [ ] 合并策略实现
- [ ] 替换策略实现
- [ ] 导入结果统计

---

#### T7.3.2 实现文件选择功能

| 属性 | 值 |
|------|-----|
| **任务ID** | T7.3.2 |
| **优先级** | P1 |
| **预计耗时** | 1h |
| **依赖任务** | T7.3.1 |

**验收标准**：
- [ ] 使用 SAF 选择文件
- [ ] 支持 JSON 文件类型
- [ ] 读取文件内容

---

#### T7.3.3 实现导入界面

| 属性 | 值 |
|------|-----|
| **任务ID** | T7.3.3 |
| **优先级** | P1 |
| **预计耗时** | 1.5h |
| **依赖任务** | T7.3.2 |

**验收标准**：
- [ ] 文件选择入口
- [ ] 导入策略选择
- [ ] 导入确认对话框
- [ ] 导入结果显示

---

### 2.4 数据备份与恢复

#### T7.4.1 实现自动备份功能

| 属性 | 值 |
|------|-----|
| **任务ID** | T7.4.1 |
| **优先级** | P1 |
| **预计耗时** | 2h |
| **依赖任务** | T7.2.1 |

**关键代码**：
```kotlin
// AutoBackupManager.kt
class AutoBackupManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val exportData: ExportDataUseCase,
    private val settingsDataStore: SettingsDataStore
) {
    private val backupDir = File(context.filesDir, "backups")

    suspend fun performBackup(): Result<File> {
        return try {
            if (!backupDir.exists()) backupDir.mkdirs()

            val data = exportData(ExportFormat.JSON, null).getOrThrow()
            val json = Json.encodeToString(data)

            val fileName = "backup_${System.currentTimeMillis()}.json"
            val file = File(backupDir, fileName)
            file.writeText(json)

            // 保留最近5个备份
            cleanOldBackups()

            settingsDataStore.setLastBackupTime(Clock.System.now())
            Result.success(file)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun cleanOldBackups() {
        val backups = backupDir.listFiles()?.sortedByDescending { it.lastModified() }
        backups?.drop(5)?.forEach { it.delete() }
    }

    fun getBackupList(): List<BackupInfo> {
        return backupDir.listFiles()
            ?.filter { it.extension == "json" }
            ?.map { BackupInfo(it.name, it.lastModified(), it.length()) }
            ?.sortedByDescending { it.timestamp }
            ?: emptyList()
    }
}
```

**验收标准**：
- [ ] 自动备份到应用目录
- [ ] 保留最近5个备份
- [ ] 记录最后备份时间

---

#### T7.4.2 实现备份恢复功能

| 属性 | 值 |
|------|-----|
| **任务ID** | T7.4.2 |
| **优先级** | P1 |
| **预计耗时** | 1.5h |
| **依赖任务** | T7.4.1, T7.3.1 |

**验收标准**：
- [ ] 列出可用备份
- [ ] 选择备份恢复
- [ ] 恢复确认对话框

---

#### T7.4.3 实现备份管理界面

| 属性 | 值 |
|------|-----|
| **任务ID** | T7.4.3 |
| **优先级** | P1 |
| **预计耗时** | 1.5h |
| **依赖任务** | T7.4.2 |

**验收标准**：
- [ ] 备份列表显示
- [ ] 手动备份按钮
- [ ] 恢复操作入口
- [ ] 删除备份功能

---

### 2.5 关于页面

#### T7.5.1 实现关于页面

| 属性 | 值 |
|------|-----|
| **任务ID** | T7.5.1 |
| **优先级** | P2 |
| **预计耗时** | 1h |
| **依赖任务** | T2.1.3 |

**关键代码**：
```kotlin
@Composable
fun AboutScreen(
    onNavigateBack: () -> Unit,
    onOpenPrivacyPolicy: () -> Unit,
    onOpenTerms: () -> Unit
) {
    Scaffold(
        topBar = {
            BlockwiseTopAppBarWithBack(
                title = "关于",
                onBackClick = onNavigateBack
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // App icon
            Image(
                painter = painterResource(R.drawable.ic_launcher),
                contentDescription = "App Icon",
                modifier = Modifier.size(80.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // App name
            Text(
                text = "Blockwise",
                style = MaterialTheme.typography.headlineMedium
            )

            // Version
            Text(
                text = "版本 ${BuildConfig.VERSION_NAME}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Links
            AboutLink(text = "隐私政策", onClick = onOpenPrivacyPolicy)
            AboutLink(text = "使用条款", onClick = onOpenTerms)
        }
    }
}
```

**验收标准**：
- [ ] 应用图标显示
- [ ] 版本号显示
- [ ] 隐私政策链接
- [ ] 使用条款链接

---

#### T7.5.2 实现开源许可页面

| 属性 | 值 |
|------|-----|
| **任务ID** | T7.5.2 |
| **优先级** | P2 |
| **预计耗时** | 1h |
| **依赖任务** | T7.5.1 |

**验收标准**：
- [ ] 列出使用的开源库
- [ ] 显示许可证信息

---

## 3. 依赖关系图

```
阶段三完成 (Repository)
        │
        └── T1.3.4 DataStore ─── T7.1.1 设置存储
                                    │
                                    └── T7.1.2 Repository
                                            │
                                            └── T7.1.3 Use Cases
                                                    │
                                                    └── T7.1.4 ViewModel
                                                            │
                                                            └── T7.1.5 设置主页面
                                                                    │
                                                            ┌───────┴───────┐
                                                            │               │
                                                    T7.1.6 主题      T7.1.7 通知

        T3.4.8 TimeEntryRepository ─── T7.2.1 导出UseCase
                                            │
                                    ┌───────┴───────┐
                                    │               │
                            T7.2.2 JSON导出   T7.2.3 CSV导出
                                    │
                                    └── T7.2.4 文件保存
                                            │
                                            └── T7.2.5 导出界面

        T7.2.2 JSON导出 ─── T7.3.1 导入UseCase
                                │
                                └── T7.3.2 文件选择
                                        │
                                        └── T7.3.3 导入界面

        T7.2.1 导出UseCase ─── T7.4.1 自动备份
                                    │
                                    └── T7.4.2 备份恢复
                                            │
                                            └── T7.4.3 备份管理界面

        T2.1.3 基础组件 ─── T7.5.1 关于页面
                                │
                                └── T7.5.2 开源许可
```

---

## 4. 验收标准清单

### 4.1 设置功能验收

| 验收项 | 要求 |
|--------|------|
| 设置存储 | DataStore 正确读写 |
| 主题切换 | 三种模式切换正常，立即生效 |
| 通知设置 | 各开关独立控制 |
| 设置页面 | 分组显示，导航正确 |

### 4.2 数据导出验收

| 验收项 | 要求 |
|--------|------|
| JSON 导出 | 格式正确，数据完整 |
| CSV 导出 | UTF-8 编码，表格正确 |
| 文件保存 | 保存到 Downloads，文件名含时间戳 |
| 导出界面 | 格式选择、日期范围、进度显示 |

### 4.3 数据导入验收

| 验收项 | 要求 |
|--------|------|
| JSON 解析 | 正确解析导出文件 |
| 合并策略 | 跳过已存在数据 |
| 替换策略 | 清空后导入 |
| 导入界面 | 文件选择、策略选择、结果显示 |

### 4.4 备份恢复验收

| 验收项 | 要求 |
|--------|------|
| 自动备份 | 保存到应用目录，保留5个 |
| 备份列表 | 显示时间、大小 |
| 恢复功能 | 确认后恢复，数据正确 |

---

## 5. 风险与注意事项

### 5.1 潜在风险

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 导入数据损坏 | 数据丢失 | 导入前自动备份 |
| 大文件导出卡顿 | 用户体验差 | 后台线程+进度显示 |
| 存储权限问题 | 无法保存文件 | 使用 MediaStore API |

### 5.2 注意事项

1. **数据安全**：导入替换模式需二次确认
2. **文件格式**：导出文件包含版本号便于兼容
3. **备份策略**：自动备份不超过5个，避免占用空间
4. **主题切换**：使用 AppCompatDelegate 确保全局生效

---

*文档版本: v1.0*
*阶段状态: 待开始*
