# Change: Add Time Block View MVP (v1.1 M5)

## Why
v1.1 的目标之一是把“时间分配”从列表阅读升级为更直观的 24 小时网格视图，让用户能一眼看出一天的占用与空白，并且在时间线与时间块两种视图间无缝切换。
当前仓库里已存在 `TimeBlockScreen`（日/周视图原型），但它作为独立 route 存在，且与 Timeline 的“同屏切换 + 选中日期一致 + 数据实时同步 + 视图偏好持久化”的 v1.1 交付物仍有差距。

## What Changes
- 在 Timeline 入口提供“时间线列表 ↔ 时间块视图”的切换（同一信息架构下切换视图）。
- 视图偏好持久化：再次进入时恢复用户上次选择的视图模式。
- 两个视图共享同一份数据与“选中日期”语义：在任一视图的创建/编辑/删除应实时反映到另一视图。
- 时间块视图 MVP 渲染：左侧 24 小时轴 + 右侧 5 分钟网格（24×12），通过“段胶囊（segment capsule）”渲染 TimeEntry 覆盖范围，避免逐格渲染。

## Impact
- Affected specs (new): `openspec/changes/add-timeblock-view-mvp/specs/timeblock-view/spec.md`
- Affected docs (reference):
  - `docs/tasks/v1.1-tasks.md` (M5)
  - `docs/PRD/PRD-v1.1.md` (Section 3.2, 3.3, 3.4)
- Affected code (expected):
  - `feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/presentation/timeline/TimelineScreen.kt`
  - `feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/presentation/timeblock/TimeBlockDayView.kt`
  - `feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/presentation/timeblock/TimeBlock.kt`
  - `feature/settings/src/main/kotlin/com/maplume/blockwise/feature/settings/data/datastore/SettingsDataStore.kt`
  - `app/src/main/kotlin/com/maplume/blockwise/MainActivity.kt`（若需要调整导航/入口容器）

## Assumptions
- 网格粒度在 v1.1 固定为 5 分钟（不做用户配置）。
- 时间块视图 MVP 先覆盖“日视图”的 24×12 网格与段胶囊渲染；周视图/缩放等属于现存能力或后续增强，不作为本变更验收核心。
- 不支持横向滚动（列数固定），主要通过纵向滚动浏览 24 小时。

## Decisions
- “同屏切换”以单一容器 Screen 维护：顶部区域（日期导航）保持一致，仅内容区域在 Timeline List 与 Time Block Day View 之间切换。
- “视图偏好”存储在 Settings DataStore 中（与现有主题/引导等偏好一致）。
- TimeEntry 数据以 repository 的 Flow 为事实源，切换视图不改变数据查询口径，只改变呈现方式。

## Open Questions
- 无。
