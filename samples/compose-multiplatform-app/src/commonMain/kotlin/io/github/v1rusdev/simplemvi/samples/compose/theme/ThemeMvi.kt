package io.github.v1rusdev.simplemvi.samples.compose.theme

import io.github.v1rusdev.simplemvi.core.EffectUi
import io.github.v1rusdev.simplemvi.core.IntentUi
import io.github.v1rusdev.simplemvi.core.SimpleMVI
import io.github.v1rusdev.simplemvi.core.StateUi
import io.github.v1rusdev.simplemvi.core.mvi

data class ThemeStateUi(val isDarkTheme: Boolean) : StateUi

sealed interface ThemeIntentUi : IntentUi {
    data object ChangeDarkTheme : ThemeIntentUi
}

sealed interface ThemeEffectUi : EffectUi

class ThemeStore : SimpleMVI<ThemeStateUi, ThemeIntentUi, ThemeEffectUi> by mvi(
    initialState = ThemeStateUi(isDarkTheme = false),
) {
    override fun onIntent(intent: ThemeIntentUi) {
        when (intent) {
            ThemeIntentUi.ChangeDarkTheme -> updateState {
                copy(isDarkTheme = !isDarkTheme)
            }
        }
    }
}
