# Change: Add Timeline Context Menu + Multi-Select (v1.1 M4)

## Why
时间线目前缺少“就地操作”的快速入口，用户进行编辑/删除/拆分/合并等动作需要进入编辑页或依赖分散入口，步骤多且打断浏览。
同时，批量操作（如合并）需要更清晰的一致交互：长按进入多选，后续点击扩展选择。

## What Changes
- 在时间线记录卡片上支持“点击弹出浮动上下文菜单”，菜单显示在点击位置附近。
- 菜单提供操作项：编辑、删除、拆分、合并。
- 支持“长按进入多选模式”；进入后可继续点击其他卡片切换选中。
- 现有数据层动作（删除/拆分/合并）复用当前实现，保证行为一致。

## Impact
- Affected specs (new): `openspec/changes/add-timeline-context-menu-multiselect/specs/timeline-interactions/spec.md`
- Affected docs (reference):
  - `docs/tasks/v1.1-tasks.md` (M4)
  - `docs/PRD/PRD-v1.1.md` (Section 1.5)
- Affected code (expected):
  - `feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/presentation/timeline/TimelineScreen.kt`
  - `feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/presentation/timeline/TimelineViewModel.kt`
  - `feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/presentation/timeline/TimeEntryItem.kt`

## Assumptions
- “多选模式”以现有 `TimelineUiState.isSelectionMode` + `selectedEntryIds` 为事实源，并保持现有进入/退出语义。
- 菜单为“浮动样式”，位置锚定在用户点击条目的附近；超出屏幕边界时需要自动避让。
- 菜单动作不会引入新数据模型/数据库变更。

## Decisions
- 普通模式下点击记录卡片仅用于打开上下文菜单；编辑仅从菜单进入，不保留“单击直接编辑”。
- 合并仅在已选记录数 >= 2 时可用（例如菜单项禁用/启用、按钮显示/隐藏）。
- 本变更不包含复制/分享功能。

## Open Questions
- 无。
