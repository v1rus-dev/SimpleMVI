package io.github.v1rusdev.simplemvi.compose.android

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import io.github.v1rusdev.simplemvi.core.EffectUi

/**
 * Collects one-time UI effects in a lifecycle-aware Android Compose scope.
 *
 * Collection starts when [lifecycleOwner] reaches [minActiveState] and stops when it moves below it.
 *
 * Example:
 * ```
 * CollectEffectsUiEvent(viewModel.uiEffects) { effect ->
 *     when (effect) {
 *         is ProfileEffect.OpenDetails -> navController.navigate("details")
 *     }
 * }
 * ```
 */
@Composable
fun <Effect : EffectUi> CollectEffectsUiEvent(
    effectsFlow: Flow<Effect>,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    onEffect: suspend CoroutineScope.(effect: Effect) -> Unit
) {
    LaunchedEffect(effectsFlow, lifecycleOwner, minActiveState) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(minActiveState) {
            effectsFlow.collect { effect -> onEffect(effect) }
        }
    }
}
