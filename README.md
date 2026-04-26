<p align="center">
  <img src="docs/images/simplemvi-banner.png" alt="SimpleMVI banner"/>
</p>

[![CI](https://github.com/v1rus-dev/SimpleMVI/actions/workflows/gradle.yml/badge.svg)](https://github.com/v1rus-dev/SimpleMVI/actions/workflows/gradle.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.v1rus-dev/simple-mvi-core?label=Maven%20Central)](https://central.sonatype.com/search?q=g%3Aio.github.v1rus-dev)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/kotlin-2.3.21-7F52FF.svg?logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/compose-1.10.3-4285F4.svg)](https://www.jetbrains.com/compose-multiplatform/)

![Android](https://img.shields.io/badge/android-3DDC84?style=flat&logo=android&logoColor=white)
![iOS](https://img.shields.io/badge/ios-000000?style=flat&logo=apple&logoColor=white)

SimpleMVI is a small Kotlin Multiplatform library for building explicit MVI-style state containers.

It provides a minimal set of contracts for **state**, **intent**, and **effect**, plus Compose Multiplatform helpers for lifecycle-aware effect collection and ViewModel integration.

This is intentionally a simple implementation. It does not force a classic MVI reducer, action pipeline, middleware, or store framework. You handle intents in `onIntent`, update state with `updateState`, and emit one-time effects when needed.

## Why SimpleMVI

- Immutable UI state through `StateFlow`.
- One public input for UI actions: `onIntent(intent)`.
- One-time UI events through a separate effects flow.
- No required reducer or middleware layer.
- Works without Compose, but includes Compose Multiplatform helpers.
- Small enough to use for screen ViewModels and shared app-level stores.

## Installation

- Latest version: see the Maven Central badge above.
- Group ID: `io.github.v1rus-dev`
- Core module: `simple-mvi-core`
- Compose Multiplatform module: `simple-mvi-compose`
- Android helpers module: `simple-mvi-android`

<details open>
<summary>Version catalog</summary>

```toml
[versions]
simplemvi = "<latest-version>"

[libraries]
# Core MVI contracts and state container
simplemvi-core = { module = "io.github.v1rus-dev:simple-mvi-core", version.ref = "simplemvi" }

# Compose Multiplatform ViewModel and effect collection helpers
simplemvi-compose = { module = "io.github.v1rus-dev:simple-mvi-compose", version.ref = "simplemvi" }

# Android-only lifecycle and SavedStateHandle helpers
simplemvi-android = { module = "io.github.v1rus-dev:simple-mvi-android", version.ref = "simplemvi" }
```

</details>

<details>
<summary>Gradle DSL</summary>

```kotlin
repositories {
    google()
    mavenCentral()
}

dependencies {
    implementation("io.github.v1rus-dev:simple-mvi-core:<latest-version>")

    // Compose Multiplatform helpers
    implementation("io.github.v1rus-dev:simple-mvi-compose:<latest-version>")

    // Android-only helpers, including SavedStateHandle support
    implementation("io.github.v1rus-dev:simple-mvi-android:<latest-version>")
}
```

</details>

## Modules

| Module | Target | Use it for |
| --- | --- | --- |
| `simple-mvi-core` | Android, iOS | MVI contracts, state holder, and effect flow. |
| `simple-mvi-compose` | Android, iOS | Compose Multiplatform `MviViewModel` and effect collection. |
| `simple-mvi-android` | Android | Android lifecycle helpers and `SavedStateHandle` extension. |

## Core Concepts

Every SimpleMVI feature uses the same three contracts:

```kotlin
import io.github.v1rusdev.simplemvi.core.EffectUi
import io.github.v1rusdev.simplemvi.core.IntentUi
import io.github.v1rusdev.simplemvi.core.StateUi

data class ProfileState(
    val name: String = "Jon Doe",
    val isRefreshing: Boolean = false,
) : StateUi

sealed interface ProfileIntent : IntentUi {
    data object RefreshClick : ProfileIntent
    data object BackClick : ProfileIntent
}

sealed interface ProfileEffect : EffectUi {
    data object NavigateBack : ProfileEffect
    data class ShowMessage(val text: String) : ProfileEffect
}
```

- `StateUi` is durable UI state and should be renderable at any time.
- `IntentUi` is a user or UI action.
- `EffectUi` is a one-time event such as navigation, snackbar, toast, or dialog.

## Compose ViewModel

Use `MviViewModel` when a Compose screen owns its state through a ViewModel.

```kotlin
import io.github.v1rusdev.simplemvi.compose.MviViewModel

class ProfileViewModel : MviViewModel<ProfileState, ProfileIntent, ProfileEffect>(
    initialState = ProfileState(),
) {
    override fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.RefreshClick -> refresh()
            ProfileIntent.BackClick -> sendEffect(ProfileEffect.NavigateBack)
        }
    }

    private fun refresh() {
        updateState { copy(isRefreshing = true) }
        sendEffect(ProfileEffect.ShowMessage("Refresh started"))
    }
}
```

Collect state and effects separately in Compose:

```kotlin
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.v1rusdev.simplemvi.compose.CollectEffectsUiEvent

@Composable
fun ProfileRoute(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffectsUiEvent(viewModel.uiEffects) { effect ->
        when (effect) {
            ProfileEffect.NavigateBack -> onBack()
            is ProfileEffect.ShowMessage -> {
                // Show a snackbar, toast, or dialog.
            }
        }
    }

    ProfileScreen(
        state = state.value,
        onIntent = viewModel::onIntent,
    )
}
```

The UI should call one function only:

```kotlin
ProfileScreen(
    state = state.value,
    onIntent = viewModel::onIntent,
)
```

## Store Delegation

If you already have a base class, delegate `SimpleMVI` to a store created by `mvi(...)`.

```kotlin
import androidx.lifecycle.ViewModel
import io.github.v1rusdev.simplemvi.core.SimpleMVI
import io.github.v1rusdev.simplemvi.core.mvi

class ProfileViewModel : ViewModel(),
    SimpleMVI<ProfileState, ProfileIntent, ProfileEffect> by mvi(
        initialState = ProfileState(),
    ) {

    override fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.RefreshClick -> updateState {
                copy(isRefreshing = true)
            }
            ProfileIntent.BackClick -> tryEmitEffect(ProfileEffect.NavigateBack)
        }
    }
}
```

`mvi(...)` creates the backing state and effect flows. In this pattern it is called when the object that delegates to it is created.

## Standalone Store

You can use `simple-mvi-core` without ViewModel or Compose. This is useful for shared stores such as app theme, session state, filters, or any state that is not owned by a single screen.

```kotlin
import io.github.v1rusdev.simplemvi.core.EffectUi
import io.github.v1rusdev.simplemvi.core.IntentUi
import io.github.v1rusdev.simplemvi.core.SimpleMVI
import io.github.v1rusdev.simplemvi.core.StateUi
import io.github.v1rusdev.simplemvi.core.mvi

data class ThemeState(
    val isDarkTheme: Boolean = false,
) : StateUi

sealed interface ThemeIntent : IntentUi {
    data object ToggleTheme : ThemeIntent
}

sealed interface ThemeEffect : EffectUi

class ThemeStore : SimpleMVI<ThemeState, ThemeIntent, ThemeEffect> by mvi(
    initialState = ThemeState(),
) {
    override fun onIntent(intent: ThemeIntent) {
        when (intent) {
            ThemeIntent.ToggleTheme -> updateState {
                copy(isDarkTheme = !isDarkTheme)
            }
        }
    }
}
```

The raw `mvi(...)` function creates a store, but its default `onIntent` does nothing. Wrap it in a class when you want intent handling:

```kotlin
val store = mvi<ThemeState, ThemeIntent, ThemeEffect>(
    initialState = ThemeState(),
)

store.updateState {
    copy(isDarkTheme = true)
}
```

## Koin Store Example

Because `SimpleMVI` is just an interface, you can create stores in DI and share them across screens.

```kotlin
import io.github.v1rusdev.simplemvi.core.SimpleMVI
import org.koin.core.qualifier.named
import org.koin.dsl.module

private const val ThemeStoreQualifier = "themeStore"

val appModule = module {
    single<SimpleMVI<ThemeState, ThemeIntent, ThemeEffect>>(named(ThemeStoreQualifier)) {
        ThemeStore()
    }
}
```

Then inject the same store from an app-level ViewModel and from a screen:

```kotlin
class MainViewModel(
    themeStore: SimpleMVI<ThemeState, ThemeIntent, ThemeEffect>,
) : ViewModel() {
    val themeState = themeStore.uiState
}
```

```kotlin
@Composable
fun ThemeRoute(
    themeStore: SimpleMVI<ThemeState, ThemeIntent, ThemeEffect>,
) {
    val state = themeStore.uiState.collectAsStateWithLifecycle()

    ThemeScreen(
        isDarkTheme = state.value.isDarkTheme,
        onToggleTheme = {
            themeStore.onIntent(ThemeIntent.ToggleTheme)
        },
    )
}
```

## Saved State

The Android module includes a small `SavedStateHandle.getOrPut` helper for route arguments and small saved values.

```kotlin
import androidx.lifecycle.SavedStateHandle
import io.github.v1rusdev.simplemvi.compose.android.getOrPut

val profileId = savedStateHandle.getOrPut("profile_id") {
    "me"
}
```

## Samples

The repository includes a Compose Multiplatform sample app:

```text
samples/compose-multiplatform-app
```

It demonstrates:

- A regular `androidx.lifecycle.ViewModel` screen with `StateFlow`.
- A `SimpleMVI` `MviViewModel` screen where Compose sends only `onIntent`.
- A named Koin singleton `SimpleMVI` store that controls the app theme.
- Jetpack Navigation Compose in shared Compose code.
- Android and iOS entry points.

## Design Goals

- Keep state immutable and easy to inspect.
- Route UI events through one `onIntent` entry point.
- Keep one-time effects separate from state.
- Support both screen-owned ViewModels and shared standalone stores.
- Stay lightweight enough for shared Kotlin code.
- Add Compose helpers without forcing UI dependencies into the core module.

## License

SimpleMVI is licensed under the [Apache License 2.0](LICENSE).
