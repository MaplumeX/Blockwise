<!-- OPENSPEC:START -->
# OpenSpec Instructions

These instructions are for AI assistants working in this project.

Always open `@/openspec/AGENTS.md` when the request:
- Mentions planning or proposals (words like proposal, spec, change, plan)
- Introduces new capabilities, breaking changes, architecture shifts, or big performance/security work
- Sounds ambiguous and you need the authoritative spec before coding

Use `@/openspec/AGENTS.md` to learn:
- How to create and apply change proposals
- Spec format and conventions
- Project structure and guidelines

Keep this managed block so 'openspec update' can refresh the instructions.

<!-- OPENSPEC:END -->

# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概述

Blockwise 是一款基于柳比歇夫时间管理法的 Android 原生应用，采用 Jetpack Compose 构建。

**核心技术栈**：
- Kotlin 2.0.21 + Jetpack Compose (Material Design 3)
- Clean Architecture + MVVM
- Room 2.6.1 (数据库) + Hilt 2.54 (依赖注入)
- Kotlin Coroutines 1.9.0 + Flow (异步处理)
- Vico 2.0.0-beta.3 (图表)
- 最低 SDK: Android 8.0 (API 26)，目标 SDK: Android 14 (API 36)

## 常用命令

```bash
# 构建
./gradlew assembleDebug          # Debug APK
./gradlew assembleRelease        # Release APK
./gradlew clean                  # 清理构建

# 测试
./gradlew test                   # 所有单元测试
./gradlew :core:domain:test      # 特定模块测试
./gradlew connectedAndroidTest   # 仪器测试（需设备）

# 代码质量
./gradlew detekt                 # Detekt 代码检查
./gradlew ktlintCheck            # ktlint 格式检查
./gradlew ktlintFormat           # 自动修复格式问题
```

## 项目结构

```
app/                    # 应用入口，MainActivity，Hilt Application
core/
  ├── common/           # 通用工具、常量、扩展函数 (JVM)
  ├── domain/           # 领域模型、仓储接口、UseCase (JVM)
  ├── data/             # Room 数据库、DAO、Entity、Mapper (Android)
  └── designsystem/     # Compose UI 组件、主题 (Android)
feature/
  ├── timeentry/        # 时间记录功能
  ├── statistics/       # 统计分析功能
  ├── goal/             # 目标管理功能
  └── settings/         # 设置功能
config/detekt/          # Detekt 配置
docs/                   # 项目文档和任务计划
```

**依赖规则**：`app` 可依赖所有模块；`feature/*` 只能依赖 `core/*`，不能互相依赖。

## 架构模式

```
Presentation (Composable + ViewModel)
         ↓ UI State / Events
Domain (UseCase + Repository接口 + Model)
         ↓ Repository Interface
Data (Repository实现 + DAO + Entity)
```

- **单向数据流**：用户交互 → ViewModel → UseCase → Repository → DAO → Database → Flow → UI
- **依赖倒置**：Presentation → Domain ← Data

## 代码风格要点

- 缩进：4 空格，最大行宽 120 字符
- 命名：`PascalCase` (类型)，`camelCase` (函数/属性)，`SCREAMING_SNAKE_CASE` (常量)
- 空安全：避免 `!!`，优先使用 `?.`、`?:` 和 `let`
- 协程：ViewModel 用 `viewModelScope`，Repository 用 `Dispatchers.IO`
- 通用扩展函数放 `core/common`，特定领域扩展放对应 feature 模块

## 关键文档

- `docs/PRD.md` - 产品需求文档
- `docs/技术框架方案.md` - 技术架构设计
- `docs/CODING_STANDARDS.md` - 完整开发规范
- `docs/tasks/` - 各阶段任务文档
- `AGENTS.md` - 仓库开发指南
