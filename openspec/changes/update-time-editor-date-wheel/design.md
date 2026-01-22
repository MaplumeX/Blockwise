## Context
- 当前时间线 Bottom Sheet 时间编辑控件仅支持“时/分”双列滚轮，draft 仅持有 `baseDate + startTime(LocalTime) + endTime(LocalTime)`。
- 当前跨日仅通过“当 end <= start 时把 end +1 天”隐式支持，无法表达超过 1 天的跨度；并且在起始日期为“今天”时会导致推断到未来（明天）。
- domain use case（Create/Update）存在 24 小时最大时长限制，会阻止长跨度条目保存。

## Goals
- 支持在时间线 Bottom Sheet 中显式选择起始/结束“日期+时间”，并实时计算起止时间点与时长。
- 取消最大时长上限，仅保留 `end > start` 的有效性约束。
- 将“跨日推断”与“禁止未来日期”规则对齐：推断不得产生未来日期。

## Non-Goals
- 不引入跨日条目在 Timeline/TimeBlock 的 Day Slice 展示与统计归因（另案）。
- 不引入新的全局日期选择策略（例如禁止在时间线选择未来日期）；仅对时间编辑控件做“不可选未来日期”的防线。

## Decisions
- **Draft 表示**：在时间编辑 draft 中引入 `startDate` / `endDate`（LocalDate）并与 `startTime` / `endTime`（LocalTime）组合成时间点；最终保存时统一在 `TimeZone.currentSystemDefault()` 下转为 `Instant`。
- **跨日推断优先级**：仅当 `endDate == startDate` 且用户把结束时间调至 `<= startTime` 时才触发“自动将 endDate 置为 startDate+1 天”；若会落到未来，则不推断并将状态判为无效。
- **校验一致性**：UI 侧禁用主按钮与提示文案基于“时间点比较”（`endInstant > startInstant`），domain 层也仅做相同的必要校验（避免 UI/Domain 口径不一致）。
- **日期列范围**：日期列不循环；默认仅允许选择“今天”及最近 29 天内的日期（共 30 天，含今天）；不得出现未来日期。

## Risks / Trade-offs
- 日期列采用“最近 30 天”窗口可控制滚轮长度与性能，但会限制创建/编辑到更早日期；需要确保编辑模式下仍能预填条目的实际 start/end 日期（若条目超出 30 天窗口，需在实现阶段定义处理策略：扩展范围或提供替代入口）。
- 现有实现与规范中存在“分钟滚轮编辑需保留秒数”的要求（`openspec/specs/timeentry-time-precision/spec.md`）；引入日期列时需避免进一步破坏秒数保留行为。