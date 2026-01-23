## 1. Proposal / Validation
- [ ] 1.1 确认现有 Timeline/Time Block 的数据归属口径（按 startTime 还是按 overlap），并在本变更中声明与之对齐的修改点
- [ ] 1.2 确认现有“未追踪时间段”卡片计算窗口与插入规则，并补齐跨日 Day Slice 情况下的期望行为
- [ ] 1.3 完成 spec deltas：Timeline 展示、Time Block 展示、交互（点击编辑）、Statistics 统计口径
- [ ] 1.4 `openspec validate update-crossday-slices-stats --strict --no-interactive` 通过

## 2. Implementation (apply stage)
- [ ] 2.1 在 domain 层引入/复用 Day Slice 计算：给定 TimeEntry 与 LocalDate，返回该日 slice 的 [sliceStart, sliceEnd)
- [ ] 2.2 Timeline：按所选日期 D 获取“与 D 重叠”的条目并转为 Day Slice 列表，按 sliceStart 升序；确保与 gap card 逻辑一致
- [ ] 2.3 Timeline：时间范围展示层支持 `24:00`（仅当 sliceEnd 为自然日边界）
- [ ] 2.4 Timeline：跨日标识与编辑入口保持对原始 TimeEntry 的引用
- [ ] 2.5 Time Block：渲染数据切片基于 Day Slice；跨小时切片保持连续；跨日标识仅展示一次
- [ ] 2.6 Statistics：实现 overlapMinutes(entry, window) 与按日/小时/分类的重叠聚合
- [ ] 2.7 补齐/更新单元测试（Day Slice 计算、overlapMinutes、按小时桶拆分等）
- [ ] 2.8 运行 `./gradlew test`（如有 UI 变更则补充必要的 instrumentation 覆盖）
