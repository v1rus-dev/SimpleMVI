package io.github.v1rusdev.simplemvi.compose

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
 * Collects one-time UI effects in a lifecycle-aware Compose scope.
 *
 * Collection starts when [lifecycleOwner] reaches [minActiveState] and stops when it moves below
 * it. The effect flow does not replay, so effects emitted while collection is stopped are
 * **silently dropped**. Send effects only in response to a user intent, and keep durable data in
 * the state flow instead.
 *
 * Example:
 * ```
 * CollectUiEffects(viewModel.uiEffects) { effect ->
 *     when (effect) {
 *         is ProfileEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.text)
 *     }
 * }
 * ```
 *
 * @param effectsFlow the effect flow to collect, usually `viewModel.uiEffects`.
 * @param lifecycleOwner the owner whose lifecycle gates collection.
 * @param minActiveState the lowest lifecycle state at which effects are collected.
 * @param onEffect invoked for every collected effect, in the collecting coroutine.
 */
@Composable
fun <Effect : EffectUi> CollectUiEffects(
    effectsFlow: Flow<Effect>,
    lifecycleOwner: LifecycleOwner = LocalLifecycleOwner.current,
    minActiveState: Lifecycle.State = Lifecycle.State.STARTED,
    onEffect: suspend CoroutineScope.(effect: Effect) -> Unit
) {
    LaunchedEffect(effectsFlow, lifecycleOwner, minActiveState) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(minActiveState) {
            effectsFlow.collect { effect ->
                onEffect(effect)
            }
        }
    }
}
