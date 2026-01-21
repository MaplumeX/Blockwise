# Change: 时间线视图快速创建（FAB + 创建模式 Bottom Sheet）

## Why
时间线视图当前的创建入口依赖“点击空白段/卡片菜单/间隙卡片”等上下文操作，入口不稳定且不易被发现。需要一个稳定、可预期的一键创建入口，降低创建一条时间记录的路径成本。

## What Changes
- 在时间线视图提供右下角悬浮操作按钮（FAB，+），位于底部导航栏上方且不遮挡内容。
- FAB 仅在时间线“列表视图（Timeline list view）”展示；进入 Time Block 日视图或其它页面/模块不展示。（产品确认：选项 A）
- 用户点击 FAB 后弹出 Bottom Sheet（创建模式），起始/结束时间默认按“当前时间”规则预填。
- 创建模式 Bottom Sheet 复用 v1.2 的 Bottom Sheet 交互承载，但：
  - 底部主按钮文案为“创建”（替代“修改/保存”）。
  - 不展示删除/合并/拆分等危险/结构性操作入口。
  - 关闭行为与 v1.2 一致（点关闭按钮或点遮罩关闭），且关闭不产生脏数据/误创建。
- 创建约束：结束时间必须晚于起始时间；当结束时间 <= 起始时间时，“创建”按钮置灰不可点，并在时间区域提示“结束时间需晚于起始时间”。

## Impact
- Affected specs:
  - `openspec/specs/timeline-interactions/spec.md`
- Likely affected code:
  - `feature/timeentry/.../presentation/timeline/TimelineScreen.kt`
  - `feature/timeentry/.../presentation/timeline/TimelineViewModel.kt`
  - 复用/扩展 v1.2 Bottom Sheet 相关 UI 组件（以现有实现为准）
- UX/behavioral changes:
  - 时间线视图新增稳定的创建入口（FAB）。
  - 新增“创建模式 Bottom Sheet”作为创建流程的一种入口形式。

## Non-Goals
- 不改变“从 untracked gap 卡片创建”的现有行为（仍按现有规范/实现执行）。
- 不改动 Time Block 日视图的长按拖拽创建交互（除非后续明确要求）。
- 不在 v1.2.1 中引入新的创建字段或复杂创建向导（仅复用既有 Bottom Sheet 能力并切换为创建模式）。

## Resolved Product Decisions
- FAB 仅在 Timeline 列表视图显示，不在 Time Block 日视图显示。
- 默认时间按分钟精度对齐时，秒数向下取整（对齐到该分钟的 00 秒）。

## Open Questions
- 无
