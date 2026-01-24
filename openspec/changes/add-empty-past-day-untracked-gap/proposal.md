# Change: Add Full-Day Untracked Gap For Empty Past Day (v1.3.1)

## Why
当用户在时间线视图查看“今天之前”的某天，且当天没有任何时间记录时，当前的空状态（例如“无时间记录”）对用户的补录行为引导较弱。
相比之下，“未追踪时间段”卡片本身就是一个低摩擦的补录入口：明确告诉用户缺失的时间范围，并可一键进入创建流程。

## What Changes
- 当用户在 Timeline 列表视图查看日期 D，且 D 严格早于“今天”（以设备系统时区定义的自然日为准）：
  - 若与区间 `[D 00:00:00, D+1 00:00:00)` 有重叠的 TimeEntry 数量为 0：
    - 不显示“无时间记录”类空状态
    - 显示 1 条“未追踪时间段”卡片，范围为 `00:00–24:00`
    - 点击卡片复用现有“未追踪时间段”交互：进入创建流程并预填 `D 00:00` 到 `D+1 00:00`
- 若日期 D 下存在至少 1 条 TimeEntry：保持既有规则（条目 + 其间未追踪时间段）不变。
- 对于“今天”：不强制替换空状态（可继续保留引导型空态）。

## Impact
- Affected specs:
  - `openspec/specs/timeline-visual/spec.md` (modify)
- Affected code (expected):
  - `feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/domain/usecase/timeline/TimelineUseCases.kt`（按选中日期产出 DayGroup / 插入 UntrackedGap）
  - `feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/presentation/timeline/TimelineScreen.kt`（空状态分支 / 列表渲染）
  - `feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/presentation/timeline/UntrackedGapItem.kt`（展示 00:00–24:00）
  - `feature/timeentry/src/test/kotlin/com/maplume/blockwise/feature/timeentry/domain/usecase/timeline/TimelineUntrackedGapTest.kt`（新增/更新用例）

## Open Questions
- 无（需求已明确：仅影响“今天之前”且无记录的日期；以设备系统时区定义自然日边界）。
