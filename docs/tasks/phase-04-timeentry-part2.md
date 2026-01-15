
### 2.4 计时器功能

#### T4.4.1 实现计时器状态管理

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.4.1 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T1.3.4 |

**任务描述**：
实现计时器状态机，管理 Idle/Running/Paused 三种状态。

**关键代码**：
```kotlin
// TimerState.kt
package com.blockwise.feature.timeentry.domain.model

import kotlinx.datetime.Instant

sealed class TimerState {
    object Idle : TimerState()

    data class Running(
        val activityId: Long,
        val activityName: String,
        val startTime: Instant,
        val tagIds: List<Long> = emptyList()
    ) : TimerState()

    data class Paused(
        val activityId: Long,
        val activityName: String,
        val startTime: Instant,
        val elapsedMillis: Long,
        val tagIds: List<Long> = emptyList()
    ) : TimerState()
}

// TimerManager.kt
class TimerManager @Inject constructor(
    private val timerPreferences: TimerPreferences
) {
    private val _state = MutableStateFlow<TimerState>(TimerState.Idle)
    val state: StateFlow<TimerState> = _state.asStateFlow()

    private val _elapsedMillis = MutableStateFlow(0L)
    val elapsedMillis: StateFlow<Long> = _elapsedMillis.asStateFlow()

    private var timerJob: Job? = null

    fun start(activityId: Long, activityName: String, tagIds: List<Long> = emptyList()) {
        val now = Clock.System.now()
        _state.value = TimerState.Running(activityId, activityName, now, tagIds)
        timerPreferences.saveState(_state.value)
        startTicking()
    }

    fun pause() {
        val current = _state.value
        if (current is TimerState.Running) {
            timerJob?.cancel()
            _state.value = TimerState.Paused(
                activityId = current.activityId,
                activityName = current.activityName,
                startTime = current.startTime,
                elapsedMillis = _elapsedMillis.value,
                tagIds = current.tagIds
            )
            timerPreferences.saveState(_state.value)
        }
    }

    fun resume() {
        val current = _state.value
        if (current is TimerState.Paused) {
            _state.value = TimerState.Running(
                activityId = current.activityId,
                activityName = current.activityName,
                startTime = current.startTime,
                tagIds = current.tagIds
            )
            timerPreferences.saveState(_state.value)
            startTicking(current.elapsedMillis)
        }
    }

    fun stop(): TimerResult? {
        val current = _state.value
        timerJob?.cancel()
        _state.value = TimerState.Idle
        _elapsedMillis.value = 0
        timerPreferences.clear()

        return when (current) {
            is TimerState.Running -> TimerResult(
                activityId = current.activityId,
                startTime = current.startTime,
                endTime = Clock.System.now(),
                tagIds = current.tagIds
            )
            is TimerState.Paused -> TimerResult(
                activityId = current.activityId,
                startTime = current.startTime,
                endTime = current.startTime + current.elapsedMillis.milliseconds,
                tagIds = current.tagIds
            )
            else -> null
        }
    }

    private fun startTicking(initialElapsed: Long = 0) {
        timerJob?.cancel()
        timerJob = CoroutineScope(Dispatchers.Default).launch {
            var elapsed = initialElapsed
            while (isActive) {
                _elapsedMillis.value = elapsed
                delay(1000)
                elapsed += 1000
            }
        }
    }
}
```

**验收标准**：
- [ ] 三种状态正确切换
- [ ] 计时精度秒级
- [ ] 状态变化通过 Flow 通知

---

#### T4.4.2 实现计时器Foreground Service

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.4.2 |
| **优先级** | P0 |
| **预计耗时** | 3h |
| **依赖任务** | T4.4.1 |

**任务描述**：
实现前台服务，确保计时器在后台持续运行。

**关键代码**：
```kotlin
// TimerService.kt
@AndroidEntryPoint
class TimerService : Service() {

    @Inject
    lateinit var timerManager: TimerManager

    private val notificationId = 1001
    private val channelId = "timer_channel"

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_START -> {
                val activityId = intent.getLongExtra(EXTRA_ACTIVITY_ID, -1)
                val activityName = intent.getStringExtra(EXTRA_ACTIVITY_NAME) ?: ""
                timerManager.start(activityId, activityName)
                startForeground(notificationId, createNotification())
            }
            ACTION_PAUSE -> timerManager.pause()
            ACTION_RESUME -> timerManager.resume()
            ACTION_STOP -> {
                timerManager.stop()
                stopForeground(STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }

        // Update notification periodically
        observeTimer()

        return START_STICKY
    }

    private fun createNotification(): Notification {
        val state = timerManager.state.value
        val elapsed = timerManager.elapsedMillis.value

        val title = when (state) {
            is TimerState.Running -> "正在计时: ${state.activityName}"
            is TimerState.Paused -> "已暂停: ${state.activityName}"
            else -> "计时器"
        }

        val content = formatDuration(elapsed)

        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(title)
            .setContentText(content)
            .setSmallIcon(R.drawable.ic_timer)
            .setOngoing(true)
            .addAction(createPauseResumeAction(state))
            .addAction(createStopAction())
            .setContentIntent(createContentIntent())
            .build()
    }

    companion object {
        const val ACTION_START = "action_start"
        const val ACTION_PAUSE = "action_pause"
        const val ACTION_RESUME = "action_resume"
        const val ACTION_STOP = "action_stop"
        const val EXTRA_ACTIVITY_ID = "activity_id"
        const val EXTRA_ACTIVITY_NAME = "activity_name"
    }
}
```

**验收标准**：
- [ ] 服务在后台持续运行
- [ ] 应用切换后计时不中断
- [ ] START_STICKY 确保服务重启

---

#### T4.4.3 实现计时器通知

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.4.3 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T4.4.2 |

**任务描述**：
实现通知栏显示计时状态和操作按钮。

**验收标准**：
- [ ] 显示活动名称和计时时长
- [ ] 暂停/继续按钮
- [ ] 停止按钮
- [ ] 点击通知打开应用

---

#### T4.4.4 实现计时器状态持久化

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.4.4 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T4.4.1 |

**任务描述**：
使用 SharedPreferences 持久化计时器状态。

**关键代码**：
```kotlin
// TimerPreferences.kt
class TimerPreferences @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs = context.getSharedPreferences("timer_prefs", Context.MODE_PRIVATE)

    fun saveState(state: TimerState) {
        prefs.edit {
            when (state) {
                is TimerState.Idle -> {
                    putString(KEY_STATE, STATE_IDLE)
                }
                is TimerState.Running -> {
                    putString(KEY_STATE, STATE_RUNNING)
                    putLong(KEY_ACTIVITY_ID, state.activityId)
                    putString(KEY_ACTIVITY_NAME, state.activityName)
                    putLong(KEY_START_TIME, state.startTime.toEpochMilliseconds())
                }
                is TimerState.Paused -> {
                    putString(KEY_STATE, STATE_PAUSED)
                    putLong(KEY_ACTIVITY_ID, state.activityId)
                    putString(KEY_ACTIVITY_NAME, state.activityName)
                    putLong(KEY_START_TIME, state.startTime.toEpochMilliseconds())
                    putLong(KEY_ELAPSED_MILLIS, state.elapsedMillis)
                }
            }
        }
    }

    fun restoreState(): TimerState {
        return when (prefs.getString(KEY_STATE, STATE_IDLE)) {
            STATE_RUNNING -> TimerState.Running(
                activityId = prefs.getLong(KEY_ACTIVITY_ID, -1),
                activityName = prefs.getString(KEY_ACTIVITY_NAME, "") ?: "",
                startTime = Instant.fromEpochMilliseconds(prefs.getLong(KEY_START_TIME, 0))
            )
            STATE_PAUSED -> TimerState.Paused(
                activityId = prefs.getLong(KEY_ACTIVITY_ID, -1),
                activityName = prefs.getString(KEY_ACTIVITY_NAME, "") ?: "",
                startTime = Instant.fromEpochMilliseconds(prefs.getLong(KEY_START_TIME, 0)),
                elapsedMillis = prefs.getLong(KEY_ELAPSED_MILLIS, 0)
            )
            else -> TimerState.Idle
        }
    }

    fun clear() {
        prefs.edit { clear() }
    }
}
```

**验收标准**：
- [ ] 状态正确保存
- [ ] 状态正确恢复
- [ ] 清除功能正常

---

#### T4.4.5 实现计时器崩溃恢复

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.4.5 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T4.4.4 |

**任务描述**：
应用重启后检测并恢复未完成的计时。

**验收标准**：
- [ ] 启动时检测未完成计时
- [ ] 显示恢复对话框
- [ ] 支持继续或放弃

---

#### T4.4.6 实现计时器Use Cases

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.4.6 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T4.4.1 - T4.4.5 |

**验收标准**：
- [ ] StartTimerUseCase
- [ ] PauseTimerUseCase
- [ ] ResumeTimerUseCase
- [ ] StopTimerUseCase（自动创建记录）

---

#### T4.4.7 实现计时器ViewModel

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.4.7 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T4.4.6 |

**验收标准**：
- [ ] 计时器状态暴露
- [ ] 操作方法封装
- [ ] 与 Service 通信

---

#### T4.4.8 实现计时器UI组件

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.4.8 |
| **优先级** | P0 |
| **预计耗时** | 2.5h |
| **依赖任务** | T4.4.7 |

**关键代码**：
```kotlin
@Composable
fun TimerWidget(
    state: TimerState,
    elapsedMillis: Long,
    onStart: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onStop: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Timer display
            Text(
                text = formatDuration(elapsedMillis),
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Activity name
            when (state) {
                is TimerState.Running -> Text(
                    text = state.activityName,
                    style = MaterialTheme.typography.bodyLarge
                )
                is TimerState.Paused -> Text(
                    text = "${state.activityName} (已暂停)",
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                else -> {}
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Control buttons
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                when (state) {
                    is TimerState.Idle -> {
                        BlockwisePrimaryButton(
                            text = "开始计时",
                            onClick = onStart
                        )
                    }
                    is TimerState.Running -> {
                        BlockwiseSecondaryButton(text = "暂停", onClick = onPause)
                        BlockwisePrimaryButton(text = "停止", onClick = onStop)
                    }
                    is TimerState.Paused -> {
                        BlockwisePrimaryButton(text = "继续", onClick = onResume)
                        BlockwiseSecondaryButton(text = "停止", onClick = onStop)
                    }
                }
            }
        }
    }
}
```

**验收标准**：
- [ ] 时长显示格式正确
- [ ] 按钮状态随计时状态变化
- [ ] 动画效果流畅

---

#### T4.4.9 实现快速开始计时功能

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.4.9 |
| **优先级** | P1 |
| **预计耗时** | 1.5h |
| **依赖任务** | T4.4.8 |

**任务描述**：
选择活动类型后一键开始计时。

**验收标准**：
- [ ] 活动类型快速选择
- [ ] 一键开始计时
- [ ] 最近使用的活动类型优先显示

---

### 2.5 时间线视图

#### T4.5.1 实现查询时间记录Use Case

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.5.1 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T3.4.8 |

**验收标准**：
- [ ] 按日期范围查询
- [ ] 按日期分组
- [ ] 支持分页

---

#### T4.5.2 实现时间线ViewModel

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.5.2 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T4.5.1 |

**关键代码**：
```kotlin
@HiltViewModel
class TimelineViewModel @Inject constructor(
    private val getTimeEntries: GetTimeEntriesUseCase,
    private val deleteTimeEntry: DeleteTimeEntryUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(TimelineUiState())
    val uiState: StateFlow<TimelineUiState> = _uiState.asStateFlow()

    init {
        loadEntries()
    }

    private fun loadEntries() {
        viewModelScope.launch {
            getTimeEntries.getRecent(limit = 50, offset = 0)
                .collect { entries ->
                    val grouped = entries.groupBy { entry ->
                        entry.startTime.toLocalDateTime(TimeZone.currentSystemDefault()).date
                    }
                    _uiState.update { it.copy(
                        entriesByDate = grouped,
                        isLoading = false
                    )}
                }
        }
    }

    fun loadMore() {
        // Pagination logic
    }

    fun delete(entryId: Long) {
        viewModelScope.launch {
            deleteTimeEntry(entryId)
        }
    }
}

data class TimelineUiState(
    val entriesByDate: Map<LocalDate, List<TimeEntry>> = emptyMap(),
    val isLoading: Boolean = true,
    val hasMore: Boolean = true
)
```

**验收标准**：
- [ ] 按日期分组数据
- [ ] 分页加载支持
- [ ] 删除操作处理

---

#### T4.5.3 实现时间线列表界面

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.5.3 |
| **优先级** | P0 |
| **预计耗时** | 2.5h |
| **依赖任务** | T4.5.2 |

**验收标准**：
- [ ] LazyColumn 列表显示
- [ ] 按日期分组显示
- [ ] 下拉刷新支持
- [ ] 加载更多支持

---

#### T4.5.4 实现时间线记录项组件

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.5.4 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T4.5.3 |

**验收标准**：
- [ ] 显示活动类型颜色
- [ ] 显示时间范围
- [ ] 显示时长
- [ ] 显示标签

---

#### T4.5.5 实现日期分组头

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.5.5 |
| **优先级** | P1 |
| **预计耗时** | 1h |
| **依赖任务** | T4.5.3 |

**验收标准**：
- [ ] 显示日期
- [ ] 显示当日总时长
- [ ] 粘性头部效果

---

#### T4.5.6 实现记录编辑入口

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.5.6 |
| **优先级** | P0 |
| **预计耗时** | 0.5h |
| **依赖任务** | T4.5.3, T4.3.5 |

**验收标准**：
- [ ] 点击记录跳转编辑
- [ ] 长按显示操作菜单

---

#### T4.5.7 实现记录拆分功能

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.5.7 |
| **优先级** | P2 |
| **预计耗时** | 2h |
| **依赖任务** | T4.3.1 |

**验收标准**：
- [ ] 选择拆分时间点
- [ ] 生成两条新记录
- [ ] 删除原记录

---

#### T4.5.8 实现记录合并功能

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.5.8 |
| **优先级** | P2 |
| **预计耗时** | 2h |
| **依赖任务** | T4.3.1 |

**验收标准**：
- [ ] 选择多条相邻记录
- [ ] 合并为一条记录
- [ ] 保留标签并集
