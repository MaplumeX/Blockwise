# Change: 更新时间编辑（日期滚轮 + 取消时长上限 + 跨日推断）

## Why
当前创建/编辑 TimeEntry 存在“单次记录时长 <= 24 小时”的硬限制（domain use case 校验），且时间线 Bottom Sheet 的时间编辑仅支持“时/分”双滚轮，无法显式选择结束日期。
这会阻碍 v1.3 目标：允许用户创建/编辑跨越多日的单条 TimeEntry，并在不产生未来日期的前提下提供合理的跨日推断。

## What Changes
- 取消单条 TimeEntry 的最大时长上限校验；仅保留基础有效性：`endTime` 必须严格晚于 `startTime`。
- 时间线 Bottom Sheet 的起始/结束时间选择控件新增“日期”列（列顺序：日期 → 时 → 分），起始/结束可分别选择日期与时间。
- 跨日自动推断：当结束日期 == 起始日期且用户把结束时间调至 <= 起始时间时，优先将结束日期自动调整为起始日期的次日；若该次日会晚于今天（将产生未来日期），则不自动调整并视为无效输入。
- 校验：结束时间点必须严格晚于开始时间点；不满足时底部主按钮置灰，并给出明确错误提示。
- 默认值：创建模式下起始日期默认跟随当前选中日期（但不得晚于今天；若选中日期晚于今天，则起始日期默认=今天），结束日期默认等于起始日期。

## Impact
- Affected specs:
  - `openspec/specs/timeline-interactions/spec.md`
  - `openspec/specs/timeentry-time-precision/spec.md`
- Likely affected code:
  - `feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/presentation/timeline/TimelineEntryBottomSheet.kt`
  - `feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/presentation/timeline/TimelineViewModel.kt`
  - `feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/domain/usecase/timeentry/CreateTimeEntryUseCase.kt`
  - `feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/domain/usecase/timeentry/UpdateTimeEntryUseCase.kt`
  - 相关单元/Compose 测试

## Non-Goals
- 不包含 v1.3 的 Day Slice 展示与统计口径调整（PRD 第 2/4/5 节）。
- 不改变 Timer 功能（除非其路径间接依赖 Create/Update use case 的时长上限校验）。

## Resolved Product Decisions
- 日期滚轮范围：仅允许选择“今天”及最近 29 天内的日期（共 30 天，含今天）。

## Open Questions
- 无
