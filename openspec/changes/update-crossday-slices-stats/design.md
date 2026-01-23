## Context
本变更引入“Day Slice”作为展示层的统一切片概念：一个跨日 TimeEntry 在每个自然日上展示该日覆盖的区间。统计口径也需与该展示保持一致，改为按窗口重叠分钟数进行归因。

## Goals / Non-Goals
- Goals:
  - Timeline/Time Block 在查看日期 D 时，展示所有与 D 重叠的条目（跨日条目按日切片显示）。
  - 点击任何 Day Slice 编辑时，编辑对象为原始 TimeEntry；编辑页展示完整起止（含跨日）。
  - 统计在任意窗口内按重叠分钟数计算，支持跨日/跨小时正确归因。
  - 显示层对自然日边界允许使用 `24:00` 表示当日结束。
- Non-Goals:
  - 不在本变更中要求对数据库 schema 做破坏性迁移。
  - 不改变 TimeEntry 的存储精度（秒级仍保留），仅调整展示/统计的归因方式。

## Decisions
- Decision: 展示与统计统一使用半开区间 `[start, end)` 语义。
  - Why: 与现有 specs 中对统计窗口 `[startTime, endTime)` 的表达一致；也便于处理 24:00/00:00 的边界。
- Decision: 自然日与统计窗口按“用户本地时区”解释；分钟统计按“截断到分钟，不四舍五入”。
  - Why: 与 Timeline 的 `HH:mm` 分钟级展示与“隐藏秒且不四舍五入”的既有规格保持一致。
- Decision: `24:00` 仅为展示层格式化特例；内部时间点仍使用 00:00:00 的下一日边界。
- Decision: Timeline 的“未追踪时间段”计算窗口以“当天 00:00 到 24:00”作为边界，并基于当日 Day Slice 排序结果插入。

## Risks / Trade-offs
- 风险: 现有 Timeline/Statistics 可能依赖 startTime 归属进行查询优化。
  - 缓解: 在数据层增加“按 overlap 查询”的 API，并通过索引/范围查询保持性能。
- 风险: 跨日/跨小时边界的秒级精度容易导致 off-by-one 分钟。
  - 缓解: 所有分钟统计使用“截断到分钟”并明确规则（例如按秒保留但分钟归因以重叠区间的整分钟数为准）。

## Migration Plan
- 若现有代码已强制避免跨日（例如 timer 会拆分），则本变更仍需覆盖“手动编辑/导入”导致的跨日条目。
- 先落地 domain 级 overlap/Day Slice 纯函数 + 单测，再逐步替换 UI/Statistics 使用。

## Open Questions
- 对于 TimeEntry 恰好在 00:00:00 结束（即跨日边界），在前一日是否应展示 24:00 结束的 slice？（默认：是，且该 sliceEnd 展示为 24:00。）
- 对于 endTime 精确落在 00:00:00 且 startTime 也在同日，是否认为该条目跨日？（默认：否；跨日判断基于自然日不同。）
- 对于包含重叠（overlapping）的 TimeEntries，Timeline gap card 的行为如何定义在跨日场景下？（默认：沿用“同一日分组内若条目重叠则不插入 gap”。）
