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

# Repository Guidelines

## Project Structure & Module Organization

- `app/`: Android application entrypoint (Jetpack Compose).
- `core/`: shared modules
  - `core/common`: cross-cutting utilities and shared Kotlin code (JVM).
  - `core/domain`: domain models + use-cases (JVM).
  - `core/data`: data layer (Room) and DI wiring (Android library).
  - `core/designsystem`: Compose UI components/theme (Android library).
- `feature/`: feature modules (`timeentry`, `statistics`, `goal`, `settings`).
- `config/`: tooling configs (e.g. `config/detekt/detekt.yml`).
- `docs/`: engineering docs and task plans (see `docs/CODING_STANDARDS.md`).

**Dependency rule**: `app` may depend on all `core/*` and `feature/*`. A `feature/*` module may depend only on `core/*` (no feature-to-feature deps; coordinate via `app`).

## Build, Test, and Development Commands

Use the Gradle wrapper (`./gradlew`) and JDK 17.

- Build a debug APK: `./gradlew assembleDebug`
- Run all unit tests: `./gradlew test`
- Run instrumentation tests (device/emulator): `./gradlew connectedAndroidTest`

For day-to-day dev, open the project in Android Studio (Ladybug+) for run configurations and Compose previews.

## Coding Style & Naming Conventions

- Formatting is defined in `.editorconfig` (spaces, 4-space indent; Kotlin/KTS max line length 120).
- Naming: `PascalCase` (types), `camelCase` (functions/properties), `SCREAMING_SNAKE_CASE` (constants).
- Prefer small, layer-focused classes; follow the module boundaries above.

## Testing Guidelines

- JVM modules (`core/common`, `core/domain`) use JUnit 5 (`src/test/kotlin`) and run via `./gradlew test`.
- Android modules use local JVM unit tests (JUnit 4) and optional instrumentation tests (`src/androidTest/...`).
- Name test classes `*Test.kt` and keep tests close to the unit under test.

## Commit & Pull Request Guidelines

- Commits follow Conventional Commits (`type(scope): subject`, scope optional). Examples from history: `feat: data layer`, `fix: fix compile error`.
- PRs should include: a clear description, linked issue/task doc, screenshots for UI changes, and passing checks (`./gradlew test` at minimum).

## Security & Configuration Tips

- Do not commit secrets/keystores. Keep machine-local settings in `local.properties` (ignored by git).

