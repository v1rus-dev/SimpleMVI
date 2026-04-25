package yegor.cheprasov.simplemvi.compose.android

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import yegor.cheprasov.simplemvi.core.EffectUi

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