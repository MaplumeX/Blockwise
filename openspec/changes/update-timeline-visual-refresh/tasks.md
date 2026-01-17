## 1. Implementation
- [ ] 1.1 确认现有时间线渲染与卡片结构（定位/读代码）
  - Evidence: 记录关键入口与组件路径（TimelineScreen / TimeEntryItem / DateGroupHeader）
- [ ] 1.2 实现垂直时间轴视觉（连接线 + 节点圆点）
  - Notes: 优先采用轻量绘制（例如 `drawBehind`）或最小布局变更，避免额外层级导致重组
  - Evidence: 手动验收：浅/深色模式均可读；列表滚动无明显掉帧
- [ ] 1.3 调整记录卡片信息结构
  - 移除左侧细条颜色指示器
  - 右上角显示活动类型
  - 标题展示备注（无备注则活动类型名）
  - 标题下方展示起止时间
  - Evidence: 手动验收：信息结构符合 PRD v1.1 M1
- [ ] 1.4 实现时间范围 chip 样式
  - 等宽字体（Monospace）
  - 背景块（圆角 4dp；padding 6x3dp）
  - Evidence: 手动验收：浅/深色模式对比度一致
- [ ] 1.5 Lint/Format 校验
  - Command: `./gradlew detekt`
  - Command: `./gradlew ktlintCheck`
- [ ] 1.6 运行单元测试与构建
  - Command: `./gradlew test`
  - Command: `./gradlew assembleDebug`

## 2. Definition of Done
- [ ] M1 的 UI 变更在时间线页面可见，并与 `docs/PRD/PRD-v1.1.md` 1.2 对齐
- [ ] 浅/深色模式均通过基础可读性检查
- [ ] `./gradlew test` 通过（若存在既有失败，需标注）
- [ ] `./gradlew detekt` 与 `./gradlew ktlintCheck` 通过
