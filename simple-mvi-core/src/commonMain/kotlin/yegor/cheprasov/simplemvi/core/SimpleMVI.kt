package yegor.cheprasov.simplemvi.core

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet

interface SimpleMVI<State : StateUi, Intent : IntentUi, Effect : EffectUi> {
    val uiState: StateFlow<State>

    val uiEffects: Flow<Effect>

    fun onIntent(intent: Intent)

    fun updateState(transform: State.() -> State): State

    suspend fun emitEffect(effect: Effect)

    fun tryEmitEffect(effect: Effect): Boolean
}

fun <State : StateUi, Intent : IntentUi, Effect : EffectUi> mvi(
    initialState: State,
): SimpleMVI<State, Intent, Effect> = mvi(
    initialState = initialState,
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
)

fun <State : StateUi, Intent : IntentUi, Effect : EffectUi> mvi(
    initialState: State,
    extraBufferCapacity: Int,
    onBufferOverflow: BufferOverflow,
): SimpleMVI<State, Intent, Effect> = SimpleMVIDelegate(
    initialState = initialState,
    extraBufferCapacity = extraBufferCapacity,
    onBufferOverflow = onBufferOverflow,
)

private class SimpleMVIDelegate<State : StateUi, Intent : IntentUi, Effect : EffectUi>(
    initialState: State,
    extraBufferCapacity: Int,
    onBufferOverflow: BufferOverflow,
) : SimpleMVI<State, Intent, Effect> {

    private val mutableUiState = MutableStateFlow(initialState)
    override val uiState: StateFlow<State> = mutableUiState.asStateFlow()

    private val mutableUiEffects = MutableSharedFlow<Effect>(
        replay = 0,
        extraBufferCapacity = extraBufferCapacity,
        onBufferOverflow = onBufferOverflow,
    )

    override val uiEffects: Flow<Effect> = mutableUiEffects.asSharedFlow()

    override fun onIntent(intent: Intent) = Unit

    override fun updateState(transform: State.() -> State): State {
        return mutableUiState.updateAndGet { currentState ->
            currentState.transform()
        }
    }

    override suspend fun emitEffect(effect: Effect) {
        mutableUiEffects.emit(effect)
    }

    override fun tryEmitEffect(effect: Effect): Boolean {
        return mutableUiEffects.tryEmit(effect)
    }
}
