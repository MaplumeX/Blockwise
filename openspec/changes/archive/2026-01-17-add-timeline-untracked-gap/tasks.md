## 1. Implementation
- [ ] 1.1 评估现有时间线数据结构与渲染方式（DayGroup + TimeEntryItem）并确定插入点
  - Evidence: 标注关键路径：`TimelineUseCases.kt` 分组/排序、`TimelineScreen.kt` LazyColumn 构建
- [ ] 1.2 在 domain 层计算未追踪时间段（gap）并产出可渲染的数据模型
  - Notes: gap 阈值 >= 1 分钟；按“天(00:00–24:00)”窗口识别：相邻记录间 gap + 首尾 gap；列表稳定
  - Evidence: 单元测试覆盖边界：1 分钟阈值、相邻/重叠不产生 gap、同日首条/末条首尾 gap、跨午夜记录不应在当天产生首尾 gap 误报
- [ ] 1.3 在时间线列表中渲染 gap 卡片（UI）
  - Notes: 视觉样式按 PRD：透明背景 + 虚线边框 + 显示时间范围；与现有 spacing 对齐
  - Evidence: 手动验收：浅/深色模式可读性一致；滚动性能无明显退化
- [ ] 1.4 点击 gap 卡片进入创建流程并预填开始/结束时间
  - Notes: 复用现有 `TimeEntryEditScreen` 预填能力；确保 endTime 可精确预填（不仅是 +1 小时默认）
  - Evidence: 手动验收：点击后进入创建页，日期/开始/结束时间均正确
- [ ] 1.5 质量与验证
  - Command: `./gradlew test`
  - Command: `./gradlew detekt`
  - Command: `./gradlew ktlintCheck`
  - Command: `./gradlew assembleDebug`

## 2. Definition of Done
- [ ] 时间线在相邻记录间隔 >= 1 分钟时插入未追踪时间段卡片
- [ ] 阈值与边界符合 PRD：不误报、不错漏（覆盖同日内相邻/重叠/1分钟临界 + 同日首尾空档 + 跨午夜记录边界）
- [ ] 点击卡片可进入创建流程，开始/结束时间预填为该空档范围
- [ ] `./gradlew test`、`./gradlew detekt`、`./gradlew ktlintCheck`、`./gradlew assembleDebug` 通过（如有既有失败需标注）
