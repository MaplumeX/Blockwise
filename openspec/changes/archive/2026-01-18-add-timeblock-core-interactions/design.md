## Context
M6 定义 Time Block 的核心交互：选中查看详情 + 空白范围创建。
此变更建立在 M5 的时间块日网格基础之上，因此交互实现需要与 M5 的网格坐标系统一致。

## Goals / Non-Goals
- Goals:
  - 统一 5 分钟吸附规则（像素->分钟->网格）
  - 点击记录段选中整条 TimeEntry，并在底部展示详情
  - 长按空白拖拽范围创建并预填时间
- Non-Goals:
  - M7 的冲突/重叠渲染与冲突列表
  - 复杂编辑（拖拽调整起止时间）作为可选增强，不作为 M6 必交付

## Decisions
- 选择状态在 ViewModel 管理：避免 Compose 层重组造成状态丢失。
- 范围选择用一个明确的数据结构表示（startMinutes/endMinutes，已吸附），并在 UI 层用 overlay 表达。

## Risks / Trade-offs
- Compose pointerInput 与垂直滚动冲突：需要明确长按阈值与手势优先级，避免滚动时误触进入 range selection。
- 详情卡片占用屏幕空间：需要保证不会遮挡关键交互；可考虑半高 bottom sheet 或可折叠。

## Open Questions
- 无。
