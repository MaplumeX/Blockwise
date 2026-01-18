# Change: Add Timeline Date Navigation (v1.1 M3)

## Why
v1.1 的时间线体验目标之一是让用户更高效地在不同日期间切换与跳转；当前时间线缺少“周内日期条 + 日历快速跳转”的统一入口，导致日期切换成本高、路径分散。

## What Changes
- 在时间线页顶部增加“周范围标题（Week Range Title）”。
- 增加“本周 7 天横向日期条（Week Strip）”，用于快速切换当天数据。
- 支持左右滑动切换周，并保持“选中星期”语义（例如从周五切到上一周仍默认选中上周五）。
- 点击周范围标题打开日历选择器（复用 `core/designsystem` 的 `BlockwiseDatePickerDialog`），支持跳转到任意日期，并提供“今天”快捷入口。
- 日期切换时提供平滑过渡动画，并保证数据一致性（不闪动、不展示错误日期的数据）。

## Impact
- Affected specs (new): `openspec/changes/add-timeline-date-navigation/specs/timeline-date-navigation/spec.md`
- Affected docs (reference):
  - `docs/tasks/v1.1-tasks.md` (M3)
  - `docs/PRD/PRD-v1.1.md` (Section 1.3)
- Affected code (expected):
  - `feature/timeentry/.../timeline/TimelineScreen.kt` / `TimelineViewModel.kt`
  - 可能复用/对齐 `feature/timeentry/.../timeblock/TimeBlockScreen.kt` 顶部日期导航体验
  - `core/designsystem/.../component/DatePicker.kt`

## Assumptions
- 周起始日按“周一到周日”定义（与现有 `TimeBlockViewModel`/Goal 周期计算保持一致）。
- Week Strip 的视觉细节（尺寸/圆角/选中样式）以 PRD 描述为准；本提案在 spec 中仅规范行为与一致性要求，不锁定具体 dp。

## Decisions
- 日期导航与时间块视图共享同一套顶部组件与状态（单一 `selectedDate` 事实源）。
- “今天”快捷入口放在周范围标题右侧。
- 周范围标题在跨月/跨年等情况下需要包含年份。

## Decisions
- 周范围标题年份仅在“有歧义”时展示（例如跨年、用户无法从上下文明确推断年份）。
