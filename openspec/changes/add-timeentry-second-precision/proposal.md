# Change: 时间条目记录秒级精度，时间线保持分钟级展示（v1.2.2）

## Why
当前时间条目的记录与校验在部分路径上隐含“分钟级”假设，导致统计/计时与后续扩展（更细粒度计时/导出）受到限制。
在不增加时间线视觉复杂度的前提下，引入秒级记录精度可以提升数据准确性，并为后续能力预留空间。

## What Changes
- 数据层：TimeEntry 的起始/结束时间支持秒级精度（`HH:mm:ss` 语义）。
- 校验：时间条目有效性判断使用秒级精度（结束时间必须严格晚于起始时间）。
- 计时：计时开始与完成时刻按系统当前时间写入（包含秒）。
- UI 展示：Timeline 列表中的起始/结束时间仍仅展示到分钟（`HH:mm`），并对秒进行截断隐藏（不四舍五入）。

## Impact
- Affected specs:
  - `openspec/specs/timeline-visual/spec.md`
  - (new) `openspec/specs/timeentry-time-precision/spec.md`
- Likely affected code:
  - `core/domain/.../TimeEntry.kt`（durationMinutes/校验）
  - `feature/timeentry/.../domain/usecase/timeentry/CreateTimeEntryUseCase.kt`（校验规则）
  - `feature/timeentry/.../domain/usecase/timer/TimerUseCases.kt`（计时最小时长策略与保存路径）
  - `feature/timeentry/.../presentation/timeline/TimeEntryItem.kt`（时间展示格式）
  - `feature/timeentry/.../presentation/timeline/UntrackedGapItem.kt`（时间展示格式/24:00 特例）

## Non-Goals
- 不改变 Timeline 时间选择控件的交互粒度（仍为“时/分”滚轮，分钟步进）。
- 不在 v1.2.2 引入秒级编辑 UI（例如秒滚轮/秒输入）。
- 不将统计视图改为秒级展示（如需，将另起变更）。

## Resolved Product Decisions
- 计时保存门槛调整为“> 0 秒”（不再依赖“最少 1 分钟”）。
- 分钟级编辑保存时间条目时，保留原秒值（仅编辑 hour/minute，不强制对齐到 `:00`）。
