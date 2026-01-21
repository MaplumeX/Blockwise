## Context
本变更希望在不增加 Timeline 视觉复杂度的前提下，将 TimeEntry 的起始/结束时间精度提升到秒级。
项目当前时间存储使用 `kotlinx.datetime.Instant` 并通过 Room Converter 以 epoch milliseconds 持久化，因此数据库层面已具备足够精度。

## Goals / Non-Goals
- Goals:
  - 数据记录与校验使用秒级精度（end 必须严格晚于 start）。
  - 计时创建条目时写入系统当前时间（包含秒）。
  - Timeline 列表展示保持 `HH:mm`，对秒截断隐藏。
- Non-Goals:
  - 秒级编辑 UI（秒滚轮/秒输入）。
  - 统计/导出格式的秒级展示（如需将单独提案）。

## Decisions
- Decision: 数据层时间戳继续使用 `Instant`（epoch ms），不引入新的时间类型或数据库列。
  - Rationale: 现有存储已支持毫秒；变更仅需调整业务规则与展示层格式。

- Decision: Timeline 列表展示通过读取 hour/minute 来格式化，天然实现“截断秒”。
  - Rationale: 避免四舍五入导致视觉跳动；与现有 `String.format("%02d:%02d")` 实现一致。

- Decision: 分钟级编辑保存时保留秒。
  - Rationale: v1.2.2 仍使用分钟级编辑控件（时/分滚轮），因此保存时仅变更 hour/minute；seconds 保持不变，避免无意的数据丢失。

## Risks / Trade-offs
- 风险：当前 `durationMinutes` 以毫秒差 / 60000 向下取整，可能导致 < 60 秒的条目 durationMinutes==0，从而触发旧的“时长必须>0”校验失败。
  - Mitigation: 调整校验逻辑，不再依赖 durationMinutes>0 来保证条目有效性；以 end > start（秒级）为准。

- 风险：Timeline 未追踪 Gap 检测以“至少 1 分钟”作为阈值，且 gap 计算可能依赖分钟差。
  - Mitigation: 本变更不改变 Gap 的可视化阈值，但需要确保其在秒存在时仍按分钟级规则稳定工作。

## Migration Plan
- 数据库无需迁移（存储已为 epoch ms）。
- 需要评估并清理各处“最少 1 分钟”假设：
  - Create/Update time entry use cases 的 durationMinutes 校验。
  - Timer stop 保存门槛。

## Resolved Product Decisions
- 计时保存门槛：调整为“> 0 秒”。
- 分钟级编辑保存：保留原秒值。
