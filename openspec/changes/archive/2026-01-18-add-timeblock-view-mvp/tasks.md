## 1. Implementation
- [x] 1.1 盘点当前 TimeBlock 相关实现与 Timeline 入口形态
  - Evidence: 确认 `feature/timeentry/.../timeblock/*` 能作为可复用 Composable 嵌入 Timeline；确认现有 Timeline 顶部结构与 actions 槽位
- [x] 1.2 定义“时间线/时间块”视图模式枚举与持久化 Key
  - Evidence: 在 `SettingsDataStore` 增加 key + Flow + setter；默认值与现有行为一致
- [x] 1.3 新增 Timeline 容器层：TopAppBar actions 提供同屏切换列表与时间块视图
  - Evidence: TopAppBar actions 中存在切换入口；切换后不丢失选中日期；返回/重新进入可恢复上次视图
- [x] 1.3.1 统一迁移 Time Block 入口
  - Evidence: 现有独立 `time_block` route 不再作为主入口；Today/其他入口统一跳转到 Timeline 容器并进入 timeblock 模式
- [x] 1.4 时间块视图 MVP：24 小时轴 + 5 分钟网格（24×12）布局与滚动
  - Evidence: 00–23 小时轴可见；网格映射到分钟无明显偏移；不支持横向滚动
- [x] 1.5 段胶囊渲染：按 TimeEntry 范围渲染连续段，避免逐格渲染
  - Evidence: 同一 TimeEntry 跨小时渲染连续；性能上不做 288 格逐项绘制
- [x] 1.6 数据一致性：在任一视图创建/编辑/删除能实时反映到另一视图
  - Evidence: repository Flow 驱动；切换视图不出现“错误日期数据”
- [x] 1.7 验证
  - [x] `./gradlew test`
  - [x] `./gradlew assembleDebug`

## 2. Definition of Done
- [x] Timeline 内支持“列表 ↔ 时间块”同屏切换
- [x] 视图偏好可持久化并在下次进入恢复
- [x] 时间块视图具备 24×12（5 分钟）网格与段胶囊渲染
- [x] 两视图共享同一选中日期与同一数据源，变更实时同步
- [x] `./gradlew test` 与 `./gradlew assembleDebug` 通过
