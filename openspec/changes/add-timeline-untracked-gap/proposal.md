# Change: Add Timeline Untracked Gap (v1.1 M2)

## Why
时间线在连续记录之间存在空档时，用户难以快速识别“未记录”的时间范围，补录成本高。
M2 通过在空档处插入“未追踪时间段”卡片，并提供一键创建入口，降低补录摩擦。

## What Changes
- 在时间线列表中识别时间空档（阈值：>= 1 分钟）：
  - 相邻记录之间的空档。
  - 每天首条记录开始前（相对 00:00）与末条记录结束后（相对 24:00）的空档。
- 对每个空档插入“未追踪时间段”卡片，展示空档时间范围，并与现有时间轴视觉一致（同一列表流内呈现）。
- 点击“未追踪时间段”卡片进入创建时间记录流程，开始/结束时间按空档预填。

## Impact
- Affected specs:
  - `openspec/specs/timeline-visual/spec.md` (extend)
- Affected code (expected):
  - `feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/domain/usecase/timeline/TimelineUseCases.kt`
  - `feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/presentation/timeline/TimelineScreen.kt`
  - `feature/timeentry/src/main/kotlin/com/maplume/blockwise/feature/timeentry/presentation/timeline/TimeEntryItem.kt`
  - `app/src/main/kotlin/com/maplume/blockwise/MainActivity.kt`

## Open Questions
- 跨天首尾空档的边界：是否以“当天 00:00–24:00”作为检测窗口，并在首条记录开始前、末条记录结束后插入 gap 卡片？（当前按此默认）
- 创建入口：是否需要直接预填开始/结束时间（精确到分钟），并允许用户在编辑页调整？（建议：是）
