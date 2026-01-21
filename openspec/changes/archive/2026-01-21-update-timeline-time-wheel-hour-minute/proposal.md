# Change: 时间选择控件更新为“时 / 分”分离滚轮

## Why
当前时间选择滚轮以“分钟数（00:00–23:59）单列”方式展示，滚动选择时不便于快速定位小时或只微调分钟。
将时间选择拆分为“小时 + 分钟”两列滚轮，可提升可控性与可读性，并与常见时间选择心智一致。

## What Changes
- 更新 Timeline 时间编辑区域的起始/结束时间选择器：
  - BEFORE: 每个时间点为单列滚轮（00:00–23:59）。
  - AFTER: 每个时间点由两列滚轮组成：小时（00–23）与分钟（00–59）。
- 布局保持：起始与结束时间仍在同一行左右并排；每侧内部为“时 / 分”两列。
- 步进与对齐：
  - 小时滚轮按 1 小时步进。
  - 分钟滚轮按 1 分钟步进。
- 显示格式：最终展示为等宽字体的 `HH:mm`，并随滚轮变化实时更新。

## Impact
- Affected specs:
  - `openspec/specs/timeline-interactions/spec.md`
- Likely affected code:
  - `feature/timeentry/.../presentation/timeline/TimelineEntryBottomSheet.kt`（TimeWheel 相关实现）
- UX/behavioral changes:
  - 时间选择从单列滚轮变为双列（时/分）滚轮；不改变时间精度与校验规则（仍为分钟精度）。

## Non-Goals
- 不改变 Timeline 创建/编辑的时间校验规则（例如创建模式的 end > start）。
- 不引入新的第三方时间选择器依赖。
- 不改变 Time Block 日视图的 5 分钟网格交互规则。

## Open Questions
- 无
