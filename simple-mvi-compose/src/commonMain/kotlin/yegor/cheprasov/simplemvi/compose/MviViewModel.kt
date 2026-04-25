package yegor.cheprasov.simplemvi.compose

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.BufferOverflow
import yegor.cheprasov.simplemvi.core.EffectUi
import yegor.cheprasov.simplemvi.core.IntentUi
import yegor.cheprasov.simplemvi.core.SimpleMVI
import yegor.cheprasov.simplemvi.core.StateUi
import yegor.cheprasov.simplemvi.core.mvi

abstract class MviViewModel<State : StateUi, Intent : IntentUi, Effect : EffectUi>(
    initialState: State,
    extraBufferCapacity: Int,
    onBufferOverflow: BufferOverflow,
) : ViewModel(),
    SimpleMVI<State, Intent, Effect> by mvi(
        initialState = initialState,
        extraBufferCapacity = extraBufferCapacity,
        onBufferOverflow = onBufferOverflow,
    ) {

    constructor(initialState: State) : this(
        initialState = initialState,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    protected fun sendEffect(effect: Effect): Boolean {
        return tryEmitEffect(effect)
    }
}