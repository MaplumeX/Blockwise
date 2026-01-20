## 1. Specification
- [ ] 1.1 在 `openspec/changes/add-timeline-quick-create-fab/specs/timeline-interactions/spec.md` 补充“时间线快速创建（FAB + 创建模式 Bottom Sheet）”的新增需求与场景。
- [ ] 1.2 运行 `openspec validate add-timeline-quick-create-fab --strict --no-interactive` 并修复所有校验问题。

## 2. UI - Timeline
- [ ] 2.1 在时间线视图增加右下角 FAB（+），确保位于底部导航栏上方且触摸目标 >= 48dp。
- [ ] 2.2 为 FAB 增加无障碍语义（例如 contentDescription: “添加时间记录”），并确保仅在时间线视图可见。

## 3. Create Mode Bottom Sheet
- [ ] 3.1 复用 v1.2 Bottom Sheet 容器与基础布局，新增“创建模式”展示：主按钮为“创建”。
- [ ] 3.2 创建模式隐藏删除/合并/拆分等危险/结构性操作入口。
- [ ] 3.3 关闭行为与 v1.2 一致（关闭按钮、点遮罩），关闭不产生脏数据/误创建。

## 4. Default Time Rules & Validation
- [ ] 4.1 实现“当前时间”默认值规则：按分钟精度对齐；日期归属按选中日期是否为今天决定。
- [ ] 4.2 实现创建约束：结束时间必须晚于起始时间；当结束时间 <= 起始时间时，“创建”按钮置灰并在时间区域显示提示文案“结束时间需晚于起始时间”。

## 5. Tests & Validation
- [ ] 5.1 为默认时间规则增加单元测试（覆盖：选中日期=今天、选中日期!=今天、分钟对齐）。
- [ ] 5.2 增加/调整 UI 测试：
  - FAB 仅在时间线视图展示
  - 点击 FAB 后 Bottom Sheet 弹出
  - 默认值符合规则，且 end<=start 时创建按钮不可用并显示提示
  - Bottom Sheet 可关闭且不会误创建
- [ ] 5.3 运行 `./gradlew test`。
- [ ] 5.4 如仓库已有对应 instrumentation 测试任务，运行 `:feature:timeentry:connectedDebugAndroidTest`（或等价任务）。
