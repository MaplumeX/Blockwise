
### 2.6 时间块视图

#### T4.6.1 实现时间块日视图组件

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.6.1 |
| **优先级** | P0 |
| **预计耗时** | 3h |
| **依赖任务** | T4.5.1 |

**任务描述**：
实现24小时时间轴的日视图，以色块形式展示时间记录。

**关键代码**：
```kotlin
@Composable
fun TimeBlockDayView(
    date: LocalDate,
    entries: List<TimeEntry>,
    onEntryClick: (TimeEntry) -> Unit,
    onEmptySlotClick: (LocalTime) -> Unit,
    modifier: Modifier = Modifier
) {
    val hourHeight = 60.dp // Each hour = 60dp

    Box(modifier = modifier) {
        // Time axis
        Column {
            (0..23).forEach { hour ->
                TimeAxisHour(
                    hour = hour,
                    modifier = Modifier.height(hourHeight)
                )
            }
        }

        // Time blocks
        entries.forEach { entry ->
            val startMinutes = entry.startTime.toLocalTime().toSecondOfDay() / 60
            val endMinutes = entry.endTime.toLocalTime().toSecondOfDay() / 60
            val topOffset = (startMinutes * hourHeight.value / 60).dp
            val height = ((endMinutes - startMinutes) * hourHeight.value / 60).dp

            TimeBlock(
                entry = entry,
                onClick = { onEntryClick(entry) },
                modifier = Modifier
                    .offset(x = 50.dp, y = topOffset)
                    .height(height.coerceAtLeast(20.dp))
                    .fillMaxWidth()
                    .padding(end = 8.dp)
            )
        }
    }
}

@Composable
fun TimeBlock(
    entry: TimeEntry,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = entry.activity.color.copy(alpha = 0.8f)
        ),
        shape = RoundedCornerShape(4.dp)
    ) {
        Column(
            modifier = Modifier.padding(4.dp)
        ) {
            Text(
                text = entry.activity.name,
                style = MaterialTheme.typography.labelSmall,
                color = Color.White,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}
```

**验收标准**：
- [ ] 24小时时间轴显示
- [ ] 色块按时间位置显示
- [ ] 色块高度对应时长
- [ ] 活动类型颜色正确

---

#### T4.6.2 实现时间块渲染逻辑

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.6.2 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T4.6.1 |

**任务描述**：
实现 Canvas 绘制和坐标计算逻辑。

**验收标准**：
- [ ] 坐标计算正确
- [ ] 跨日记录处理
- [ ] 性能优化（remember缓存）

---

#### T4.6.3 实现时间块周视图组件

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.6.3 |
| **优先级** | P0 |
| **预计耗时** | 3h |
| **依赖任务** | T4.6.1 |

**关键代码**：
```kotlin
@Composable
fun TimeBlockWeekView(
    weekStart: LocalDate,
    entriesByDay: Map<LocalDate, List<TimeEntry>>,
    onEntryClick: (TimeEntry) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(modifier = modifier.horizontalScroll(rememberScrollState())) {
        (0..6).forEach { dayOffset ->
            val date = weekStart.plus(dayOffset, DateTimeUnit.DAY)
            val entries = entriesByDay[date] ?: emptyList()

            Column(
                modifier = Modifier.width(100.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Day header
                Text(
                    text = date.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    style = MaterialTheme.typography.labelMedium
                )
                Text(
                    text = "${date.dayOfMonth}",
                    style = MaterialTheme.typography.titleMedium
                )

                // Day column
                TimeBlockDayColumn(
                    entries = entries,
                    onEntryClick = onEntryClick,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
```

**验收标准**：
- [ ] 7天并列显示
- [ ] 日期头部显示
- [ ] 横向滚动支持
- [ ] 工作日/周末区分

---

#### T4.6.4 实现时间块重叠处理

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.6.4 |
| **优先级** | P1 |
| **预计耗时** | 2h |
| **依赖任务** | T4.6.2 |

**任务描述**：
处理同一时间段多条记录的并列显示。

**验收标准**：
- [ ] 检测重叠记录
- [ ] 并列显示（最多3条）
- [ ] 超出显示"更多"

---

#### T4.6.5 实现时间块点击交互

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.6.5 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T4.6.1 |

**验收标准**：
- [ ] 点击打开详情/编辑
- [ ] 点击反馈效果

---

#### T4.6.6 实现时间块长按删除

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.6.6 |
| **优先级** | P1 |
| **预计耗时** | 1h |
| **依赖任务** | T4.6.5 |

**验收标准**：
- [ ] 长按显示删除确认
- [ ] 删除成功刷新视图

---

#### T4.6.7 实现空白区域快速创建

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.6.7 |
| **优先级** | P1 |
| **预计耗时** | 1.5h |
| **依赖任务** | T4.6.1 |

**验收标准**：
- [ ] 点击空白区域
- [ ] 预填充点击时间
- [ ] 跳转创建页面

---

#### T4.6.8 实现时间块拖拽调整

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.6.8 |
| **优先级** | P2 |
| **预计耗时** | 3h |
| **依赖任务** | T4.6.1 |

**验收标准**：
- [ ] 拖拽调整开始时间
- [ ] 拖拽调整时长
- [ ] 实时预览效果

---

#### T4.6.9 实现时间块缩放手势

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.6.9 |
| **优先级** | P2 |
| **预计耗时** | 2h |
| **依赖任务** | T4.6.1 |

**验收标准**：
- [ ] 双指缩放
- [ ] 调整视图密度
- [ ] 平滑动画

---

#### T4.6.10 实现时间块视图ViewModel

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.6.10 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T4.5.1 |

**验收标准**：
- [ ] 日期状态管理
- [ ] 数据加载
- [ ] 视图模式切换

---

#### T4.6.11 实现时间块日期导航

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.6.11 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T4.6.10 |

**验收标准**：
- [ ] 左右滑动切换日期
- [ ] 日期选择器
- [ ] "今天"快速跳转

---

#### T4.6.12 实现时间块视图统计摘要

| 属性 | 值 |
|------|-----|
| **任务ID** | T4.6.12 |
| **优先级** | P1 |
| **预计耗时** | 1h |
| **依赖任务** | T4.6.10 |

**验收标准**：
- [ ] 显示当日/周总时长
- [ ] 显示记录数量

---

## 3. 依赖关系图

```
阶段三完成 (Repository)
        │
        ├── T4.1.1 活动类型UseCases ── T4.1.2 ViewModel ── T4.1.3 列表界面
        │                                      │              │
        │                                      └── T4.1.4 编辑界面 ── T4.1.5 颜色选择器
        │                                      │
        │                                      └── T4.1.6 预置数据
        │
        ├── T4.2.1 标签UseCases ── T4.2.2 ViewModel ── T4.2.3 管理界面
        │                                              │
        │                                              └── T4.2.4 编辑对话框
        │
        ├── T4.3.1-3 记录UseCases ── T4.3.4 ViewModel ── T4.3.5 编辑界面
        │         │                                       │
        │         └── T4.3.6 重叠校验                      │
        │                                                 │
        ├── T4.4.1 计时器状态 ── T4.4.2 Service ── T4.4.3 通知
        │         │                    │
        │         └── T4.4.4 持久化 ── T4.4.5 崩溃恢复
        │                    │
        │                    └── T4.4.6 UseCases ── T4.4.7 ViewModel ── T4.4.8 UI
        │                                                               │
        │                                                               └── T4.4.9 快速开始
        │
        ├── T4.5.1 查询UseCase ── T4.5.2 ViewModel ── T4.5.3 列表界面
        │                                              │
        │                                              ├── T4.5.4 记录项
        │                                              ├── T4.5.5 日期分组头
        │                                              └── T4.5.6 编辑入口
        │
        └── T4.6.1 日视图 ── T4.6.2 渲染逻辑 ── T4.6.4 重叠处理
                  │                              │
                  ├── T4.6.3 周视图              ├── T4.6.8 拖拽
                  │                              └── T4.6.9 缩放
                  │
                  ├── T4.6.5 点击 ── T4.6.6 长按删除
                  │              └── T4.6.7 空白创建
                  │
                  └── T4.6.10 ViewModel ── T4.6.11 日期导航
                                        └── T4.6.12 统计摘要
```

---

## 4. 验收标准清单

### 4.1 活动类型管理验收

| 验收项 | 要求 |
|--------|------|
| 列表显示 | 正确显示所有活动类型 |
| 创建功能 | 可创建新活动类型 |
| 编辑功能 | 可编辑现有活动类型 |
| 删除功能 | 可删除（软删除）活动类型 |
| 颜色选择 | 颜色选择器正常工作 |
| 预置数据 | 首次启动有默认数据 |

### 4.2 标签管理验收

| 验收项 | 要求 |
|--------|------|
| 列表显示 | 正确显示所有标签 |
| 创建功能 | 可创建新标签，名称唯一 |
| 编辑功能 | 可编辑现有标签 |
| 删除功能 | 可删除标签 |

### 4.3 手动记录验收

| 验收项 | 要求 |
|--------|------|
| 创建记录 | 可创建新时间记录 |
| 编辑记录 | 可编辑现有记录 |
| 删除记录 | 可删除记录 |
| 时间验证 | 结束时间必须晚于开始时间 |
| 标签关联 | 可关联多个标签 |

### 4.4 计时器验收

| 验收项 | 要求 |
|--------|------|
| 开始计时 | 选择活动后开始计时 |
| 暂停/继续 | 暂停和继续功能正常 |
| 停止计时 | 停止后自动创建记录 |
| 后台运行 | 切换应用后继续计时 |
| 通知显示 | 通知栏显示计时状态 |
| 崩溃恢复 | 应用重启后可恢复 |

### 4.5 时间线视图验收

| 验收项 | 要求 |
|--------|------|
| 列表显示 | 按时间倒序显示记录 |
| 日期分组 | 按日期分组显示 |
| 分页加载 | 滚动加载更多 |
| 编辑入口 | 点击可进入编辑 |

### 4.6 时间块视图验收

| 验收项 | 要求 |
|--------|------|
| 日视图 | 24小时时间轴正确显示 |
| 周视图 | 7天并列正确显示 |
| 色块显示 | 位置和高度正确 |
| 日期导航 | 切换日期正常 |
| 点击交互 | 点击色块可编辑 |

---

## 5. 风险与注意事项

### 5.1 潜在风险

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 计时器后台被杀 | 计时中断 | Foreground Service + 持久化 |
| 时间块渲染性能 | 卡顿 | Canvas优化 + 虚拟化 |
| 时区问题 | 时间显示错误 | 统一使用系统时区 |

### 5.2 注意事项

1. **Foreground Service 权限**：Android 9+ 需要 FOREGROUND_SERVICE 权限
2. **通知渠道**：Android 8+ 必须创建通知渠道
3. **电池优化**：引导用户关闭电池优化
4. **时间块性能**：大量记录时使用 LazyColumn

---

*文档版本: v1.0*
*阶段状态: 已完成*
