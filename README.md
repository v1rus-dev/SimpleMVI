# SimpleMVI

[![CI](https://github.com/v1rus-dev/SimpleMVI/actions/workflows/gradle.yml/badge.svg)](https://github.com/v1rus-dev/SimpleMVI/actions/workflows/gradle.yml)
[![Maven Central](https://img.shields.io/maven-central/v/io.github.v1rus-dev/simple-mvi-core?label=Maven%20Central)](https://central.sonatype.com/search?q=g%3Aio.github.v1rus-dev)
[![License](https://img.shields.io/badge/license-Apache--2.0-blue.svg)](LICENSE)
[![Kotlin](https://img.shields.io/badge/kotlin-2.3.21-7F52FF.svg?logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/compose-1.10.3-4285F4.svg)](https://www.jetbrains.com/compose-multiplatform/)

![Android](https://img.shields.io/badge/android-3DDC84?style=flat&logo=android&logoColor=white)
![iOS](https://img.shields.io/badge/ios-000000?style=flat&logo=apple&logoColor=white)

SimpleMVI is a small Kotlin Multiplatform library for building explicit MVI-style state containers.

It gives you a tiny set of contracts for **state**, **intent**, and **effect**, plus Compose helpers for lifecycle-aware effect collection and ViewModel integration.

## Quickstart

- Latest project version: `0.1.0`
- Group ID: `io.github.v1rus-dev`
- Core module: `simple-mvi-core`
- Compose Multiplatform module: `simple-mvi-compose`
- Android helpers module: `simple-mvi-android`

<details open>
<summary>Version catalog</summary>

```toml
[versions]
simplemvi = "0.1.0"

[libraries]
simplemvi-core = { module = "io.github.v1rus-dev:simple-mvi-core", version.ref = "simplemvi" }
simplemvi-compose = { module = "io.github.v1rus-dev:simple-mvi-compose", version.ref = "simplemvi" }
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
    implementation("io.github.v1rus-dev:simple-mvi-core:0.1.0")

    // Compose Multiplatform helpers
    implementation("io.github.v1rus-dev:simple-mvi-compose:0.1.0")

    // Android-only helpers, including SavedStateHandle support
    implementation("io.github.v1rus-dev:simple-mvi-android:0.1.0")
}
```

</details>

## Modules

| Module | Target | Use it for |
| --- | --- | --- |
| `simple-mvi-core` | Android, iOS | MVI contracts, state holder, and effect flow. |
| `simple-mvi-compose` | Android, iOS | Compose Multiplatform `MviViewModel` and effect collection. |
| `simple-mvi-android` | Android | Android lifecycle helpers and `SavedStateHandle` extension. |

## Show Me The Code

### 1. Define a contract

```kotlin
import yegor.cheprasov.simplemvi.core.EffectUi
import yegor.cheprasov.simplemvi.core.IntentUi
import yegor.cheprasov.simplemvi.core.StateUi

sealed interface ProfileState : StateUi {
    data object Loading : ProfileState
    data class Content(
        val name: String,
        val isRefreshing: Boolean = false,
    ) : ProfileState
}

sealed interface ProfileIntent : IntentUi {
    data object RefreshClicked : ProfileIntent
    data object BackClicked : ProfileIntent
}

sealed interface ProfileEffect : EffectUi {
    data object NavigateBack : ProfileEffect
    data class ShowMessage(val text: String) : ProfileEffect
}
```

### 2. Create a ViewModel

```kotlin
import yegor.cheprasov.simplemvi.compose.MviViewModel

class ProfileViewModel : MviViewModel<ProfileState, ProfileIntent, ProfileEffect>(
    initialState = ProfileState.Loading,
) {
    override fun onIntent(intent: ProfileIntent) {
        when (intent) {
            ProfileIntent.RefreshClicked -> refresh()
            ProfileIntent.BackClicked -> sendEffect(ProfileEffect.NavigateBack)
        }
    }

    private fun refresh() {
        updateState {
            when (this) {
                ProfileState.Loading -> this
                is ProfileState.Content -> copy(isRefreshing = true)
            }
        }

        sendEffect(ProfileEffect.ShowMessage("Profile refresh started"))
    }
}
```

### 3. Collect state and effects in Compose

```kotlin
import androidx.compose.runtime.Composable
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import yegor.cheprasov.simplemvi.compose.CollectEffectsUiEvent

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onBack: () -> Unit,
) {
    val state = viewModel.uiState.collectAsStateWithLifecycle()

    CollectEffectsUiEvent(viewModel.uiEffects) { effect ->
        when (effect) {
            ProfileEffect.NavigateBack -> onBack()
            is ProfileEffect.ShowMessage -> {
                // Show a snackbar, toast, or dialog here.
            }
        }
    }

    ProfileContent(
        state = state.value,
        onRefreshClick = {
            viewModel.onIntent(ProfileIntent.RefreshClicked)
        },
    )
}
```

## Standalone Store

You can also use the core module without a ViewModel.

```kotlin
import yegor.cheprasov.simplemvi.core.mvi

val store = mvi<ProfileState, ProfileIntent, ProfileEffect>(
    initialState = ProfileState.Loading,
)

store.updateState {
    ProfileState.Content(name = "Yegor")
}
```

## Saved State

The Android module includes a small `SavedStateHandle.getOrPut` helper for route arguments and small saved values.

```kotlin
import androidx.lifecycle.SavedStateHandle
import yegor.cheprasov.simplemvi.compose.android.getOrPut

val profileId = savedStateHandle.getOrPut("profile_id") {
    "me"
}
```

## Design Goals

- Keep state immutable and easy to inspect.
- Route UI events through one `onIntent` entry point.
- Keep one-time effects separate from state.
- Stay lightweight enough for shared Kotlin code.
- Add Compose helpers without forcing UI dependencies into the core module.

## License

SimpleMVI is licensed under the [Apache License 2.0](LICENSE).
