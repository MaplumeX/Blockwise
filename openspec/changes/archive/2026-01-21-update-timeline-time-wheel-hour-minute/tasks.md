## 1. Specification
- [x] 1.1 在 `openspec/changes/update-timeline-time-wheel-hour-minute/specs/timeline-interactions/spec.md` 追加/修改与“起始/结束时间滚轮拆分为时/分两列”相关的需求与场景。
- [x] 1.2 运行 `openspec validate update-timeline-time-wheel-hour-minute --strict --no-interactive` 并修复所有校验问题。

## 2. Implementation
- [x] 2.1 将 Timeline Bottom Sheet 的起始/结束时间选择器从“单列（00:00–23:59）滚轮”更新为“小时列 + 分钟列”双列滚轮。
- [x] 2.2 保持起始/结束两侧并排布局；每侧内部为“时 / 分”两列。
- [x] 2.3 确保小时滚轮步进为 1 小时、分钟滚轮步进为 1 分钟，并且选择变化实时反映为 `HH:mm`（等宽字体）。

## 3. Tests & Validation
- [x] 3.1 增加/调整 UI 测试：起始/结束时间选择器均为“时滚轮 + 分滚轮”两列，并随滚轮变化实时更新显示。
- [x] 3.2 运行 `./gradlew test`。
- [x] 3.3 如仓库已有对应 instrumentation 测试任务，运行 `:feature:timeentry:connectedDebugAndroidTest`（或等价任务）。
