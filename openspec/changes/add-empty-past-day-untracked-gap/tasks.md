## 1. Implementation
- [ ] 1.1 盘点当前“无记录”空状态的触发条件与渲染位置
  - Evidence: 标注关键路径：`TimelineUseCases.kt` 产物结构、`TimelineScreen.kt` 何时进入空状态分支
- [ ] 1.2 在 domain 层补齐“历史空日”的全日未追踪段产出
  - Notes: 仅当选中日期 D 严格早于 today（系统时区自然日）且 D 内 overlap 条目数为 0 时，产出 1 个 gap：`[D 00:00, D+1 00:00)`
  - Evidence: `TimelineUntrackedGapTest.kt` 新增用例覆盖：
    - D < today 且无条目 -> 仅 1 个 gap（00:00–24:00）
    - D == today 且无条目 -> 不强制插入全日 gap（保持既有行为）
    - D < today 且有条目 -> 不额外插入全日 gap（仍按首尾/相邻规则）
- [ ] 1.3 UI 行为验证：历史空日不显示空状态、改为展示全日未追踪卡片
  - Evidence: 手动验收：切换到“今天之前”任意无记录日期，列表中显示 00:00–24:00 的未追踪卡片；点击进入创建流程并预填正确起止
  - Optional: 如已有 Compose UI 测试基建，可补一条 UI test 验证“无空状态 + 有 gap 卡片”
- [ ] 1.4 质量与验证
  - Command: `./gradlew test`
  - Command: `./gradlew detekt`
  - Command: `./gradlew ktlintCheck`
  - Command: `./gradlew assembleDebug`

## 2. Definition of Done
- [ ] 当 D < today 且无任何 overlap TimeEntry 时：显示 1 条 00:00–24:00 未追踪时间段卡片，且不显示空状态
- [ ] 当 D < today 且存在至少 1 条 overlap TimeEntry 时：表现与既有规则一致（不额外插入全日提示）
- [ ] 当 D == today 且无记录时：不因本变更强制替换空状态
- [ ] 单元测试覆盖新增边界；`./gradlew test` / `detekt` / `ktlintCheck` / `assembleDebug` 通过（如有既有失败需标注）
