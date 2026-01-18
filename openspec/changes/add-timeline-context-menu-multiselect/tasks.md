## 1. Implementation
- [ ] 1.1 盘点时间线现有交互与动作落点
  - Evidence: 确认 `TimelineViewModel.kt` 现有删除/拆分/合并与 selection mode 状态；记录现有点击/长按行为
- [ ] 1.2 定义上下文菜单的状态模型与事件
  - Evidence: 明确菜单锚点（entryId + 屏幕坐标/anchor bounds）与打开/关闭时机
- [ ] 1.3 实现时间线条目点击打开上下文菜单（浮动、就地）
  - Evidence: 点击任意记录卡片在点击附近出现菜单，并且边界避让正确
- [ ] 1.4 菜单项：编辑
  - Evidence: 从菜单触发编辑与现有导航一致（触发 NavigateToEdit）
- [ ] 1.5 菜单项：删除/拆分/合并
  - Evidence: 删除/拆分/合并复用现有确认弹窗与 usecase，行为一致
- [ ] 1.6 多选模式交互校正
  - Evidence: 长按进入多选；多选后继续点击其他卡片能 toggle；退出清晰（TopBar 关闭按钮）
- [ ] 1.7 测试与验证
  - Command: `./gradlew test`
  - Command: `./gradlew detekt`
  - Command: `./gradlew ktlintCheck`
  - Command: `./gradlew assembleDebug`

## 2. Definition of Done
- [ ] 时间线支持点击弹出浮动上下文菜单（编辑、删除、拆分、合并）
- [ ] 时间线支持长按进入多选模式并可多选切换
- [ ] 菜单操作与现有数据层一致（状态回写及时）
- [ ] 误触成本低：进入/退出多选与菜单关闭路径清晰
- [ ] `./gradlew test` / `./gradlew detekt` / `./gradlew ktlintCheck` 通过（若存在既有失败需标注）
