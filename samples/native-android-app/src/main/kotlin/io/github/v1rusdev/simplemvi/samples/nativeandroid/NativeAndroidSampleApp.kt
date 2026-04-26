package io.github.v1rusdev.simplemvi.samples.nativeandroid

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.v1rusdev.simplemvi.compose.android.CollectEffectsUiEvent
import io.github.v1rusdev.simplemvi.samples.nativeandroid.counter.NativeCounterEffect
import io.github.v1rusdev.simplemvi.samples.nativeandroid.counter.NativeCounterScreen
import io.github.v1rusdev.simplemvi.samples.nativeandroid.counter.NativeCounterViewModel

@Composable
fun NativeAndroidSampleApp(
    viewModel: NativeCounterViewModel = viewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState: SnackbarHostState = remember { SnackbarHostState() }

    CollectEffectsUiEvent(viewModel.uiEffects) { effect ->
        when (effect) {
            is NativeCounterEffect.Message -> snackbarHostState.showSnackbar(effect.text)
        }
    }

    MaterialTheme(
        colorScheme = lightColorScheme(),
    ) {
        NativeCounterScreen(
            state = state,
            snackbarHostState = snackbarHostState,
            onIntent = viewModel::onIntent,
        )
    }
}
