package io.github.v1rusdev.simplemvi.samples.compose

import androidx.lifecycle.ViewModel
import io.github.v1rusdev.simplemvi.core.SimpleMVI
import io.github.v1rusdev.simplemvi.samples.compose.theme.ThemeEffectUi
import io.github.v1rusdev.simplemvi.samples.compose.theme.ThemeIntentUi
import io.github.v1rusdev.simplemvi.samples.compose.theme.ThemeStateUi
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(
    themeStore: SimpleMVI<ThemeStateUi, ThemeIntentUi, ThemeEffectUi>,
) : ViewModel() {

    val themeState: StateFlow<ThemeStateUi> = themeStore.uiState
}
