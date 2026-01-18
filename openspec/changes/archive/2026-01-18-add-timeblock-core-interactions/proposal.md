# Change: Add Time Block Core Interactions (v1.1 M6)

## Why
时间块视图的价值不止于“看见分布”，还需要覆盖高频的“选中查看详情”与“按范围快速创建”。
v1.1 M6 要求用户能在网格上：
- 点击一段记录直接选中整条记录并查看详情
- 长按空白区域拖拽选择范围，并自动按 5 分钟网格吸附后进入创建

仓库现状：TimeBlock 日/周视图已支持点击记录进入编辑、点击小时行创建，但缺少“跨小时选中一致高亮 + 底部详情卡片”以及“空白区域长按拖拽范围创建（5 分钟吸附）”。

## What Changes
- 记录段点击：选中整条 TimeEntry（跨小时切片一致高亮），并在底部展示详情卡片（活动、时间范围、时长、备注、标签、快捷操作）。
- 空白区域长按拖拽：形成范围选择，吸附到最近 5 分钟格；松手后弹出创建并预填起止时间。

## Impact
- Affected specs (new): `openspec/changes/add-timeblock-core-interactions/specs/timeblock-interactions/spec.md`
- Affected docs (reference):
  - `docs/tasks/v1.1-tasks.md` (M6)
  - `docs/PRD/PRD-v1.1.md` (Section 3.5)
- Affected code (expected):
  - `feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/presentation/timeblock/TimeBlockDayView.kt`
  - `feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/presentation/timeblock/TimeBlock.kt`
  - `feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/presentation/timeblock/TimeBlockViewModel.kt`
  - （若复用 Timeline 容器集成）`feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/presentation/timeline/TimelineScreen.kt`

## Assumptions
- 范围选择的最小单位为 5 分钟；范围边界统一按“就近吸附”规则对齐。
- 范围创建仅适用于空白区域；若起点/终点命中现有记录段，优先判定为记录段交互（选中/详情）。
- M6 不覆盖 M7 的重叠容错表现与冲突列表。

## Decisions
- 详情卡片优先使用 BottomSheet/底部卡片样式，保持内容在当前视图上下文内可见。
- “选中态”由 ViewModel 维护（selectedEntryId/selectedEntry），并在渲染层传入，避免 UI 内部隐式状态导致切换/重组丢失。

## Open Questions
- 详情卡片中的“快捷操作”范围：仅提供编辑/删除（最小）还是包含复制/分享/拆分/合并？（建议：先最小化为编辑/删除，其他跟随 M4 习惯或后续扩展）
