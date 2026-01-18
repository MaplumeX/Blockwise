## 1. Proposal Tasks (No Code)
- [x] 1.1 现状对齐与范围确认
  - Evidence: 记录当前时间块日视图网格方向/滚动方向/粒度常量位置（例如 `TimeBlockDayView.kt`）与 Timeline 入口集成点（例如 `TimelineScreen.kt`）。
  - Evidence: 从 `docs/PRD/PRD-v1.2.md` 提取 v1.2 对“时间块视图集成（更新）”的硬约束与非目标。
- [x] 1.2 需求澄清
  - Evidence: v1.2 仅支持“长按空白区域并拖拽”创建；空白点击不作为创建入口。
  - Evidence: “空白段提示（点击空白区域高亮连续空白段）”不在本条目验收范围，后续单独迭代。
- [x] 1.3 撰写 spec delta：timeblock-view
  - Evidence: 在 `openspec/changes/update-timeblock-view-horizontal-grid/specs/timeblock-view/spec.md` 中以 MODIFIED/ADDED 形式表达 v1.2 的方向约束与段渲染规则。
  - Evidence: 每个 Requirement 至少包含 1 个 `#### Scenario:`。
- [x] 1.4 验证提案
  - Evidence: `openspec validate update-timeblock-view-horizontal-grid --strict --no-interactive` 通过。

## 2. Implementation Plan (For Apply Stage)
- [x] 2.1 将 TimeBlockDayView 的网格绘制与段渲染逻辑校准到“小时为行、分钟为列”的横向网格（24×12）
- [x] 2.2 确认不支持横向滚动（输入手势/布局约束）
- [x] 2.3 校验跨小时切片与圆角策略符合 spec
- [x] 2.4 回归验证：Timeline 入口切换/偏好恢复/日期一致性
- [x] 2.5 运行验证：`./gradlew test`、`./gradlew assembleDebug`
