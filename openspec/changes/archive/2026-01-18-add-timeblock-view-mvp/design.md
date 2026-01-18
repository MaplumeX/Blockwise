## Context
v1.1 M5 要求把时间块视图作为时间线的另一种呈现方式集成进同一入口，并保证：
- 顶部日期导航/筛选保持一致
- 两视图共享选中日期与数据源
- 视图偏好可持久化
- 时间块渲染采用“段胶囊”而非逐格

仓库现状：
- 已存在 `feature/timeentry/.../timeblock/`（日/周视图 + TimeBlock 组件），但目前作为独立 `TimeEntryNavigation.TIME_BLOCK_ROUTE` 挂载，且交互/网格粒度与 v1.1 任务文档描述存在差异（例如日视图目前按“小时行 click”创建）。

## Goals / Non-Goals
- Goals:
  - 在 Timeline 入口同屏切换 Timeline List 与 Time Block Day View
  - 统一选中日期状态，并在两视图间保持一致
  - 视图偏好持久化（DataStore）
  - 时间块渲染策略明确：按连续范围渲染，不逐格
- Non-Goals:
  - M7 的容错/缓存/骨架屏/可访问性
  - v1.1 不承诺的横向滚动与可配置粒度

## Decisions
- 容器层：新增一个“TimelineContainer/TimelineHost”承载两个 content（Timeline list 与 timeblock day），并复用 Timeline 顶部日期导航能力。
- 状态来源：选中日期由容器统一管理（或由 TimelineViewModel 托管并对 timeblock 子组件下发）；TimeEntry 数据继续由 repository/usecase Flow 提供。
- 偏好存储：在 `SettingsDataStore` 存储 `timeline_view_mode`（string/int）并映射到 enum。

## Risks / Trade-offs
- 复用现有 `TimeBlockScreen` vs 直接嵌入 `TimeBlockDayView`：
  - 复用 Screen 的优点是逻辑齐全；缺点是它自带 TopBar/周视图等可能与 Timeline 顶部重复。
  - 直接复用 `TimeBlockDayView` 更贴近“内容区切换”的目标，但需要补齐它缺失的网格粒度/交互（M6）。

## Migration Plan
- 第一步先做“入口集成 + 视图偏好 + MVP 渲染策略约束”，优先使用可复用的 composable。
- 统一迁移：收敛为 Timeline 入口的 timeblock 模式，不再保留独立 `time_block` route 作为主路径。

## Open Questions
- 无。
