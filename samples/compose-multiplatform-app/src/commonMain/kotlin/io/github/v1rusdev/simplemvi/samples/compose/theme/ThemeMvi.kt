package io.github.v1rusdev.simplemvi.samples.compose.theme

import io.github.v1rusdev.simplemvi.core.EffectUi
import io.github.v1rusdev.simplemvi.core.IntentUi
import io.github.v1rusdev.simplemvi.core.SimpleMviStore
import io.github.v1rusdev.simplemvi.core.StateUi

data class ThemeStateUi(val isDarkTheme: Boolean) : StateUi

sealed interface ThemeIntentUi : IntentUi {
    data object ChangeDarkTheme : ThemeIntentUi
}

sealed interface ThemeEffectUi : EffectUi

class ThemeStore : SimpleMviStore<ThemeStateUi, ThemeIntentUi, ThemeEffectUi>(
    initialState = ThemeStateUi(isDarkTheme = false),
) {
    override fun handleIntent(intent: ThemeIntentUi) {
        when (intent) {
            ThemeIntentUi.ChangeDarkTheme -> updateState {
                copy(isDarkTheme = !isDarkTheme)
            }
        }
    }
}
