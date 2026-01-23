# Change: 跨日条目按 Day Slice 展示，并将统计改为重叠分钟口径

## Why
当前按“startTime 归属日”展示/统计会导致跨日条目在部分日期缺失，且统计口径与用户在按日视角看到的内容不一致。

## What Changes
- Timeline List：按日展示与日期 D 重叠的 TimeEntry（以 Day Slice 作为展示单元），跨日条目在其覆盖的每一天都展示对应 Day Slice。
- Timeline List：时间范围展示保持分钟级 `HH:mm`；当 slice 结束点为自然日边界 (D+1 00:00:00) 时，展示层允许用 `24:00`。
- Timeline List：对跨日 TimeEntry 的 Day Slice 增加轻量跨日标识（显示实际起止“日期+时间”）。
- Timeline List / Time Block：点击某天的 Day Slice 进入详情/编辑时，编辑对象始终为原始 TimeEntry，编辑页展示完整起止范围。
- Time Block Day View：按日网格渲染与日期 D 重叠的 Day Slice；跨小时切片规则延续现有实现；跨日标识在任一小时切片展示一次即可。
- Statistics：所有统计（按日趋势、按小时分布、分类统计）改为基于统计窗口与 TimeEntry 的重叠分钟数 `overlapMinutes(entry, window)` 计算，不再仅按 startTime 归属。

## Impact
- Affected specs:
  - `openspec/specs/timeline-visual/spec.md`
  - `openspec/specs/timeline-interactions/spec.md`
  - `openspec/specs/timeblock-view/spec.md`
  - `openspec/specs/timeblock-interactions/spec.md`
  - (新增) `statistics-overlap` 能力规格
- Affected code (expected):
  - Timeline 数据加载与按日分组/排序逻辑
  - “未追踪时间段”插入逻辑（需要从 Day Slice 角度保持一致）
  - Time Block 切片渲染的数据准备逻辑
  - Statistics 聚合计算逻辑（daily/hourly/category）
