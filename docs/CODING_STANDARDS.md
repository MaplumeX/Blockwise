# Blockwise 项目开发规范

> 版本：v1.0
> 最后更新：2025-01-15
> 适用范围：Blockwise Android 应用开发

---

## 目录

1. [代码风格规范](#1-代码风格规范)
2. [命名约定](#2-命名约定)
3. [目录结构规范](#3-目录结构规范)
4. [文档编写规范](#4-文档编写规范)

---

## 1. 代码风格规范

### 1.1 基础规范

本项目遵循 [Kotlin 官方编码规范](https://kotlinlang.org/docs/coding-conventions.html)，并在此基础上进行项目特定的补充。

#### 1.1.1 缩进与格式

```kotlin
// ✓ 正确：使用 4 个空格缩进
class TimeEntryRepository {
    fun getEntries(): Flow<List<TimeEntry>> {
        return dao.getAllEntries()
    }
}

// ✗ 错误：使用 Tab 或 2 个空格
class TimeEntryRepository {
  fun getEntries(): Flow<List<TimeEntry>> {
    return dao.getAllEntries()
  }
}
```

| 规则 | 值 |
|-----|-----|
| 缩进 | 4 个空格 |
| 最大行宽 | 120 字符 |
| 连续缩进 | 8 个空格 |
| 文件末尾 | 保留一个空行 |

#### 1.1.2 空行规则

```kotlin
class TimeEntryViewModel(
    private val getTimeEntriesUseCase: GetTimeEntriesUseCase,
    private val saveTimeEntryUseCase: SaveTimeEntryUseCase
) : ViewModel() {

    // 属性声明之间不需要空行
    private val _uiState = MutableStateFlow(TimeEntryUiState())
    val uiState: StateFlow<TimeEntryUiState> = _uiState.asStateFlow()

    // 方法之间保留一个空行
    fun loadEntries() {
        viewModelScope.launch {
            // ...
        }
    }

    fun saveEntry(entry: TimeEntry) {
        viewModelScope.launch {
            // ...
        }
    }
}
```

**空行使用规则：**
- 类声明与第一个成员之间：1 个空行
- 方法之间：1 个空行
- 属性声明之间：不需要空行
- 逻辑代码块之间：1 个空行
- import 语句分组之间：1 个空行

#### 1.1.3 Import 规范

```kotlin
// ✓ 正确：按类型分组，组间空行
package com.maplume.blockwise.feature.timeline

import android.os.Bundle
import androidx.activity.ComponentActivity

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*

import com.maplume.blockwise.core.domain.model.TimeEntry
import com.maplume.blockwise.core.domain.usecase.GetTimeEntriesUseCase

import javax.inject.Inject

import kotlinx.coroutines.flow.Flow
```

**Import 顺序：**
1. Android SDK (`android.*`)
2. AndroidX (`androidx.*`)
3. 项目内部类 (`com.maplume.blockwise.*`)
4. Java 标准库 (`java.*`, `javax.*`)
5. 第三方库 (`kotlinx.*`, 其他)

**禁止使用通配符 import（`*`）的例外：**
- Compose 布局相关：`androidx.compose.foundation.layout.*`
- Compose Material：`androidx.compose.material3.*`
- Compose Runtime：`androidx.compose.runtime.*`

### 1.2 Kotlin 特性使用

#### 1.2.1 空安全

```kotlin
// ✓ 正确：明确处理可空类型
fun getEntryTitle(entry: TimeEntry?): String {
    return entry?.title ?: "未命名"
}

// ✓ 正确：使用 let 进行空检查
entry?.let {
    saveEntry(it)
}

// ✗ 错误：滥用 !! 操作符
fun getEntryTitle(entry: TimeEntry?): String {
    return entry!!.title  // 危险！
}
```

**空安全规则：**
- 避免使用 `!!` 操作符，除非有 100% 把握不为 null
- 优先使用 `?.`、`?:` 和 `let` 处理可空类型
- ViewModel 暴露的数据应尽量使用非空类型

#### 1.2.2 数据类

```kotlin
// ✓ 正确：使用 data class 表示纯数据
data class TimeEntry(
    val id: Long = 0,
    val title: String,
    val startTime: Long,
    val endTime: Long,
    val activityTypeId: Long,
    val note: String = ""
)

// ✓ 正确：复杂数据类使用 Builder 模式或 copy()
val updatedEntry = entry.copy(
    title = "新标题",
    endTime = System.currentTimeMillis()
)
```

#### 1.2.3 扩展函数

```kotlin
// ✓ 正确：适度使用扩展函数提高可读性
fun Long.toFormattedDuration(): String {
    val hours = this / 3600000
    val minutes = (this % 3600000) / 60000
    return "${hours}h ${minutes}m"
}

// ✓ 正确：在合适的位置定义扩展函数
// 文件：core/common/src/.../extension/LongExtensions.kt
```

**扩展函数规则：**
- 通用扩展函数放在 `core/common` 模块
- 特定领域扩展函数放在对应 feature 模块
- 避免在顶层定义过多扩展函数导致命名冲突

#### 1.2.4 协程使用

```kotlin
// ✓ 正确：ViewModel 中使用 viewModelScope
class TimeEntryViewModel @Inject constructor(
    private val getTimeEntriesUseCase: GetTimeEntriesUseCase
) : ViewModel() {

    init {
        viewModelScope.launch {
            getTimeEntriesUseCase()
                .catch { e -> handleError(e) }
                .collect { entries ->
                    _uiState.update { it.copy(entries = entries) }
                }
        }
    }
}

// ✓ 正确：Repository 层使用 Dispatchers.IO
class TimeEntryRepositoryImpl(
    private val dao: TimeEntryDao,
    private val dispatcher: CoroutineDispatcher = Dispatchers.IO
) : TimeEntryRepository {

    override fun getEntries(): Flow<List<TimeEntry>> {
        return dao.getAllEntries()
            .flowOn(dispatcher)
    }
}
```

### 1.3 Jetpack Compose 规范

#### 1.3.1 Composable 函数结构

```kotlin
/**
 * 时间记录卡片组件
 *
 * @param entry 时间记录数据
 * @param onEntryClick 点击回调
 * @param modifier 修饰符
 */
@Composable
fun TimeEntryCard(
    entry: TimeEntry,
    onEntryClick: (TimeEntry) -> Unit,
    modifier: Modifier = Modifier  // Modifier 必须有默认值且放最后
) {
    // 1. remember 和状态声明
    var expanded by remember { mutableStateOf(false) }

    // 2. 副作用（如果有）
    LaunchedEffect(entry.id) {
        // ...
    }

    // 3. UI 结构
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onEntryClick(entry) },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        // 内容
    }
}
```

**Composable 函数规则：**
- 参数顺序：必需参数 → 可选参数 → 回调 → `modifier`
- `modifier` 参数必须有默认值 `Modifier`
- 函数名使用 PascalCase（大驼峰）
- 无状态组件优先，有状态组件提升状态

#### 1.3.2 状态管理

```kotlin
// ✓ 正确：状态提升模式
@Composable
fun TimelineScreen(
    viewModel: TimelineViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    TimelineContent(
        entries = uiState.entries,
        isLoading = uiState.isLoading,
        onEntryClick = viewModel::onEntryClick,
        onRefresh = viewModel::refresh
    )
}

@Composable
private fun TimelineContent(
    entries: List<TimeEntry>,
    isLoading: Boolean,
    onEntryClick: (TimeEntry) -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier
) {
    // 纯 UI，无状态
}
```

#### 1.3.3 Preview 规范

```kotlin
@Preview(
    name = "Light Mode",
    showBackground = true
)
@Preview(
    name = "Dark Mode",
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun TimeEntryCardPreview() {
    BlockwiseTheme {
        TimeEntryCard(
            entry = TimeEntry(
                id = 1,
                title = "阅读",
                startTime = System.currentTimeMillis() - 3600000,
                endTime = System.currentTimeMillis(),
                activityTypeId = 1
            ),
            onEntryClick = {}
        )
    }
}
```

**Preview 规则：**
- 为每个公开 Composable 提供 Preview
- 同时提供亮色和暗色模式 Preview
- Preview 函数使用 `private` 修饰
- Preview 函数命名：`{ComponentName}Preview`

#### 1.3.4 Modifier 链式调用

```kotlin
// ✓ 正确：每个 modifier 单独一行，按逻辑分组
Box(
    modifier = modifier
        // 尺寸
        .fillMaxWidth()
        .height(200.dp)
        // 内边距
        .padding(horizontal = 16.dp, vertical = 8.dp)
        // 背景
        .background(
            color = MaterialTheme.colorScheme.surface,
            shape = RoundedCornerShape(12.dp)
        )
        // 交互
        .clickable { onClick() }
)

// ✗ 错误：链式调用在一行
Box(modifier = modifier.fillMaxWidth().height(200.dp).padding(16.dp).clickable { onClick() })
```

### 1.4 架构层规范

#### 1.4.1 Domain 层

```kotlin
// Use Case 规范
class GetTimeEntriesUseCase @Inject constructor(
    private val repository: TimeEntryRepository
) {
    // 使用 invoke 操作符，简化调用
    operator fun invoke(
        startDate: Long? = null,
        endDate: Long? = null
    ): Flow<List<TimeEntry>> {
        return repository.getEntries(startDate, endDate)
    }
}

// Domain Model 规范
data class TimeEntry(
    val id: Long,
    val title: String,
    val duration: Duration,  // 使用领域概念
    val activityType: ActivityType,
    val tags: List<Tag>
)
```

#### 1.4.2 Data 层

```kotlin
// Entity 规范
@Entity(tableName = "time_entries")
data class TimeEntryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "title")
    val title: String,

    @ColumnInfo(name = "start_time")
    val startTime: Long,

    @ColumnInfo(name = "end_time")
    val endTime: Long,

    @ColumnInfo(name = "activity_type_id")
    val activityTypeId: Long,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis()
)

// DAO 规范
@Dao
interface TimeEntryDao {

    @Query("SELECT * FROM time_entries ORDER BY start_time DESC")
    fun getAllEntries(): Flow<List<TimeEntryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEntry(entry: TimeEntryEntity): Long

    @Delete
    suspend fun deleteEntry(entry: TimeEntryEntity)
}

// Repository 实现规范
class TimeEntryRepositoryImpl @Inject constructor(
    private val dao: TimeEntryDao,
    private val mapper: TimeEntryMapper
) : TimeEntryRepository {

    override fun getEntries(): Flow<List<TimeEntry>> {
        return dao.getAllEntries()
            .map { entities -> entities.map(mapper::toDomain) }
    }
}
```

#### 1.4.3 Presentation 层

```kotlin
// UI State 规范
data class TimelineUiState(
    val entries: List<TimeEntry> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedDate: LocalDate = LocalDate.now()
)

// ViewModel 规范
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val getTimeEntriesUseCase: GetTimeEntriesUseCase,
    private val deleteTimeEntryUseCase: DeleteTimeEntryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    // 单向数据流：UI Event -> ViewModel -> UI State
    fun onEvent(event: TimelineEvent) {
        when (event) {
            is TimelineEvent.LoadEntries -> loadEntries()
            is TimelineEvent.DeleteEntry -> deleteEntry(event.entryId)
            is TimelineEvent.SelectDate -> selectDate(event.date)
        }
    }

    private fun loadEntries() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            // ...
        }
    }
}

// Event 规范
sealed interface TimelineEvent {
    data object LoadEntries : TimelineEvent
    data class DeleteEntry(val entryId: Long) : TimelineEvent
    data class SelectDate(val date: LocalDate) : TimelineEvent
}
```

### 1.5 注释规范

#### 1.5.1 KDoc 注释

```kotlin
/**
 * 时间记录仓库接口
 *
 * 负责时间记录数据的持久化操作，提供 CRUD 功能。
 * 所有读取操作返回 [Flow]，支持响应式数据更新。
 *
 * @see TimeEntryRepositoryImpl 默认实现
 * @see TimeEntry 数据模型
 */
interface TimeEntryRepository {

    /**
     * 获取指定时间范围内的所有时间记录
     *
     * @param startDate 开始时间戳（毫秒），为 null 时不限制
     * @param endDate 结束时间戳（毫秒），为 null 时不限制
     * @return 时间记录流，按开始时间降序排列
     */
    fun getEntries(
        startDate: Long? = null,
        endDate: Long? = null
    ): Flow<List<TimeEntry>>

    /**
     * 保存时间记录
     *
     * 如果记录已存在（id > 0），则更新；否则插入新记录。
     *
     * @param entry 要保存的时间记录
     * @return 保存后的记录 ID
     * @throws IllegalArgumentException 如果 [entry.startTime] >= [entry.endTime]
     */
    suspend fun saveEntry(entry: TimeEntry): Long
}
```

#### 1.5.2 代码内注释

```kotlin
// ✓ 正确：解释"为什么"，而非"是什么"
// 使用 SharedPreferences 而非 DataStore，因为计时器服务需要同步读取
private val prefs = context.getSharedPreferences("timer", Context.MODE_PRIVATE)

// ✓ 正确：标记待办事项
// TODO: 实现时间块合并功能 (v1.1)
// FIXME: 深色模式下图表颜色对比度不足

// ✗ 错误：解释显而易见的代码
// 获取所有条目
val entries = repository.getEntries()
```

### 1.6 测试规范

```kotlin
// 单元测试命名：方法名_场景_期望结果
class TimeEntryRepositoryTest {

    @Test
    fun `getEntries with date range returns filtered entries`() = runTest {
        // Given
        val startDate = LocalDate.of(2025, 1, 1).toEpochMilli()
        val endDate = LocalDate.of(2025, 1, 31).toEpochMilli()

        // When
        val result = repository.getEntries(startDate, endDate).first()

        // Then
        assertThat(result).hasSize(5)
        assertThat(result).allMatch { it.startTime >= startDate }
    }

    @Test
    fun `saveEntry with invalid time range throws exception`() = runTest {
        // Given
        val invalidEntry = TimeEntry(
            startTime = 1000,
            endTime = 500  // end < start
        )

        // When & Then
        assertThrows<IllegalArgumentException> {
            repository.saveEntry(invalidEntry)
        }
    }
}

// Compose UI 测试
class TimeEntryCardTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun timeEntryCard_displaysCorrectTitle() {
        // Given
        val entry = TimeEntry(title = "阅读")

        // When
        composeTestRule.setContent {
            TimeEntryCard(entry = entry, onEntryClick = {})
        }

        // Then
        composeTestRule.onNodeWithText("阅读").assertIsDisplayed()
    }
}
```

---

## 2. 命名约定

### 2.1 总体原则

| 原则 | 说明 |
|-----|------|
| **清晰性** | 名称应准确描述其用途，避免歧义 |
| **一致性** | 相同概念使用相同命名模式 |
| **简洁性** | 在清晰的前提下尽量简短 |
| **可搜索** | 避免使用单字母名称（循环变量除外） |

### 2.2 Kotlin 命名规范

#### 2.2.1 包名

```kotlin
// ✓ 正确：全小写，用 . 分隔
package com.maplume.blockwise.feature.timeline
package com.maplume.blockwise.core.database

// ✗ 错误：使用下划线或大写
package com.maplume.blockwise.time_line
package com.maplume.blockwise.TimeLine
```

#### 2.2.2 类名

| 类型 | 命名规则 | 示例 |
|-----|---------|------|
| 普通类 | PascalCase | `TimeEntryManager` |
| 接口 | PascalCase | `TimeEntryRepository` |
| 抽象类 | `Abstract` 前缀（可选） | `AbstractUseCase` |
| 实现类 | `Impl` 后缀 | `TimeEntryRepositoryImpl` |
| 数据类 | PascalCase | `TimeEntry` |
| 密封类 | PascalCase | `TimelineEvent` |
| Entity | `Entity` 后缀 | `TimeEntryEntity` |
| DAO | `Dao` 后缀 | `TimeEntryDao` |
| ViewModel | `ViewModel` 后缀 | `TimelineViewModel` |
| UseCase | `UseCase` 后缀 | `GetTimeEntriesUseCase` |
| Mapper | `Mapper` 后缀 | `TimeEntryMapper` |
| 测试类 | `Test` 后缀 | `TimeEntryRepositoryTest` |

```kotlin
// 完整示例
interface TimeEntryRepository                    // 接口
class TimeEntryRepositoryImpl : TimeEntryRepository  // 实现

data class TimeEntry(...)                        // Domain Model
data class TimeEntryEntity(...)                  // Room Entity
data class TimeEntryDto(...)                     // 网络传输对象

class GetTimeEntriesUseCase(...)                 // Use Case
class TimelineViewModel(...)                     // ViewModel

sealed interface TimelineEvent {                 // UI Event
    data object LoadEntries : TimelineEvent
    data class SelectEntry(val id: Long) : TimelineEvent
}

data class TimelineUiState(...)                  // UI State
```

#### 2.2.3 函数名

```kotlin
// 普通函数：camelCase，动词开头
fun saveTimeEntry(entry: TimeEntry): Long
fun calculateDuration(start: Long, end: Long): Long
fun isValidTimeRange(start: Long, end: Long): Boolean

// Composable 函数：PascalCase，名词
@Composable
fun TimeEntryCard(entry: TimeEntry, ...)

@Composable
fun TimelineScreen(viewModel: TimelineViewModel, ...)

// 工厂函数：可以使用类名
fun TimeEntry(title: String, duration: Long): TimeEntry

// 测试函数：使用反引号包裹的描述性名称
@Test
fun `getEntries returns empty list when no data`()
```

#### 2.2.4 变量名

```kotlin
// 普通变量：camelCase
val timeEntry: TimeEntry
var isLoading: Boolean
val entriesList: List<TimeEntry>

// 常量：SCREAMING_SNAKE_CASE
const val MAX_ENTRIES_PER_PAGE = 50
const val DEFAULT_DURATION_MINUTES = 30

// 伴生对象常量
companion object {
    private const val TAG = "TimelineViewModel"
    const val KEY_SELECTED_DATE = "selected_date"
}

// 私有 backing 属性：_ 前缀
private val _uiState = MutableStateFlow(TimelineUiState())
val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

// Flow/LiveData：无特殊后缀
val entries: Flow<List<TimeEntry>>        // ✓
val entriesFlow: Flow<List<TimeEntry>>    // ✗ 冗余
```

#### 2.2.5 参数名

```kotlin
// 函数参数：camelCase，描述性命名
fun getEntriesByDateRange(
    startDate: Long,          // ✓ 清晰
    endDate: Long,            // ✓ 清晰
    includeDeleted: Boolean   // ✓ 清晰
)

// Lambda 参数：描述性命名（非 it）
entries.filter { entry -> entry.isCompleted }  // ✓
entries.filter { it.isCompleted }              // ✓ 简单场景可用 it

// 回调参数：on + 动词
onEntryClick: (TimeEntry) -> Unit
onDateSelected: (LocalDate) -> Unit
onDismiss: () -> Unit
```

### 2.3 资源命名规范

#### 2.3.1 布局资源（如使用 XML）

```
格式：{模块}_{组件类型}_{描述}

activity_main.xml
fragment_timeline.xml
item_time_entry.xml
dialog_confirm_delete.xml
view_duration_picker.xml
```

#### 2.3.2 Drawable 资源

```
格式：{类型}_{描述}[_{状态}]

ic_add_24.xml              // 图标，24dp
ic_timer_start.xml         // 图标
ic_timer_stop.xml          // 图标
bg_card_rounded.xml        // 背景
bg_button_primary.xml      // 背景
shape_circle.xml           // 形状
selector_button.xml        // 选择器
```

#### 2.3.3 字符串资源

```xml
<!-- 格式：{模块}_{描述} -->

<!-- 通用 -->
<string name="app_name">Blockwise</string>
<string name="action_save">保存</string>
<string name="action_cancel">取消</string>
<string name="action_delete">删除</string>

<!-- Timeline 模块 -->
<string name="timeline_title">时间线</string>
<string name="timeline_empty_message">暂无记录</string>
<string name="timeline_entry_duration">%1$d 小时 %2$d 分钟</string>

<!-- Statistics 模块 -->
<string name="statistics_title">统计</string>
<string name="statistics_daily">日统计</string>
<string name="statistics_weekly">周统计</string>

<!-- 错误信息 -->
<string name="error_network">网络连接失败</string>
<string name="error_invalid_time">时间范围无效</string>

<!-- 格式化字符串 -->
<string name="format_duration_hours_minutes">%1$d小时%2$d分钟</string>
<plurals name="format_entries_count">
    <item quantity="other">%d 条记录</item>
</plurals>
```

#### 2.3.4 颜色资源

```xml
<!-- 格式：{类型}_{描述}[_{变体}] -->

<!-- 品牌色 -->
<color name="brand_primary">#6650A4</color>
<color name="brand_secondary">#625B71</color>

<!-- 语义色 -->
<color name="semantic_success">#4CAF50</color>
<color name="semantic_warning">#FF9800</color>
<color name="semantic_error">#F44336</color>

<!-- 文字色 -->
<color name="text_primary">#1C1B1F</color>
<color name="text_secondary">#49454F</color>
<color name="text_disabled">#1C1B1F60</color>

<!-- 背景色 -->
<color name="background_primary">#FFFBFE</color>
<color name="background_surface">#FFFBFE</color>
```

#### 2.3.5 Dimen 资源

```xml
<!-- 格式：{用途}_{尺寸描述} -->

<!-- 间距 -->
<dimen name="spacing_xs">4dp</dimen>
<dimen name="spacing_sm">8dp</dimen>
<dimen name="spacing_md">16dp</dimen>
<dimen name="spacing_lg">24dp</dimen>
<dimen name="spacing_xl">32dp</dimen>

<!-- 字体大小 -->
<dimen name="text_size_body">14sp</dimen>
<dimen name="text_size_title">20sp</dimen>
<dimen name="text_size_headline">24sp</dimen>

<!-- 圆角 -->
<dimen name="corner_radius_sm">4dp</dimen>
<dimen name="corner_radius_md">8dp</dimen>
<dimen name="corner_radius_lg">16dp</dimen>

<!-- 组件尺寸 -->
<dimen name="button_height">48dp</dimen>
<dimen name="icon_size_sm">16dp</dimen>
<dimen name="icon_size_md">24dp</dimen>
<dimen name="icon_size_lg">48dp</dimen>
```

### 2.4 数据库命名规范

#### 2.4.1 表名

```kotlin
// 格式：snake_case，复数形式
@Entity(tableName = "time_entries")
@Entity(tableName = "activity_types")
@Entity(tableName = "tags")
@Entity(tableName = "time_entry_tags")  // 关联表
```

#### 2.4.2 列名

```kotlin
// 格式：snake_case
@ColumnInfo(name = "id")
@ColumnInfo(name = "start_time")
@ColumnInfo(name = "end_time")
@ColumnInfo(name = "activity_type_id")  // 外键
@ColumnInfo(name = "created_at")
@ColumnInfo(name = "updated_at")
@ColumnInfo(name = "is_deleted")        // 布尔值用 is_ 前缀
```

#### 2.4.3 索引名

```kotlin
// 格式：idx_{表名}_{列名}
@Entity(
    tableName = "time_entries",
    indices = [
        Index(name = "idx_time_entries_start_time", value = ["start_time"]),
        Index(name = "idx_time_entries_activity_type_id", value = ["activity_type_id"])
    ]
)
```

### 2.5 特殊命名场景

#### 2.5.1 缩写处理

```kotlin
// 缩写词作为普通单词处理
class HttpClient          // ✓
class HTTPClient          // ✗

fun parseJson(): String   // ✓
fun parseJSON(): String   // ✗

val xmlParser: XmlParser  // ✓
val XMLParser: XMLParser  // ✗

// 例外：常见两字母缩写全大写
val ioDispatcher: CoroutineDispatcher
val uiState: StateFlow<UiState>
```

#### 2.5.2 布尔值命名

```kotlin
// 使用 is/has/can/should 前缀
val isLoading: Boolean
val hasError: Boolean
val canEdit: Boolean
val shouldRefresh: Boolean

// 函数返回布尔值
fun isValidTimeRange(): Boolean
fun hasUnsavedChanges(): Boolean
fun canDeleteEntry(): Boolean
```

#### 2.5.3 集合命名

```kotlin
// 使用复数或集合类型后缀
val entries: List<TimeEntry>           // ✓ 复数
val entryList: List<TimeEntry>         // ✓ 带类型后缀
val tagSet: Set<Tag>                   // ✓ 带类型后缀
val entryMap: Map<Long, TimeEntry>     // ✓ 带类型后缀

// 避免歧义
val selectedEntries: List<TimeEntry>   // ✓ 清晰
val selected: List<TimeEntry>          // ✗ 不清晰
```

---

## 3. 目录结构规范

### 3.1 项目整体结构

```
Blockwise/
├── app/                              # 应用主模块
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/maplume/blockwise/
│   │   │   │   ├── BlockwiseApplication.kt  # Application 类
│   │   │   │   ├── MainActivity.kt          # 主 Activity
│   │   │   │   └── navigation/              # 导航配置
│   │   │   │       └── BlockwiseNavHost.kt
│   │   │   ├── res/                         # 应用级资源
│   │   │   └── AndroidManifest.xml
│   │   ├── test/                            # 单元测试
│   │   └── androidTest/                     # 仪器测试
│   └── build.gradle.kts
│
├── core/                             # 核心模块（被所有 feature 依赖）
│   ├── common/                       # 通用工具和扩展
│   ├── database/                     # Room 数据库
│   ├── datastore/                    # DataStore 偏好存储
│   ├── domain/                       # 领域模型和用例
│   ├── ui/                           # 通用 UI 组件
│   └── testing/                      # 测试工具
│
├── feature/                          # 功能模块
│   ├── timeline/                     # 时间线功能
│   ├── record/                       # 时间记录功能
│   ├── statistics/                   # 统计分析功能
│   ├── goals/                        # 目标管理功能
│   └── settings/                     # 设置功能
│
├── docs/                             # 项目文档
│   ├── PRD.md                        # 产品需求文档
│   ├── 技术框架方案.md               # 技术框架设计
│   ├── CODING_STANDARDS.md           # 开发规范（本文档）
│   └── prototype/                    # 原型设计
│
├── build.gradle.kts                  # 根 Gradle 配置
├── settings.gradle.kts               # Gradle 设置
├── gradle/
│   └── libs.versions.toml            # 依赖版本管理
└── README.md                         # 项目说明
```

### 3.2 Core 模块结构

#### 3.2.1 core/common

```
core/common/
├── src/main/java/com/maplume/blockwise/core/common/
│   ├── di/                           # 依赖注入
│   │   └── CommonModule.kt
│   ├── extension/                    # 扩展函数
│   │   ├── DateExtensions.kt
│   │   ├── FlowExtensions.kt
│   │   └── StringExtensions.kt
│   ├── result/                       # 结果封装
│   │   └── Result.kt
│   └── util/                         # 工具类
│       ├── DateTimeUtils.kt
│       └── ValidationUtils.kt
└── build.gradle.kts
```

#### 3.2.2 core/database

```
core/database/
├── src/main/java/com/maplume/blockwise/core/database/
│   ├── di/                           # 依赖注入
│   │   └── DatabaseModule.kt
│   ├── dao/                          # 数据访问对象
│   │   ├── TimeEntryDao.kt
│   │   ├── ActivityTypeDao.kt
│   │   └── TagDao.kt
│   ├── entity/                       # 数据库实体
│   │   ├── TimeEntryEntity.kt
│   │   ├── ActivityTypeEntity.kt
│   │   ├── TagEntity.kt
│   │   └── relation/                 # 关联实体
│   │       └── TimeEntryWithTags.kt
│   ├── converter/                    # 类型转换器
│   │   └── DateConverters.kt
│   ├── migration/                    # 数据库迁移
│   │   └── Migration1To2.kt
│   └── BlockwiseDatabase.kt          # 数据库定义
└── build.gradle.kts
```

#### 3.2.3 core/domain

```
core/domain/
├── src/main/java/com/maplume/blockwise/core/domain/
│   ├── model/                        # 领域模型
│   │   ├── TimeEntry.kt
│   │   ├── ActivityType.kt
│   │   ├── Tag.kt
│   │   └── Goal.kt
│   ├── repository/                   # 仓库接口
│   │   ├── TimeEntryRepository.kt
│   │   ├── ActivityTypeRepository.kt
│   │   └── TagRepository.kt
│   └── usecase/                      # 用例
│       ├── timeentry/
│       │   ├── GetTimeEntriesUseCase.kt
│       │   ├── SaveTimeEntryUseCase.kt
│       │   └── DeleteTimeEntryUseCase.kt
│       ├── statistics/
│       │   ├── GetDailyStatisticsUseCase.kt
│       │   └── GetWeeklyStatisticsUseCase.kt
│       └── goal/
│           └── CheckGoalProgressUseCase.kt
└── build.gradle.kts
```

#### 3.2.4 core/ui

```
core/ui/
├── src/main/java/com/maplume/blockwise/core/ui/
│   ├── component/                    # 通用 UI 组件
│   │   ├── BlockwiseButton.kt
│   │   ├── BlockwiseCard.kt
│   │   ├── BlockwiseDialog.kt
│   │   ├── DurationPicker.kt
│   │   └── LoadingIndicator.kt
│   ├── theme/                        # 主题定义
│   │   ├── Color.kt
│   │   ├── Theme.kt
│   │   ├── Type.kt
│   │   └── Shape.kt
│   └── icon/                         # 图标定义
│       └── BlockwiseIcons.kt
├── src/main/res/
│   ├── values/
│   │   ├── colors.xml
│   │   ├── dimens.xml
│   │   └── strings.xml
│   └── drawable/
└── build.gradle.kts
```

### 3.3 Feature 模块结构

每个 Feature 模块采用统一的内部结构：

```
feature/timeline/
├── src/
│   ├── main/java/com/maplume/blockwise/feature/timeline/
│   │   ├── di/                       # 依赖注入
│   │   │   └── TimelineModule.kt
│   │   ├── data/                     # 数据层实现
│   │   │   ├── repository/
│   │   │   │   └── TimelineRepositoryImpl.kt
│   │   │   └── mapper/
│   │   │       └── TimeEntryMapper.kt
│   │   ├── navigation/               # 导航
│   │   │   └── TimelineNavigation.kt
│   │   └── ui/                       # UI 层
│   │       ├── TimelineScreen.kt     # 主屏幕
│   │       ├── TimelineViewModel.kt  # ViewModel
│   │       ├── TimelineUiState.kt    # UI 状态
│   │       ├── TimelineEvent.kt      # UI 事件
│   │       └── component/            # 屏幕内组件
│   │           ├── TimeEntryItem.kt
│   │           ├── DateHeader.kt
│   │           └── EmptyState.kt
│   ├── test/                         # 单元测试
│   │   └── java/com/maplume/blockwise/feature/timeline/
│   │       ├── ui/
│   │       │   └── TimelineViewModelTest.kt
│   │       └── data/
│   │           └── TimelineRepositoryImplTest.kt
│   └── androidTest/                  # UI 测试
│       └── java/com/maplume/blockwise/feature/timeline/
│           └── TimelineScreenTest.kt
└── build.gradle.kts
```

### 3.4 模块依赖规则

```
┌─────────────────────────────────────────────────────┐
│                        app                          │
│         (依赖所有 feature 和 core 模块)               │
└─────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────┐
│                     feature/*                       │
│    timeline │ record │ statistics │ goals │ settings │
│         (仅依赖 core 模块，不互相依赖)                 │
└─────────────────────────────────────────────────────┘
                          │
                          ▼
┌─────────────────────────────────────────────────────┐
│                       core/*                        │
│   common │ database │ datastore │ domain │ ui       │
│              (可互相依赖，无循环依赖)                  │
└─────────────────────────────────────────────────────┘
```

**依赖规则：**

1. **app 模块**
   - 依赖所有 `feature/*` 模块
   - 依赖所有 `core/*` 模块
   - 负责组装和导航

2. **feature 模块**
   - 只能依赖 `core/*` 模块
   - 不能依赖其他 `feature` 模块
   - 通过 app 模块实现 feature 间通信

3. **core 模块**
   - `core/common`: 无依赖，被所有模块依赖
   - `core/domain`: 依赖 `core/common`
   - `core/database`: 依赖 `core/common`
   - `core/ui`: 依赖 `core/common`
   - 避免循环依赖

### 3.5 文件组织原则

#### 3.5.1 单一职责

```
✓ 正确：一个文件一个主要类
TimeEntryDao.kt          → interface TimeEntryDao
TimeEntryEntity.kt       → data class TimeEntryEntity
TimeEntryMapper.kt       → class TimeEntryMapper

✗ 错误：一个文件多个不相关类
TimeEntryStuff.kt        → TimeEntryDao + TimeEntryEntity + TimeEntryMapper
```

#### 3.5.2 相关类可以放同一文件

```kotlin
// TimelineUiState.kt - 相关的密封类和数据类可以放一起
data class TimelineUiState(
    val entries: List<TimeEntry> = emptyList(),
    val isLoading: Boolean = false
)

sealed interface TimelineEvent {
    data object LoadEntries : TimelineEvent
    data class SelectEntry(val id: Long) : TimelineEvent
}
```

#### 3.5.3 文件命名与类名一致

```
TimeEntryRepository.kt       → interface TimeEntryRepository
TimeEntryRepositoryImpl.kt   → class TimeEntryRepositoryImpl
GetTimeEntriesUseCase.kt     → class GetTimeEntriesUseCase
```

---

## 4. 文档编写规范

### 4.1 文档类型与位置

| 文档类型 | 位置 | 说明 |
|---------|------|------|
| 项目说明 | `/README.md` | 项目概述、快速开始 |
| 产品需求 | `/docs/PRD.md` | 产品需求文档 |
| 技术方案 | `/docs/技术框架方案.md` | 架构设计、技术选型 |
| 开发规范 | `/docs/CODING_STANDARDS.md` | 本文档 |
| API 文档 | `/docs/api/` | 接口文档（如有） |
| 变更日志 | `/CHANGELOG.md` | 版本变更记录 |
| 贡献指南 | `/CONTRIBUTING.md` | 贡献流程（如开源） |

### 4.2 Markdown 格式规范

#### 4.2.1 标题层级

```markdown
# 一级标题（文档标题，仅一个）

## 二级标题（主要章节）

### 三级标题（子章节）

#### 四级标题（细分内容）
```

**规则：**
- 每个文档只有一个一级标题
- 标题层级不跳跃（不要从二级直接到四级）
- 标题前后保留空行

#### 4.2.2 列表格式

```markdown
<!-- 无序列表 -->
- 第一项
- 第二项
  - 嵌套项
  - 嵌套项
- 第三项

<!-- 有序列表 -->
1. 第一步
2. 第二步
3. 第三步

<!-- 任务列表 -->
- [x] 已完成任务
- [ ] 待完成任务
```

#### 4.2.3 代码块

````markdown
<!-- 行内代码 -->
使用 `TimeEntry` 类表示时间记录。

<!-- 代码块：指定语言 -->
```kotlin
data class TimeEntry(
    val id: Long,
    val title: String
)
```

<!-- 代码块：带文件路径 -->
```kotlin
// core/domain/model/TimeEntry.kt
data class TimeEntry(
    val id: Long,
    val title: String
)
```
````

#### 4.2.4 表格格式

```markdown
| 列1 | 列2 | 列3 |
|-----|-----|-----|
| 内容 | 内容 | 内容 |
| 内容 | 内容 | 内容 |

<!-- 对齐方式 -->
| 左对齐 | 居中 | 右对齐 |
|:-------|:----:|-------:|
| 内容   | 内容 | 内容   |
```

#### 4.2.5 链接和图片

```markdown
<!-- 链接 -->
[链接文字](URL)
[Kotlin 官方文档](https://kotlinlang.org/docs/)

<!-- 内部链接 -->
[查看代码风格规范](#1-代码风格规范)

<!-- 图片 -->
![图片描述](./images/screenshot.png)

<!-- 带尺寸的图片（HTML） -->
<img src="./images/screenshot.png" width="400" alt="截图">
```

### 4.3 README.md 模板

```markdown
# Blockwise

基于柳比歇夫时间管理法的 Android 时间追踪应用。

## 功能特性

- 📝 精确的时间记录
- 📊 多维度统计分析
- 🎯 目标管理与追踪
- 🌙 深色模式支持

## 技术栈

- **语言**: Kotlin 2.0
- **UI**: Jetpack Compose + Material Design 3
- **架构**: Clean Architecture + MVVM
- **数据库**: Room
- **依赖注入**: Hilt
- **异步**: Kotlin Coroutines + Flow

## 快速开始

### 环境要求

- Android Studio Ladybug 或更高版本
- JDK 11+
- Android SDK 36

### 构建项目

```bash
# 克隆项目
git clone https://github.com/maplume/blockwise.git

# 进入目录
cd blockwise

# 构建 Debug 版本
./gradlew assembleDebug
```

### 运行测试

```bash
# 单元测试
./gradlew test

# 仪器测试
./gradlew connectedAndroidTest
```

## 项目结构

```
Blockwise/
├── app/          # 应用入口
├── core/         # 核心模块
├── feature/      # 功能模块
└── docs/         # 文档
```

详见 [目录结构规范](docs/CODING_STANDARDS.md#3-目录结构规范)

## 文档

- [产品需求文档](docs/PRD.md)
- [技术框架方案](docs/技术框架方案.md)
- [开发规范](docs/CODING_STANDARDS.md)

## 版本历史

查看 [CHANGELOG.md](CHANGELOG.md)

## 许可证

[MIT License](LICENSE)
```

### 4.4 代码文档规范

#### 4.4.1 模块 README

每个核心模块应包含 README.md：

```markdown
# core/database

Room 数据库模块，提供本地数据持久化功能。

## 依赖

- `core:common`
- `androidx.room:room-runtime`
- `androidx.room:room-ktx`

## 主要组件

| 组件 | 说明 |
|-----|------|
| `BlockwiseDatabase` | 数据库定义 |
| `TimeEntryDao` | 时间记录 DAO |
| `TimeEntryEntity` | 时间记录实体 |

## 使用方式

```kotlin
@Inject
lateinit var timeEntryDao: TimeEntryDao

// 获取所有记录
timeEntryDao.getAllEntries()
    .collect { entries ->
        // 处理数据
    }
```

## 数据库迁移

迁移文件位于 `migration/` 目录，命名格式：`Migration{from}To{to}.kt`
```

#### 4.4.2 复杂算法文档

```kotlin
/**
 * 时间块冲突检测算法
 *
 * ## 算法说明
 *
 * 检测新时间记录是否与现有记录存在时间重叠。
 *
 * ## 时间复杂度
 *
 * O(n)，其中 n 为同一天的记录数量。
 *
 * ## 使用示例
 *
 * ```kotlin
 * val conflicts = detectConflicts(newEntry, existingEntries)
 * if (conflicts.isNotEmpty()) {
 *     showConflictDialog(conflicts)
 * }
 * ```
 *
 * @param newEntry 待检测的新记录
 * @param existingEntries 现有记录列表
 * @return 冲突的记录列表，无冲突时返回空列表
 */
fun detectConflicts(
    newEntry: TimeEntry,
    existingEntries: List<TimeEntry>
): List<TimeEntry> {
    // 实现...
}
```

### 4.5 变更日志规范

遵循 [Keep a Changelog](https://keepachangelog.com/) 格式：

```markdown
# Changelog

本项目的所有重要变更都将记录在此文件中。

格式基于 [Keep a Changelog](https://keepachangelog.com/zh-CN/1.0.0/)，
版本号遵循 [语义化版本](https://semver.org/lang/zh-CN/)。

## [Unreleased]

### Added
- 新增时间块合并功能

### Changed
- 优化统计图表渲染性能

### Fixed
- 修复深色模式下文字对比度问题

## [1.0.0] - 2025-01-15

### Added
- 时间记录功能：手动记录、计时器记录
- 时间线视图：按日期展示记录
- 统计分析：日/周/月统计
- 目标管理：设定和追踪时间目标
- 深色模式支持

### Security
- 数据本地加密存储

[Unreleased]: https://github.com/maplume/blockwise/compare/v1.0.0...HEAD
[1.0.0]: https://github.com/maplume/blockwise/releases/tag/v1.0.0
```

**变更类型：**
- `Added` - 新功能
- `Changed` - 现有功能的变更
- `Deprecated` - 即将移除的功能
- `Removed` - 已移除的功能
- `Fixed` - Bug 修复
- `Security` - 安全相关修复

### 4.6 Git Commit 规范

遵循 [Conventional Commits](https://www.conventionalcommits.org/) 规范：

```
<type>(<scope>): <subject>

<body>

<footer>
```

#### 4.6.1 Type 类型

| Type | 说明 | 示例 |
|------|------|------|
| `feat` | 新功能 | `feat(timeline): add time entry merge` |
| `fix` | Bug 修复 | `fix(record): correct duration calculation` |
| `docs` | 文档变更 | `docs: update README` |
| `style` | 代码格式 | `style: fix indentation` |
| `refactor` | 重构 | `refactor(database): simplify DAO queries` |
| `perf` | 性能优化 | `perf(statistics): optimize chart rendering` |
| `test` | 测试相关 | `test(timeline): add ViewModel tests` |
| `chore` | 构建/工具 | `chore: update dependencies` |

#### 4.6.2 Scope 范围

```
feat(timeline): ...      # timeline 模块
feat(record): ...        # record 模块
feat(statistics): ...    # statistics 模块
feat(goals): ...         # goals 模块
feat(database): ...      # database 模块
feat(ui): ...            # ui 模块
fix(app): ...            # app 模块
```

#### 4.6.3 示例

```
feat(timeline): add swipe-to-delete gesture

Implement swipe gesture on time entry items to enable quick deletion.
Uses Material3 SwipeToDismiss component.

Closes #123
```

```
fix(record): correct negative duration display

Duration was showing negative values when end time was manually
set before start time. Added validation to prevent this case.

Fixes #456
```

### 4.7 Issue 和 PR 模板

#### 4.7.1 Bug Report 模板

```markdown
## Bug 描述

简要描述遇到的问题。

## 复现步骤

1. 进入 '...'
2. 点击 '...'
3. 滚动到 '...'
4. 看到错误

## 期望行为

描述你期望发生的情况。

## 实际行为

描述实际发生的情况。

## 截图

如适用，添加截图帮助说明问题。

## 环境信息

- 设备: [例如 Pixel 6]
- Android 版本: [例如 Android 14]
- App 版本: [例如 1.0.0]

## 额外信息

添加任何其他相关信息。
```

#### 4.7.2 Pull Request 模板

```markdown
## 变更说明

简要描述此 PR 的变更内容。

## 变更类型

- [ ] Bug 修复（非破坏性变更，修复问题）
- [ ] 新功能（非破坏性变更，添加功能）
- [ ] 破坏性变更（会导致现有功能不按预期工作）
- [ ] 文档更新

## 关联 Issue

Closes #(issue 编号)

## 测试清单

- [ ] 我已添加/更新相关测试
- [ ] 所有新旧测试通过
- [ ] 我已在真机/模拟器上测试

## 截图（如适用）

添加相关截图。

## 其他说明

添加任何需要审阅者注意的信息。
```

---

## 附录

### A. 工具配置

#### A.1 EditorConfig

```ini
# .editorconfig

root = true

[*]
charset = utf-8
end_of_line = lf
indent_style = space
indent_size = 4
insert_final_newline = true
trim_trailing_whitespace = true

[*.{kt,kts}]
max_line_length = 120

[*.md]
trim_trailing_whitespace = false

[*.{xml,json}]
indent_size = 2
```

#### A.2 Detekt 配置（推荐）

```yaml
# detekt.yml

complexity:
  LongMethod:
    threshold: 30
  LongParameterList:
    threshold: 6
  ComplexCondition:
    threshold: 4

naming:
  FunctionNaming:
    functionPattern: '[a-z][a-zA-Z0-9]*'
  VariableNaming:
    variablePattern: '[a-z][a-zA-Z0-9]*'

style:
  MaxLineLength:
    maxLineLength: 120
  WildcardImport:
    excludeImports:
      - 'androidx.compose.foundation.layout.*'
      - 'androidx.compose.material3.*'
      - 'androidx.compose.runtime.*'
```

### B. 参考资料

- [Kotlin 编码规范](https://kotlinlang.org/docs/coding-conventions.html)
- [Android Kotlin 样式指南](https://developer.android.com/kotlin/style-guide)
- [Compose API 指南](https://developer.android.com/jetpack/compose/api-guidelines)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [Keep a Changelog](https://keepachangelog.com/)

---

> **文档维护**：本文档应随项目发展持续更新。如有规范变更，请通过 PR 提交并在变更日志中记录。
