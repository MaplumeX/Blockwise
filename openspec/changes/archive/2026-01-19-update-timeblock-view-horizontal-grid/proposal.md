# Change: Update Time Block View Integration (v1.2)

## Why
v1.2 需要对时间块视图（Time Block View）的网格布局方向做明确约束（小时为行、分钟为列的“横向网格”），以保证阅读一致性与后续编辑交互的可预期性。

仓库当前已具备 Timeline 入口的“列表 ↔ 时间块”视图切换、视图偏好持久化、共享数据源/选中日期，以及时间块日视图 24×12（5 分钟）网格与段胶囊渲染（v1.1 交付）。本变更聚焦在不改变入口信息架构的前提下，将时间块视图的布局约束提升为 v1.2 的明确要求，并补齐/澄清与该约束相关的交互边界。

## What Changes
- 强化时间块日视图布局方向约束：仅支持“小时为行、分钟为列”的横向网格（24 行 × 12 列；5 分钟/格）。
- 明确时间块段胶囊的跨小时切片规则：按小时行切片显示，但保持同色、对齐与连续感；起始行/结束行圆角策略明确。
- 明确滚动方向与缩放边界：仅支持垂直滚动浏览 24 小时；不支持横向滚动；5 分钟粒度在 v1.2 固定不可配置。
- 约束/对齐 Timeline 入口集成行为：切换入口、偏好恢复、共享数据源与选中日期语义保持不变（复用既有实现）。

## Impact
- Affected specs: `openspec/specs/timeblock-view/spec.md`
- Affected docs (reference):
  - `docs/PRD/PRD-v1.2.md` (Section 1)
  - `docs/PRD/PRD-v1.1.md` (Section 3)
- Affected code (expected):
  - `feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/presentation/timeblock/TimeBlockDayView.kt`
  - `feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/presentation/timeline/TimelineScreen.kt` (集成点应保持不变)

## Assumptions
- v1.2 仅覆盖“时间块视图集成（更新）”中关于横向网格方向/滚动边界/段渲染规则的约束；不引入新的视图入口或新的数据模型。
- 不新增横向滚动、缩放手势或可配置粒度能力。

## Open Questions
- 无。
