## 1. Implementation
- [x] 1.1 盘点时间线现有交互与动作落点
  - Evidence: `TimelineViewModel.kt` 已有删除/拆分/合并确认状态（entryToDelete/entryToSplit/showMergeConfirmation）与 selection mode（selectedEntryIds/isSelectionMode）；点击/长按行为已确认并按提案调整
- [x] 1.2 定义上下文菜单的状态模型与事件
  - Evidence: 使用 `TimelineUiState.contextMenu: TimelineContextMenuState?`（entryId + tapOffset），并提供 open/dismiss 与动作回调（edit/delete/split）
- [x] 1.3 实现时间线条目点击打开上下文菜单（浮动、就地）
  - Evidence: 非 selection mode 下点击卡片弹出浮动菜单（由 VM state 控制可见）；菜单位置使用 tapOffset 作为锚点，并在 `PopupPositionProvider` 中手动 clamp 到屏幕范围内（确保完全可见）
- [x] 1.4 菜单项：编辑
  - Evidence: 选择“编辑”触发 `NavigateToEdit`（通过 `TimelineViewModel.onContextMenuEdit`）
- [x] 1.5 菜单项：删除/拆分/合并
  - Evidence: 删除/拆分从菜单触发并复用 Timeline 现有确认弹窗与 usecase；合并保持原有 selection mode TopBar/FAB 入口（不放在菜单中）
- [x] 1.6 多选模式交互校正
  - Evidence: 长按进入 selection mode；selection mode 下点击条目仅 toggle（不弹菜单）；退出通过 TopBar 关闭按钮清空 selection
- [x] 1.7 测试与验证
  - Command: `./gradlew test` ✅
  - Command: `./gradlew detekt` / `./gradlew ktlintCheck` ⚠️ 项目中不存在这些 task（`./gradlew tasks --all` 已确认）
  - Command: `./gradlew assembleDebug` ✅

## 2. Definition of Done
- [x] 时间线支持点击弹出浮动上下文菜单（编辑、删除、拆分）
- [x] 时间线支持长按进入多选模式并可多选切换
- [x] 菜单操作与现有数据层一致（状态回写及时）
- [x] 误触成本低：进入/退出多选与菜单关闭路径清晰
- [x] `./gradlew test` / `./gradlew assembleDebug` 通过；`detekt`/`ktlintCheck` 任务不存在（已标注）
