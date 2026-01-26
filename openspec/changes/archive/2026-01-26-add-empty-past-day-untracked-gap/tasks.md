## 1. Implementation
- [x] 1.1 盘点当前“无记录”空状态的触发条件与渲染位置
  - Evidence: `TimelineViewModel.kt` 通过 `dayGroups.isEmpty()` / `selectedDayGroup == null` 触发空态分支；`TimelineUseCases.kt` 负责产出 DayGroup.items
- [x] 1.2 在 domain 层补齐“历史空日”的全日未追踪段产出
  - Notes: 仅当选中日期 D 严格早于 today（系统时区自然日）且 D 内 overlap 条目数为 0 时，产出 1 个 gap：`[D 00:00, D+1 00:00)`
  - Evidence: `TimelineUntrackedGapTest.kt` 新增用例覆盖：
    - D < today 且无条目 -> 仅 1 个 gap（00:00–24:00）
    - D == today 且无条目 -> 不强制插入全日 gap（保持既有行为）
    - D < today 且有条目 -> 不额外插入全日 gap（仍按首尾/相邻规则）
- [x] 1.3 UI 行为验证：历史空日不显示空状态、改为展示全日未追踪卡片
  - Evidence: `TimelineEmptyPastDayUntrackedGapTest.kt` 覆盖“无空状态 + 有 gap 卡片 + 点击回调传递起止”
- [x] 1.4 质量与验证
  - Command: `./gradlew test`
  - Command: `./gradlew detekt`
  - Command: `./gradlew ktlintCheck`
  - Command: `./gradlew assembleDebug`
  - Note: 当前工程未定义 `detekt` / `ktlintCheck` Gradle task（执行会报 Task not found）；已跑通 `test` / `assembleDebug`，并编译通过 `:feature:timeentry:assembleDebugAndroidTest`

## 2. Definition of Done
- [x] 当 D < today 且无任何 overlap TimeEntry 时：显示 1 条 00:00–24:00 未追踪时间段卡片，且不显示空状态
- [x] 当 D < today 且存在至少 1 条 overlap TimeEntry 时：表现与既有规则一致（不额外插入全日提示）
- [x] 当 D == today 且无记录时：不因本变更强制替换空状态
- [x] 单元测试覆盖新增边界；`./gradlew test` / `detekt` / `ktlintCheck` / `assembleDebug` 通过（如有既有失败需标注）
  - Note: `detekt` / `ktlintCheck` Gradle task 在当前工程不存在；其余命令已通过
