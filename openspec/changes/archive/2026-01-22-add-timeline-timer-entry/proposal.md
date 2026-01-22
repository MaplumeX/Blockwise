# Change: Add Timeline Today Timer Entry Point

## Why
在当天时间线视图提供稳定、可预期的一键计时入口，降低开始记录真实活动的操作成本，尤其支持“边做边记”的使用场景。

## What Changes
- 在 Timeline View 且选中日期=今天时，在列表中（最新条目下方）展示计时入口按钮（推荐 Extended FAB），并在计时中切换文案为“完成”。
- 点击“开始”先弹出活动类型选择框；取消不产生任何记录。
- 计时开始后在当天时间线列表展示“计时中条目”，并随时间更新“进行中/现在”的语义。
- 点击“完成”结束计时并创建 TimeEntry（秒级精度），并确保 end > start（最小时长 1 秒）。
- 若计时跨越 24:00:00，则写入时按自然日拆分为两条 TimeEntry（分界点 24:00:00/00:00:00）。

## Impact
- Affected specs: timeline-interactions, timeline-visual, timeentry-time-precision
- Affected code (expected): feature/timeentry (TimelineScreen/TimelineViewModel/TimelineItem), existing timer flow (TimerManager/TimerUseCases), time entry creation/repository layer
- Non-goals: 暂停/继续、多计时器并行、系统通知栏控制（不在 v1.2.2 范围内）
