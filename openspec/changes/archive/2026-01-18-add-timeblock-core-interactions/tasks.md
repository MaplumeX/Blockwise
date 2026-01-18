## 1. Implementation
- [x] 1.1 明确 TimeBlock 日视图的 5 分钟网格映射与吸附规则
  - Evidence: 定义像素->分钟->5 分钟吸附的一致算法；边界处理（0..24h）明确
- [x] 1.2 记录段选中态：选中整条 TimeEntry
  - Evidence: 点击任意切片后，跨小时所有切片一致高亮；再次点击可保持选中或切换
- [x] 1.3 底部详情卡片
  - Evidence: 选中后展示活动、时间范围、时长、备注、标签；提供至少“编辑”入口
- [x] 1.4 空白区域长按拖拽范围创建
  - Evidence: 长按后进入 range selection；拖拽过程实时更新范围；松手触发 NavigateToCreate 并预填起止时间
- [x] 1.5 验证吸附一致性
  - Evidence: 选择范围与预填时间一致；5 分钟粒度无偏差；弱化未选中段不影响可读
- [x] 1.6 验证
  - [x] `./gradlew test`
  - [x] `./gradlew assembleDebug`

## 2. Definition of Done
- [x] 点击记录段可选中整条记录并显示底部详情卡片
- [x] 空白区域支持长按拖拽范围创建，范围吸附到 5 分钟格并正确预填
- [x] 选中态反馈清晰且不影响可读性
- [x] `./gradlew test` 与 `./gradlew assembleDebug` 通过
