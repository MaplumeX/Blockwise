---
文档类型: 阶段任务文档
阶段编号: 01
阶段名称: 项目基础架构搭建
版本: v1.0
创建日期: 2026-01-15
预计工期: 3-4天
前置条件: 无
---

# 阶段一：项目基础架构搭建

## 1. 阶段目标

### 1.1 核心目标

搭建 Blockwise 项目的技术基础架构，包括项目初始化、模块化结构、核心依赖集成，为后续功能开发奠定坚实基础。

### 1.2 交付成果

- 可编译运行的 Android 项目
- 完整的多模块架构结构
- 所有核心依赖库正确集成
- 统一的依赖版本管理
- 代码质量检查工具配置完成

### 1.3 技术规格

| 规格项 | 要求 |
|--------|------|
| 最低 Android 版本 | API 26 (Android 8.0) |
| 目标 Android 版本 | API 34 (Android 14) |
| Kotlin 版本 | 1.9.0+ |
| Compose 版本 | 1.5.0+ |
| Gradle 版本 | 8.0+ |

---

## 2. 任务列表

### 2.1 项目初始化

#### T1.1.1 创建Android项目

| 属性 | 值 |
|------|-----|
| **任务ID** | T1.1.1 |
| **优先级** | P0 |
| **预计耗时** | 0.5h |
| **依赖任务** | 无 |

**任务描述**：
使用 Android Studio 创建新的 Android 项目，配置基础 Gradle 设置。

**执行步骤**：
1. 创建新项目，选择 "Empty Compose Activity" 模板
2. 设置项目名称为 `Blockwise`
3. 设置包名为 `com.blockwise.app`
4. 设置最低 SDK 版本为 API 26
5. 设置目标 SDK 版本为 API 34
6. 启用 Kotlin DSL 作为 Gradle 构建脚本语言

**验收标准**：
- [ ] 项目可成功同步 Gradle
- [ ] 项目可编译通过
- [ ] 可在模拟器/真机上运行显示默认界面

---

#### T1.1.2 配置Kotlin版本

| 属性 | 值 |
|------|-----|
| **任务ID** | T1.1.2 |
| **优先级** | P0 |
| **预计耗时** | 0.5h |
| **依赖任务** | T1.1.1 |

**任务描述**：
设置 Kotlin 版本为 1.9+，启用必要的编译器插件。

**执行步骤**：
1. 在 `gradle/libs.versions.toml` 中定义 Kotlin 版本
2. 配置 Kotlin 编译器选项（JVM target 17）
3. 启用 Kotlin 序列化插件
4. 启用 KSP (Kotlin Symbol Processing) 插件

**关键配置**：
```kotlin
// build.gradle.kts (project level)
plugins {
    kotlin("jvm") version "1.9.x" apply false
    kotlin("plugin.serialization") version "1.9.x" apply false
    id("com.google.devtools.ksp") version "1.9.x-x.x.x" apply false
}
```

**验收标准**：
- [ ] Kotlin 版本 >= 1.9.0
- [ ] KSP 插件正确配置
- [ ] 序列化插件正确配置
- [ ] 项目编译无警告

---

#### T1.1.3 配置Gradle依赖管理

| 属性 | 值 |
|------|-----|
| **任务ID** | T1.1.3 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T1.1.1 |

**任务描述**：
使用 Version Catalog 统一管理所有依赖版本，确保版本一致性。

**执行步骤**：
1. 创建 `gradle/libs.versions.toml` 文件
2. 定义所有依赖的版本号
3. 定义依赖库引用
4. 定义插件引用
5. 创建依赖 bundle 分组

**文件结构**：
```
gradle/
└── libs.versions.toml    # 版本目录
```

**关键依赖分类**：
- `[versions]`: 版本号定义
- `[libraries]`: 库依赖定义
- `[bundles]`: 依赖组合
- `[plugins]`: 插件定义

**验收标准**：
- [ ] `libs.versions.toml` 文件创建完成
- [ ] 所有核心依赖版本已定义
- [ ] 模块可通过 `libs.xxx` 引用依赖
- [ ] Gradle 同步成功

---

#### T1.1.4 配置代码规范工具

| 属性 | 值 |
|------|-----|
| **任务ID** | T1.1.4 |
| **优先级** | P1 |
| **预计耗时** | 1h |
| **依赖任务** | T1.1.1 |

**任务描述**：
集成 ktlint 和 detekt 进行代码质量检查，确保代码风格统一。

**执行步骤**：
1. 添加 ktlint Gradle 插件
2. 添加 detekt Gradle 插件
3. 创建 `.editorconfig` 文件配置代码风格
4. 创建 `detekt.yml` 自定义规则配置
5. 配置 pre-commit hook（可选）

**关键配置文件**：
```
project-root/
├── .editorconfig         # 代码风格配置
├── config/
│   └── detekt/
│       └── detekt.yml    # Detekt 规则配置
```

**验收标准**：
- [ ] `./gradlew ktlintCheck` 可执行
- [ ] `./gradlew detekt` 可执行
- [ ] 代码格式化规则已配置
- [ ] 无代码规范检查错误

---

### 2.2 模块化架构搭建

#### T1.2.1 创建core:common模块

| 属性 | 值 |
|------|-----|
| **任务ID** | T1.2.1 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T1.1.3 |

**任务描述**：
创建核心通用模块，包含扩展函数、工具类、常量定义等通用代码。

**执行步骤**：
1. 创建 `core/common` 模块目录结构
2. 创建 `build.gradle.kts` 配置文件
3. 创建基础包结构
4. 添加通用扩展函数占位文件

**目录结构**：
```
core/
└── common/
    ├── build.gradle.kts
    └── src/
        └── main/
            └── kotlin/
                └── com/blockwise/core/common/
                    ├── extensions/      # 扩展函数
                    ├── utils/           # 工具类
                    └── constants/       # 常量定义
```

**模块类型**：纯 Kotlin 库模块（无 Android 依赖）

**验收标准**：
- [ ] 模块可独立编译
- [ ] 包结构符合规范
- [ ] 其他模块可依赖此模块

---

#### T1.2.2 创建core:designsystem模块

| 属性 | 值 |
|------|-----|
| **任务ID** | T1.2.2 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T1.1.3 |

**任务描述**：
创建设计系统模块，包含主题、颜色、通用 UI 组件定义。

**执行步骤**：
1. 创建 `core/designsystem` 模块目录结构
2. 配置 Compose 依赖
3. 创建主题相关包结构
4. 创建通用组件包结构

**目录结构**：
```
core/
└── designsystem/
    ├── build.gradle.kts
    └── src/
        └── main/
            └── kotlin/
                └── com/blockwise/core/designsystem/
                    ├── theme/           # 主题定义
                    │   ├── Color.kt
                    │   ├── Type.kt
                    │   └── Theme.kt
                    ├── component/       # 通用组件
                    └── icon/            # 图标资源
```

**模块类型**：Android 库模块

**验收标准**：
- [ ] 模块可独立编译
- [ ] Compose 依赖正确配置
- [ ] 包结构符合规范

---

#### T1.2.3 创建core:data模块

| 属性 | 值 |
|------|-----|
| **任务ID** | T1.2.3 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T1.1.3 |

**任务描述**：
创建核心数据层模块，包含数据库配置、基础实体、类型转换器。

**执行步骤**：
1. 创建 `core/data` 模块目录结构
2. 配置 Room 数据库依赖
3. 创建数据库相关包结构
4. 创建基础实体和转换器占位文件

**目录结构**：
```
core/
└── data/
    ├── build.gradle.kts
    └── src/
        └── main/
            └── kotlin/
                └── com/blockwise/core/data/
                    ├── database/        # 数据库配置
                    │   ├── AppDatabase.kt
                    │   └── converter/   # 类型转换器
                    ├── entity/          # 基础实体
                    └── dao/             # DAO 接口
```

**模块类型**：Android 库模块

**验收标准**：
- [ ] 模块可独立编译
- [ ] Room 依赖正确配置
- [ ] KSP 处理器正确配置

---

#### T1.2.4 创建core:domain模块

| 属性 | 值 |
|------|-----|
| **任务ID** | T1.2.4 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T1.1.3 |

**任务描述**：
创建核心领域层模块，包含通用领域模型、Repository 接口定义。

**执行步骤**：
1. 创建 `core/domain` 模块目录结构
2. 创建领域模型包结构
3. 创建 Repository 接口包结构
4. 创建基础 Use Case 基类

**目录结构**：
```
core/
└── domain/
    ├── build.gradle.kts
    └── src/
        └── main/
            └── kotlin/
                └── com/blockwise/core/domain/
                    ├── model/           # 领域模型
                    ├── repository/      # Repository 接口
                    └── usecase/         # Use Case 基类
```

**模块类型**：纯 Kotlin 库模块（无 Android 依赖）

**验收标准**：
- [ ] 模块可独立编译
- [ ] 无 Android 框架依赖
- [ ] 包结构符合 Clean Architecture

---

#### T1.2.5 创建feature模块结构

| 属性 | 值 |
|------|-----|
| **任务ID** | T1.2.5 |
| **优先级** | P0 |
| **预计耗时** | 2h |
| **依赖任务** | T1.2.1, T1.2.2, T1.2.3, T1.2.4 |

**任务描述**：
创建所有功能模块的骨架结构，包括 timeentry、statistics、goal、settings 模块。

**执行步骤**：
1. 创建 `feature/timeentry` 模块（三层结构）
2. 创建 `feature/statistics` 模块（三层结构）
3. 创建 `feature/goal` 模块（三层结构）
4. 创建 `feature/settings` 模块（三层结构）
5. 配置模块间依赖关系

**目录结构**：
```
feature/
├── timeentry/
│   ├── build.gradle.kts
│   └── src/main/kotlin/com/blockwise/feature/timeentry/
│       ├── data/            # 数据层实现
│       ├── domain/          # 业务逻辑
│       └── presentation/    # UI 展示
├── statistics/
│   └── (同上结构)
├── goal/
│   └── (同上结构)
└── settings/
    └── (同上结构)
```

**模块依赖规则**：
- feature 模块 → core 模块
- feature 模块之间**不直接依赖**
- 通过 core 模块共享通用功能

**验收标准**：
- [ ] 4 个 feature 模块全部创建
- [ ] 每个模块包含 data/domain/presentation 三层
- [ ] 模块依赖关系正确配置
- [ ] 所有模块可编译通过

---

### 2.3 核心依赖集成

#### T1.3.1 集成Jetpack Compose

| 属性 | 值 |
|------|-----|
| **任务ID** | T1.3.1 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T1.2.2 |

**任务描述**：
配置 Jetpack Compose UI 框架，包括 Compose 编译器和 Material3 组件库。

**执行步骤**：
1. 在 Version Catalog 中添加 Compose 相关版本
2. 配置 Compose 编译器插件
3. 添加 Compose UI 核心库
4. 添加 Material3 组件库
5. 配置 Compose 预览功能

**关键依赖**：
```toml
[versions]
compose-bom = "2024.x.x"
compose-compiler = "1.5.x"

[libraries]
compose-bom = { group = "androidx.compose", name = "compose-bom", version.ref = "compose-bom" }
compose-ui = { group = "androidx.compose.ui", name = "ui" }
compose-ui-graphics = { group = "androidx.compose.ui", name = "ui-graphics" }
compose-ui-tooling = { group = "androidx.compose.ui", name = "ui-tooling" }
compose-ui-tooling-preview = { group = "androidx.compose.ui", name = "ui-tooling-preview" }
compose-material3 = { group = "androidx.compose.material3", name = "material3" }
```

**验收标准**：
- [ ] Compose BOM 正确配置
- [ ] Material3 组件可使用
- [ ] Compose Preview 功能正常
- [ ] 可在 designsystem 模块中编写 Composable 函数

---

#### T1.3.2 集成Room数据库

| 属性 | 值 |
|------|-----|
| **任务ID** | T1.3.2 |
| **优先级** | P0 |
| **预计耗时** | 1h |
| **依赖任务** | T1.2.3 |

**任务描述**：
配置 Room 数据库持久化框架，包括 KSP 注解处理器。

**执行步骤**：
1. 在 Version Catalog 中添加 Room 版本
2. 添加 Room 运行时库
3. 添加 Room KTX 扩展库
4. 配置 Room KSP 注解处理器
5. 验证注解处理器工作正常

**关键依赖**：
```toml
[versions]
room = "2.6.x"

[libraries]
room-runtime = { group = "androidx.room", name = "room-runtime", version.ref = "room" }
room-ktx = { group = "androidx.room", name = "room-ktx", version.ref = "room" }
room-compiler = { group = "androidx.room", name = "room-compiler", version.ref = "room" }
```

**验收标准**：
- [ ] Room 依赖正确添加
- [ ] KSP 处理器配置正确
- [ ] 可创建简单的 Entity 和 DAO 并编译通过

---

#### T1.3.3 集成Hilt依赖注入

| 属性 | 值 |
|------|-----|
| **任务ID** | T1.3.3 |
| **优先级** | P0 |
| **预计耗时** | 1.5h |
| **依赖任务** | T1.1.3 |

**任务描述**：
配置 Hilt 依赖注入框架，创建 Application 类。

**执行步骤**：
1. 在 Version Catalog 中添加 Hilt 版本
2. 添加 Hilt Gradle 插件
3. 添加 Hilt Android 库
4. 配置 Hilt KSP 注解处理器
5. 创建 `BlockwiseApplication` 类并添加 `@HiltAndroidApp` 注解
6. 在 `AndroidManifest.xml` 中配置 Application 类

**关键依赖**：
```toml
[versions]
hilt = "2.48.x"

[libraries]
hilt-android = { group = "com.google.dagger", name = "hilt-android", version.ref = "hilt" }
hilt-compiler = { group = "com.google.dagger", name = "hilt-compiler", version.ref = "hilt" }

[plugins]
hilt = { id = "com.google.dagger.hilt.android", version.ref = "hilt" }
```

**关键代码**：
```kotlin
@HiltAndroidApp
class BlockwiseApplication : Application()
```

**验收标准**：
- [ ] Hilt 插件正确配置
- [ ] Application 类创建并注解
- [ ] AndroidManifest 配置正确
- [ ] 项目编译通过无 Hilt 相关错误

---

#### T1.3.4 集成Kotlin Coroutines

| 属性 | 值 |
|------|-----|
| **任务ID** | T1.3.4 |
| **优先级** | P0 |
| **预计耗时** | 0.5h |
| **依赖任务** | T1.1.3 |

**任务描述**：
配置 Kotlin Coroutines 异步编程框架，包括 Flow 响应式流。

**执行步骤**：
1. 在 Version Catalog 中添加 Coroutines 版本
2. 添加 Coroutines Core 库
3. 添加 Coroutines Android 库
4. 验证 Flow 可正常使用

**关键依赖**：
```toml
[versions]
coroutines = "1.7.x"

[libraries]
coroutines-core = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-core", version.ref = "coroutines" }
coroutines-android = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-android", version.ref = "coroutines" }
```

**验收标准**：
- [ ] Coroutines 依赖正确添加
- [ ] 可在代码中使用 `suspend` 函数
- [ ] 可使用 `Flow` 类型

---

#### T1.3.5 集成Kotlinx DateTime

| 属性 | 值 |
|------|-----|
| **任务ID** | T1.3.5 |
| **优先级** | P0 |
| **预计耗时** | 0.5h |
| **依赖任务** | T1.1.3 |

**任务描述**：
配置 Kotlinx DateTime 日期时间处理库，用于时区安全的时间操作。

**执行步骤**：
1. 在 Version Catalog 中添加 Kotlinx DateTime 版本
2. 添加库依赖到 common 模块
3. 验证 Instant、LocalDate 等类型可用

**关键依赖**：
```toml
[versions]
kotlinx-datetime = "0.5.x"

[libraries]
kotlinx-datetime = { group = "org.jetbrains.kotlinx", name = "kotlinx-datetime", version.ref = "kotlinx-datetime" }
```

**验收标准**：
- [ ] 依赖正确添加
- [ ] `Instant`、`LocalDate`、`LocalDateTime` 类型可用
- [ ] 时区转换功能正常

---

#### T1.3.6 集成Kotlinx Serialization

| 属性 | 值 |
|------|-----|
| **任务ID** | T1.3.6 |
| **优先级** | P0 |
| **预计耗时** | 0.5h |
| **依赖任务** | T1.1.3 |

**任务描述**：
配置 Kotlinx Serialization JSON 序列化库，用于数据导入导出。

**执行步骤**：
1. 在 Version Catalog 中添加 Serialization 版本
2. 确保 Kotlin 序列化插件已配置（T1.1.2）
3. 添加 JSON 序列化库依赖
4. 验证 `@Serializable` 注解可用

**关键依赖**：
```toml
[versions]
kotlinx-serialization = "1.6.x"

[libraries]
kotlinx-serialization-json = { group = "org.jetbrains.kotlinx", name = "kotlinx-serialization-json", version.ref = "kotlinx-serialization" }
```

**验收标准**：
- [ ] 序列化插件正确配置
- [ ] JSON 库依赖正确添加
- [ ] `@Serializable` 注解可用于数据类

---

#### T1.3.7 集成Vico图表库

| 属性 | 值 |
|------|-----|
| **任务ID** | T1.3.7 |
| **优先级** | P1 |
| **预计耗时** | 0.5h |
| **依赖任务** | T1.3.1 |

**任务描述**：
配置 Vico 图表库，用于统计数据可视化。

**执行步骤**：
1. 在 Version Catalog 中添加 Vico 版本
2. 添加 Vico Compose 库依赖
3. 添加 Vico Compose M3 主题库
4. 验证图表组件可用

**关键依赖**：
```toml
[versions]
vico = "2.0.x"

[libraries]
vico-compose = { group = "com.patrykandpatrick.vico", name = "compose", version.ref = "vico" }
vico-compose-m3 = { group = "com.patrykandpatrick.vico", name = "compose-m3", version.ref = "vico" }
```

**验收标准**：
- [ ] Vico 依赖正确添加
- [ ] 图表组件可在 Compose 中使用
- [ ] 与 Material3 主题兼容

---

#### T1.3.8 集成Compose Navigation

| 属性 | 值 |
|------|-----|
| **任务ID** | T1.3.8 |
| **优先级** | P0 |
| **预计耗时** | 0.5h |
| **依赖任务** | T1.3.1 |

**任务描述**：
配置 Compose Navigation 导航组件，实现应用内页面导航。

**执行步骤**：
1. 在 Version Catalog 中添加 Navigation 版本
2. 添加 Navigation Compose 库依赖
3. 验证 NavHost、NavController 可用

**关键依赖**：
```toml
[versions]
navigation = "2.7.x"

[libraries]
navigation-compose = { group = "androidx.navigation", name = "navigation-compose", version.ref = "navigation" }
```

**验收标准**：
- [ ] Navigation 依赖正确添加
- [ ] `NavHost`、`NavController` 可使用
- [ ] 可定义基础导航路由

---

#### T1.3.9 集成测试框架

| 属性 | 值 |
|------|-----|
| **任务ID** | T1.3.9 |
| **优先级** | P1 |
| **预计耗时** | 1h |
| **依赖任务** | T1.1.3 |

**任务描述**：
配置测试框架，包括 JUnit5、Mockk、Turbine、Coroutines Test。

**执行步骤**：
1. 在 Version Catalog 中添加测试相关版本
2. 添加 JUnit5 依赖
3. 添加 Mockk 依赖
4. 添加 Turbine (Flow 测试) 依赖
5. 添加 Coroutines Test 依赖
6. 配置 JUnit5 测试运行器
7. 编写示例测试验证配置

**关键依赖**：
```toml
[versions]
junit5 = "5.10.x"
mockk = "1.13.x"
turbine = "1.0.x"

[libraries]
junit5-api = { group = "org.junit.jupiter", name = "junit-jupiter-api", version.ref = "junit5" }
junit5-engine = { group = "org.junit.jupiter", name = "junit-jupiter-engine", version.ref = "junit5" }
mockk = { group = "io.mockk", name = "mockk", version.ref = "mockk" }
turbine = { group = "app.cash.turbine", name = "turbine", version.ref = "turbine" }
coroutines-test = { group = "org.jetbrains.kotlinx", name = "kotlinx-coroutines-test", version.ref = "coroutines" }
```

**验收标准**：
- [ ] 测试依赖全部正确添加
- [ ] JUnit5 测试可运行
- [ ] Mockk 可用于创建 Mock 对象
- [ ] Turbine 可用于测试 Flow

---

## 3. 依赖关系图

```
T1.1.1 创建Android项目
    │
    ├── T1.1.2 配置Kotlin版本
    │
    ├── T1.1.3 配置Gradle依赖管理
    │       │
    │       ├── T1.2.1 创建core:common模块
    │       │       │
    │       ├── T1.2.2 创建core:designsystem模块 ──┐
    │       │       │                              │
    │       ├── T1.2.3 创建core:data模块 ─────────┼── T1.2.5 创建feature模块结构
    │       │       │                              │
    │       ├── T1.2.4 创建core:domain模块 ───────┘
    │       │
    │       ├── T1.3.3 集成Hilt依赖注入
    │       │
    │       ├── T1.3.4 集成Kotlin Coroutines
    │       │
    │       ├── T1.3.5 集成Kotlinx DateTime
    │       │
    │       ├── T1.3.6 集成Kotlinx Serialization
    │       │
    │       └── T1.3.9 集成测试框架
    │
    └── T1.1.4 配置代码规范工具

T1.2.2 创建core:designsystem模块
    │
    └── T1.3.1 集成Jetpack Compose
            │
            ├── T1.3.7 集成Vico图表库
            │
            └── T1.3.8 集成Compose Navigation

T1.2.3 创建core:data模块
    │
    └── T1.3.2 集成Room数据库
```

---

## 4. 验收标准清单

### 4.1 项目结构验收

| 验收项 | 要求 | 检查方式 |
|--------|------|----------|
| 项目可编译 | `./gradlew build` 成功 | 命令行执行 |
| 项目可运行 | 应用可安装到设备 | 模拟器/真机测试 |
| 模块结构完整 | 全部模块创建 | 目录检查 |
| 依赖版本统一 | Version Catalog 配置 | 文件检查 |

### 4.2 模块结构验收

| 模块 | 验收标准 |
|------|----------|
| `:app` | 可编译运行，Hilt 配置正确 |
| `:core:common` | 纯 Kotlin 模块，无 Android 依赖 |
| `:core:designsystem` | Compose 依赖正确，可编写 Composable |
| `:core:data` | Room 配置正确，KSP 处理器可用 |
| `:core:domain` | 纯 Kotlin 模块，无框架依赖 |
| `:feature:timeentry` | 三层结构完整，依赖 core 模块 |
| `:feature:statistics` | 三层结构完整，依赖 core 模块 |
| `:feature:goal` | 三层结构完整，依赖 core 模块 |
| `:feature:settings` | 三层结构完整，依赖 core 模块 |

### 4.3 依赖集成验收

| 依赖 | 验收标准 |
|------|----------|
| Jetpack Compose | Composable 函数可编写，Preview 可用 |
| Room | Entity/DAO 可定义，KSP 处理正常 |
| Hilt | @Inject/@HiltAndroidApp 可用 |
| Coroutines | suspend/Flow 可使用 |
| Kotlinx DateTime | Instant/LocalDate 可使用 |
| Kotlinx Serialization | @Serializable 可用 |
| Vico | 图表组件可导入 |
| Navigation | NavHost/NavController 可用 |
| 测试框架 | JUnit5 测试可运行 |

### 4.4 代码质量验收

| 验收项 | 要求 |
|--------|------|
| ktlint 检查 | `./gradlew ktlintCheck` 无错误 |
| detekt 检查 | `./gradlew detekt` 无严重问题 |
| 编译警告 | 无编译警告（或已知可忽略） |

---

## 5. 最终项目结构

```
Blockwise/
├── app/                              # 应用入口模块
│   ├── build.gradle.kts
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           └── kotlin/com/blockwise/app/
│               ├── BlockwiseApplication.kt
│               └── MainActivity.kt
│
├── core/                             # 核心模块
│   ├── common/                       # 通用工具
│   │   ├── build.gradle.kts
│   │   └── src/main/kotlin/...
│   ├── designsystem/                 # 设计系统
│   │   ├── build.gradle.kts
│   │   └── src/main/kotlin/...
│   ├── data/                         # 数据层基础
│   │   ├── build.gradle.kts
│   │   └── src/main/kotlin/...
│   └── domain/                       # 领域层基础
│       ├── build.gradle.kts
│       └── src/main/kotlin/...
│
├── feature/                          # 功能模块
│   ├── timeentry/                    # 时间记录
│   │   ├── build.gradle.kts
│   │   └── src/main/kotlin/.../
│   │       ├── data/
│   │       ├── domain/
│   │       └── presentation/
│   ├── statistics/                   # 统计分析
│   ├── goal/                         # 目标管理
│   └── settings/                     # 设置
│
├── gradle/
│   └── libs.versions.toml            # 版本目录
│
├── config/
│   └── detekt/
│       └── detekt.yml                # Detekt 配置
│
├── .editorconfig                     # 代码风格配置
├── build.gradle.kts                  # 根构建脚本
├── settings.gradle.kts               # 模块配置
└── gradle.properties                 # Gradle 属性
```

---

## 6. 风险与注意事项

### 6.1 潜在风险

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| 依赖版本冲突 | 编译失败 | 使用 BOM 管理版本，测试兼容性 |
| KSP 配置错误 | Room/Hilt 不工作 | 仔细检查插件版本匹配 |
| 模块依赖循环 | 编译失败 | 严格遵循依赖方向规则 |

### 6.2 注意事项

1. **Kotlin 与 KSP 版本匹配**：KSP 版本必须与 Kotlin 版本兼容
2. **Compose 编译器版本**：必须与 Kotlin 版本匹配
3. **Hilt 与 Compose**：使用 `hilt-navigation-compose` 进行集成
4. **模块可见性**：正确配置 `api`/`implementation` 依赖范围

---

*文档版本: v1.0*
*阶段状态: 待开始*
