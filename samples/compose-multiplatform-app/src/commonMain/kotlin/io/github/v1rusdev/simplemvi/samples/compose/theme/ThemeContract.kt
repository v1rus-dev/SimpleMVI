package io.github.v1rusdev.simplemvi.samples.compose.theme

import io.github.v1rusdev.simplemvi.core.EffectUi
import io.github.v1rusdev.simplemvi.core.IntentUi
import io.github.v1rusdev.simplemvi.core.BaseMviStore
import io.github.v1rusdev.simplemvi.core.StateUi

data class ThemeState(val isDarkTheme: Boolean) : StateUi

sealed interface ThemeIntent : IntentUi {
    data object ChangeDarkTheme : ThemeIntent
}

sealed interface ThemeEffect : EffectUi

class ThemeStore : BaseMviStore<ThemeState, ThemeIntent, ThemeEffect>(
    initialState = ThemeState(isDarkTheme = false),
) {
    override fun handleIntent(intent: ThemeIntent) {
        when (intent) {
            ThemeIntent.ChangeDarkTheme -> updateState {
                copy(isDarkTheme = !isDarkTheme)
            }
        }
    }
}
