## 1. Implementation
- [x] 1.1 移除 Create/Update TimeEntry 的 24 小时最大时长校验，并确保仅保留 `end > start`。
- [x] 1.2 更新/新增 domain 单测：允许保存 >24h 的 TimeEntry；移除“超过 24 小时失败”的断言与文案依赖。
- [x] 1.3 为时间线 Bottom Sheet 的 draft 引入 start/end 日期字段，并将有效性判断升级为“时间点比较”。
- [x] 1.4 实现时间滚轮三列（日期→时→分）：
- [x] 1.4.1 日期列：步进 1 天；不循环；范围为最近 30 天（含今天）；today 为最后一项。
- [x] 1.4.2 起始/结束独立选择日期与时间；滚轮变化时即时刷新 UI（显示的起止与时长）。
- [x] 1.5 实现跨日自动推断：当 endDate==startDate 且 endTime<=startTime 时，将 endDate 自动设为 startDate+1 天；若该次日会晚于 today，则不推断并视为无效输入。
- [x] 1.6 校验与反馈：当 end 时间点不严格晚于 start 时间点时，底部主按钮置灰，并显示明确错误提示（创建/编辑均适用）。
- [x] 1.7 更新/新增 Compose 测试：
- [x] 1.7.1 日期列仅包含最近 30 天（含今天），且不出现未来日期（today 为底部）。
- [x] 1.7.2 起始为非 today 时，endTime 调至 <= startTime 会触发 endDate 自动 +1 天并允许保存。
- [x] 1.7.3 起始为 today 时，endTime 调至 <= startTime 不得推断到明天，需提示无效并禁用主按钮。
- [x] 1.8 运行 `./gradlew test`（如有 UI 测试再跑对应 instrumentation），确保无回归。

## 2. Validation
- [x] 2.1 `openspec validate update-time-editor-date-wheel --strict --no-interactive` 通过。
- [x] 2.2 关键验收用例手动回归（对应 PRD 3.1~3.2.1）：>24h 保存、跨日推断不产生未来、日期列仅 today 及以前。
