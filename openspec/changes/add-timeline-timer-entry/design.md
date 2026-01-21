## Context
该变更在 Timeline Today（选中日期=今天）增加一个列表内的计时入口，并在计时中展示“计时中条目”。项目已存在 TimerManager/TimerService/TimerUseCases 计时体系，并存在活动类型选择器实现（TimelineEntryBottomSheet 的 Dialog 以及 TimerScreen 的 BottomSheet）。

## Goals / Non-Goals
- Goals:
  - 提供稳定的一键计时入口（开始/完成），只在 Timeline Today 可见。
  - 计时中条目在列表中即时反馈，并随时间更新。
  - 生成的 TimeEntry 保存秒级精度，且满足 end > start；跨日自动拆分。
- Non-Goals:
  - 暂停/继续、多计时器并行、系统通知栏控制。

## Decisions
- Decision: 复用既有 timer domain（TimerManager/UseCases）作为单计时器数据源。
  - Why: 已有状态机（Idle/Running/Paused）与持久化恢复逻辑，符合“单计时器约束”，避免新增并行计时逻辑。
- Decision: Timeline 列表中的“计时中条目”作为 UI 层的额外 item（或扩展 TimelineItem sealed class）。
  - Why: 需要与已有 Entry/UntrackedGap 一致地参与分组与渲染；若以 UI 层插入，改动更小但可维护性需评估。
- Decision: 活动类型选择框优先复用现有实现，必要时抽取为可复用组件。
  - Why: 需求明确“可复用现有活动类型选择器”，避免重复实现。

## Risks / Trade-offs
- UI 插入方式（直接插入 vs 扩展 TimelineItem）会影响测试与长期可维护性。
- 使用现有 TimerService 前台通知可能超出 v1.2.2 预期（但非新增要求）；需要确保 Timeline 入口不会强依赖通知交互。

## Migration Plan
无数据迁移。新增行为需确保不影响既有 Timeline FAB/BottomSheet 交互。

## Open Questions
- “最新条目下方”的定义：Timeline 按时间正序（最新在底部），因此按钮插入在当日分组 items 之后。
- Today 中隐藏原 Quick Create FAB（右下角 "+"），改由列表内计时入口承担主要入口角色。
- 计时中条目的视觉样式细节（badge/标签）是否已有设计稿？若无，先用最小可用实现并留出可替换的 slot。
