# Project Context

## Purpose
Blockwise 是一个 Android 时间记录/时间块管理应用，目标是帮助用户以“条目（time entry）”的方式记录活动，并提供统计/目标/设置等能力。

项目采用模块化结构（`core/*` + `feature/*` + `app`）来隔离职责，便于迭代与测试。

## Tech Stack
- 平台：Android（minSdk 26 / targetSdk 36 / compileSdk 36）
- 语言与构建：Kotlin（2.0.21）+ Gradle Kotlin DSL + AGP（8.13.2）+ JDK 17
- UI：Jetpack Compose（Compose BOM 2024.12.01）+ Material 3
- 架构：分层（domain/data/ui）+ 多模块；ViewModel + Flow/Coroutines
- DI：Hilt（含 `androidx.hilt:hilt-navigation-compose`，KSP 处理器）
- 数据：Room（KSP，导出 schema 到 `core/data/schemas`）
- 异步：Kotlin Coroutines（1.9.0）
- 序列化与时间：kotlinx.serialization + kotlinx-datetime
- 设置/偏好：AndroidX DataStore（preferences）
- 后台任务：WorkManager（项目已引入版本依赖）
- 日志：Timber
- 图表：Vico（statistics feature）
- 图片：Coil（版本已锁定）
- 质量工具：ktlint + detekt

## Project Conventions

### Code Style
- 格式化由 `.editorconfig` 统一：
  - 缩进：4 空格；行宽：120；文件末尾保留 1 个空行；LF 换行
  - Kotlin：允许尾随逗号；ktlint 风格 `android_studio`
- import 分组顺序（来自 `docs/CODING_STANDARDS.md`）：
  1) `android.*` 2) `androidx.*` 3) `com.maplume.blockwise.*` 4) `java.*`/`javax.*` 5) 第三方（`kotlinx.*` 等）
- 命名（项目惯例）：
  - `PascalCase`（类型）、`camelCase`（函数/属性）、`SCREAMING_SNAKE_CASE`（常量）
  - Composable：`@Composable fun FooBar(...)`；ktlint 允许 Composable 特例
- 空安全：尽量避免 `!!`；优先 `?.` / `?:` / `let`

### Architecture Patterns
- 模块边界（见 `settings.gradle.kts`）：
  - `app`：应用入口与导航/装配层，可依赖所有 `core/*` 与 `feature/*`
  - `core/*`：共享能力
    - `core/common`：跨模块工具/扩展/通用实现
    - `core/domain`：领域模型与 use-case（JVM）
    - `core/data`：Room + 数据层实现 + DI wiring（Android library）
    - `core/designsystem`：Compose 组件与主题（Android library）
    - `core/testing`：测试公共依赖（JUnit5/Mockk/Turbine 等）
  - `feature/*`：按功能拆分（`timeentry`/`statistics`/`goal`/`settings`/`onboarding`）
- 依赖规则（项目约定）：feature 只能依赖 `core/*`，禁止 feature-to-feature；跨 feature 协调放在 `app`
- 分层建议：
  - domain：纯 Kotlin（模型/use-case），不依赖 Android
  - data：DAO/Repository/数据源实现，使用 `Dispatchers.IO` 或注入 dispatcher
  - ui：Compose + ViewModel；使用 `viewModelScope` 收集 Flow

### Testing Strategy
- JVM 模块（例如 `core/common`、`core/domain`）使用 JUnit 5（`useJUnitPlatform()`）
- Android 模块包含：
  - 本地单元测试：JUnit4（`testImplementation(libs.junit)`）
  - Android instrumentation：`androidx.test.ext:junit` + Espresso + Compose UI Test
- Flow 测试使用 Turbine；mock 使用 Mockk
- 期望：优先在 domain/data 层写快速单测；UI 行为用 Compose instrumentation 覆盖关键路径

### Git Workflow
- Commit 规范：Conventional Commits（`type(scope): subject`）
  - 例：`feat(statistics): add weekly chart`、`fix(app): correct duration display`
- PR 期望：描述清晰、必要时附 UI 截图；至少通过 `./gradlew test`

## Domain Context
- 核心业务概念：
  - Time Entry：用户记录的一段活动/时间块（开始/结束/类型/备注等）
  - Statistics：对时间条目做聚合统计（例如按天/周/月、按类型）
  - Goal：目标与达成追踪（例如某类活动累计时长目标）
  - Settings：用户偏好与配置（DataStore）
- 时间与时区：项目引入 `kotlinx-datetime`；建议在 domain 层使用 `Instant/LocalDateTime` 或统一的时间表示，避免在多处自行处理时区。

## Important Constraints
- 目标运行环境：Android 8.0+（minSdk 26），JDK 17 构建
- 仓库采用多模块依赖约束：禁止 feature-to-feature 依赖
- 质量门槛：detekt `maxIssues: 0`（不允许新增问题）；遵循 ktlint/.editorconfig
- Room schema 必须随构建导出（KSP `room.schemaLocation` 已配置）

## External Dependencies
- 当前无后端服务/第三方 API 的显式接入信息（如后续引入：同步服务、登录、分析等，请在此补充）
- 主要第三方库：Room、Hilt、DataStore、WorkManager、Timber、Vico、Coil、kotlinx.serialization、kotlinx-datetime
