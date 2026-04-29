# Repository Guidelines

## Project Structure & Module Organization

This is a Gradle Kotlin Multiplatform repository for the SimpleMVI library. Core library code lives in `simple-mvi-core/src/commonMain`, with tests in `simple-mvi-core/src/commonTest`. Compose Multiplatform integration is in `simple-mvi-compose/src/commonMain`, and Android-specific lifecycle, ViewModel, and `SavedStateHandle` helpers are in `simple-mvi-compose-android/src/main`. Sample apps are under `samples/compose-multiplatform-app` and `samples/native-android-app`. Documentation assets, including images used by the README, are in `docs/images`.

## Build, Test, and Development Commands

Use the checked-in Gradle wrapper for all tasks. Run `./gradlew` from Unix-like shells, including WSL, and use `gradlew.bat` or `.\gradlew.bat` from Command Prompt or PowerShell. CI runs with JDK 17, so prefer that locally as well even though the Android and JVM targets compile to Java 11 bytecode.

```bash
./gradlew check --no-configuration-cache
```

Runs the main validation task used by CI.

```bash
./gradlew :simple-mvi-core:allTests --no-configuration-cache
```

Runs the full multiplatform test suite for the core module.

```bash
./gradlew :samples:native-android-app:assembleDebug
./gradlew :samples:compose-multiplatform-app:assembleDebug
```

Builds the Android variants of the sample apps for local verification. The Compose Multiplatform sample also contains an iOS app entry point under `samples/compose-multiplatform-app/iosApp`, so avoid treating the samples directory as Android-only.

## Coding Style & Naming Conventions

Write Kotlin using four-space indentation and idiomatic Kotlin naming: `PascalCase` for classes and interfaces, `camelCase` for functions and properties, and package names under `io.github.v1rusdev.simplemvi`. Keep public APIs small and explicit, especially in the `core` module. Match existing MVI vocabulary: `StateUi`, `IntentUi`, `EffectUi`, `SimpleMVI`, and `MviViewModel`. Keep naming consistent with published artifacts too: the Android integration lives in the `simple-mvi-compose-android` module but is published as `simple-mvi-android`. The project does not currently define ktlint or detekt tasks, so rely on Kotlin compiler checks and consistent formatting with nearby code.

## Testing Guidelines

Tests use `kotlin.test` and `kotlinx-coroutines-test`; core tests also use Koin where dependency injection behavior is relevant. Place multiplatform tests in `src/commonTest/kotlin` and name files after the unit under test, for example `SimpleMVIEffectsTest.kt`. Add tests for state transitions, effect delivery, coroutine behavior, and DI integration when modifying core behavior.

## Commit & Pull Request Guidelines

Recent commits use short imperative messages such as `Add tests`, `Update workflows`, and `Update version to 0.3.0`. Keep commit subjects concise and focused on one change. Pull requests should include a brief description, the modules affected, test commands run, and screenshots or recordings when sample UI changes are visible. Link related issues when applicable and call out publishing or API compatibility impact for library changes.

## Security & Configuration Tips

Do not commit signing keys, Maven Central credentials, or local Android/iOS configuration. Publishing is handled by GitHub Actions and Vanniktech Maven Publish; keep release-sensitive values in repository secrets.
