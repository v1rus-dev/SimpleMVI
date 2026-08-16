package io.github.v1rusdev.simplemvi.samples.compose

import androidx.lifecycle.ViewModel
import io.github.v1rusdev.simplemvi.core.MviStore
import io.github.v1rusdev.simplemvi.samples.compose.theme.ThemeEffect
import io.github.v1rusdev.simplemvi.samples.compose.theme.ThemeIntent
import io.github.v1rusdev.simplemvi.samples.compose.theme.ThemeState
import kotlinx.coroutines.flow.StateFlow

class MainViewModel(
    themeStore: MviStore<ThemeState, ThemeIntent, ThemeEffect>,
) : ViewModel() {

    val themeState: StateFlow<ThemeState> = themeStore.uiState
}
