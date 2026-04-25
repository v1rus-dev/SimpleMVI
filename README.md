# SimpleMVI

SimpleMVI is a Kotlin Multiplatform library for building small, explicit MVI-style state containers.

The project is split into a small core module and optional Compose integrations, so the state-management layer can stay independent from UI framework code.

## Modules

| Module | Artifact | Description |
| --- | --- | --- |
| `simple-mvi-core` | `yegor.cheprasov.simplemvi:simple-mvi-core` | Core MVI primitives for shared Kotlin code. |
| `simple-mvi-compose` | `yegor.cheprasov.simplemvi:simple-mvi-compose` | Compose Multiplatform integration built on top of the core module. |
| `simple-mvi-compose-android` | `yegor.cheprasov.simplemvi:simple-mvi-compose-android` | Android-specific Compose lifecycle and ViewModel integration. |

## Supported Targets

The multiplatform modules are configured for:

- Android
- JVM
- iOS x64
- iOS arm64
- iOS simulator arm64
- Linux x64

## Installation

Add Maven Central to your repositories:

```kotlin
repositories {
    mavenCentral()
    google()
}
```

Add the modules you need:

```kotlin
dependencies {
    implementation("yegor.cheprasov.simplemvi:simple-mvi-core:<version>")
}
```

For Compose Multiplatform:

```kotlin
dependencies {
    implementation("yegor.cheprasov.simplemvi:simple-mvi-compose:<version>")
}
```

For Android Compose integration:

```kotlin
dependencies {
    implementation("yegor.cheprasov.simplemvi:simple-mvi-compose-android:<version>")
}
```

Replace `<version>` with the published library version.

## Project Setup

The repository uses:

- Kotlin Multiplatform
- Android Gradle Plugin
- Compose Multiplatform
- Compose Compiler Gradle plugin
- Vanniktech Maven Publish

The main version catalog is located at [`gradle/libs.versions.toml`](gradle/libs.versions.toml).

## Build Locally

```bash
./gradlew build
```

To run checks for a specific module:

```bash
./gradlew :simple-mvi-core:check
./gradlew :simple-mvi-compose:check
./gradlew :simple-mvi-compose-android:check
```

## Publishing

The modules are configured with `com.vanniktech.maven.publish` and are intended to be published to Maven Central.

Before publishing, make sure signing credentials and Maven Central credentials are configured in your local Gradle environment.

## License

This project is licensed under the Apache License 2.0. See [`LICENSE`](LICENSE) for details.
