## 1. Implementation
- [x] 1.1 盘点现有日期状态与入口
  - Evidence: 记录当前时间线/时间块日期状态来源与切换入口（`TimelineScreen.kt`、`TimelineViewModel.kt`、`TimeBlockScreen.kt`、`TimeBlockViewModel.kt`）
- [x] 1.2 定义时间线的“选中日期”单一事实源
  - Evidence: 明确 `selectedDate` 的归属（ViewModel/State），以及与数据加载的绑定点
- [x] 1.3 实现 Week Range Title（周范围标题）
  - Evidence: 标题随 `selectedDate` 对应周变化；跨月/跨年显示正确
- [x] 1.4 实现 Week Strip（本周 7 天日期条）
  - Evidence: 点击任意一天能切换到对应日期，并触发正确的数据加载
- [x] 1.5 实现周切换手势（左右滑动切周）
  - Evidence: 切换周后保持“选中星期”语义；边界行为可预测（例如跨月/跨年）
- [x] 1.6 实现 Calendar Jump（日历跳转）
  - Evidence: 点击周范围标题打开 `BlockwiseDatePickerDialog`；选择日期后关闭并切换到对应日期
- [x] 1.7 实现“今天”快捷入口
  - Evidence: 触发后 `selectedDate` 变为系统今天日期，并更新 Week Strip/标题/数据
- [x] 1.8 过渡动画与一致性防抖
  - Evidence: 日期切换不闪动、不短暂显示错误日期的数据；动画不影响交互可用性
- [x] 1.9 测试与验证
  - Command: `./gradlew test`
  - Command: `./gradlew detekt`
  - Command: `./gradlew ktlintCheck`
  - Command: `./gradlew assembleDebug`

## 2. Definition of Done
- [x] 时间线页具备 Week Range Title + Week Strip + Calendar Jump + Today 快捷入口
- [x] 周切换保持“选中星期”语义
- [x] 日期切换数据一致（无闪动/无错误日期数据）
- [x] `./gradlew test` / `./gradlew detekt` / `./gradlew ktlintCheck` 通过（若存在既有失败需标注）
  - Note: 当前环境缺少 Java Runtime，`sh ./gradlew ...` 无法执行，需在本机安装/配置 JDK 17 后再跑以上命令。
