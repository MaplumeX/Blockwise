# Change: Update Timeline Visual Refresh (v1.1 M1)

## Why
v1.0 的时间线列表在信息结构和视觉层级上偏“功能性”，用户快速扫读一整天记录的成本较高。
v1.1 M1 通过引入垂直时间轴与更清晰的卡片信息结构，提升可读性与一致性（浅/深色模式），为后续 v1.1 的时间块视图集成打下视觉基线。

## What Changes
- 在时间线列表左侧引入垂直时间轴视觉：连接线 + 节点圆点。
- 调整单条记录卡片信息结构：
  - 移除左侧细条颜色指示器。
  - 右上角显示活动类型。
  - 标题展示“备注”，无备注则展示活动类型名。
  - 标题下方展示起止时间。
- 调整时间范围展示样式：等宽字体（Monospace）+ 浅色背景块（圆角 4dp，padding 6x3dp）。

## Impact
- Affected docs:
  - `docs/tasks/v1.1-tasks.md` (M1)
  - `docs/PRD/PRD-v1.1.md` (Section 1.2)
- Affected code (expected):
  - `feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/presentation/timeline/TimelineScreen.kt`
  - `feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/presentation/timeline/TimeEntryItem.kt`
  - `feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/presentation/timeline/DateGroupHeader.kt`
- UX/Performance considerations:
  - 新增绘制层（轴线/节点）需要控制重组与绘制开销，确保列表滚动帧率目标不受影响。
  - 浅/深色模式下对比度需要一致；时间范围 chip 背景色需与主题体系对齐。

## Open Questions
- 垂直轴线与节点是否需要“贯穿组间间距/跨 sticky header”，还是仅在条目行内绘制即可？
- 时间范围 chip 的具体颜色来源：使用 `MaterialTheme.colorScheme.surfaceVariant` 等通用色，还是在 design system 定义专用 token？
- 右上角活动类型的呈现形式：纯文本、tag/chip、或带图标？（本提案默认：纯文本，最小改动）
