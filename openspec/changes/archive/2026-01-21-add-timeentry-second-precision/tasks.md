## 1. Specification
- [x] 1.1 新增 `openspec/changes/add-timeentry-second-precision/specs/timeentry-time-precision/spec.md`：定义 TimeEntry 秒级记录/比较/计时写入需求与场景。
- [x] 1.2 更新 `openspec/changes/add-timeentry-second-precision/specs/timeline-visual/spec.md`：定义 Timeline 时间范围显示为 `HH:mm` 并截断秒（不四舍五入）。
- [x] 1.3 运行 `openspec validate add-timeentry-second-precision --strict --no-interactive` 并修复所有校验问题。

## 2. Implementation (after approval)
- [x] 2.1 数据与校验：统一以秒级精度判断 end > start（移除任何“依赖分钟差”为有效性的逻辑）；分钟级编辑保存时保留原秒值。
- [x] 2.2 计时：确保计时开始/结束使用系统当前时间（含秒）；计时保存门槛改为“> 0 秒”（不再依赖“最少 1 分钟”）。
- [x] 2.3 时间线展示：确保 Timeline 列表时间范围只显示 `HH:mm`，秒被截断隐藏（不四舍五入）。
- [x] 2.4 未追踪时间段（Gap）：保持 `HH:mm` 展示规则，并明确跨天 `24:00` 的显示不受秒影响。

## 3. Tests & Validation (after approval)
- [x] 3.1 单测：覆盖秒级边界（start==end、相差 1 秒、跨分钟但秒截断显示）。
- [x] 3.2 如果存在 UI 测试：验证 Timeline 列表不出现秒。
- [x] 3.3 运行 `./gradlew test`（如有 instrumentation 测试任务，运行对应任务）。
