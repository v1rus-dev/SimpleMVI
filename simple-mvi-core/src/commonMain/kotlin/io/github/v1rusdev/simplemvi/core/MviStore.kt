package io.github.v1rusdev.simplemvi.core

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.updateAndGet

/**
 * Minimal MVI state container with observable state, one-time effects, and intent handling.
 *
 * Implement [onIntent] in your screen model and use [updateState] plus [emitEffect] or
 * [tryEmitEffect] to react to UI events. Prefer [BaseMviStore] when you want a core store
 * that guarantees global intent observability before intent handling.
 *
 * Example:
 * ```
 * override fun onIntent(intent: ProfileIntent) {
 *     when (intent) {
 *         ProfileIntent.RefreshClicked -> updateState { copy(isRefreshing = true) }
 *     }
 * }
 * ```
 */
interface MviStore<State : StateUi, Intent : IntentUi, Effect : EffectUi> {
    /**
     * Observable UI state for the screen.
     *
     * Collect this flow from UI code and render every emitted [State].
     */
    val uiState: StateFlow<State>

    /**
     * One-time UI effects such as navigation or messages.
     *
     * Collect this flow separately from [uiState] so effects are handled once.
     */
    val uiEffects: Flow<Effect>

    /**
     * Handles a UI intent.
     *
     * Override this function in your implementation and route every user action through it.
     */
    fun onIntent(intent: Intent)

    /**
     * Atomically updates [uiState] and returns the new state.
     *
     * Example:
     * ```
     * updateState { copy(isLoading = true) }
     * ```
     */
    fun updateState(transform: State.() -> State): State

    /**
     * Suspends until [effect] is emitted to [uiEffects].
     *
     * Use this from coroutines when the caller can suspend and the effect must not be dropped.
     * With a suspending [BufferOverflow] strategy this waits for buffer space instead of
     * discarding the effect, which is what [tryEmitEffect] would do.
     */
    suspend fun emitEffect(effect: Effect)

    /**
     * Tries to emit [effect] to [uiEffects] without suspension.
     *
     * Returns `true` when the effect was accepted by the underlying shared flow. A `false` result
     * means the effect was dropped and never reached the UI; global observability hooks are not
     * notified in that case. Prefer [emitEffect] when losing the effect is not acceptable.
     */
    fun tryEmitEffect(effect: Effect): Boolean
}

/**
 * Creates an [MviStore] with a small default effect buffer.
 *
 * The default effect flow keeps one extra effect and drops the oldest item on overflow.
 *
 * Example:
 * ```
 * val store = createStore<ProfileState, ProfileIntent, ProfileEffect>(
 *     initialState = ProfileState.Loading,
 * )
 * ```
 *
 * You can also use it as a delegate inside your own class:
 * ```
 * class ProfileViewModel : ViewModel(),
 *     MviStore<ProfileState, ProfileIntent, ProfileEffect> by createStore(
 *         initialState = ProfileState.Loading,
 *     )
 * ```
 *
 * The returned store does not handle intents on its own: its [MviStore.onIntent] only notifies
 * [MviConfig]. Override [MviStore.onIntent] in the delegating class, or extend [BaseMviStore],
 * to add intent handling on top of that notification.
 */
fun <State : StateUi, Intent : IntentUi, Effect : EffectUi> createStore(
    initialState: State,
): MviStore<State, Intent, Effect> = createStore(
    initialState = initialState,
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
)

/**
 * Creates an [MviStore] with custom effect buffering.
 *
 * Use this overload when one-time effects need a different [extraBufferCapacity] or
 * [onBufferOverflow] strategy than the default one extra slot with
 * [BufferOverflow.DROP_OLDEST].
 */
fun <State : StateUi, Intent : IntentUi, Effect : EffectUi> createStore(
    initialState: State,
    extraBufferCapacity: Int,
    onBufferOverflow: BufferOverflow,
): MviStore<State, Intent, Effect> = MviStoreImpl(
    initialState = initialState,
    extraBufferCapacity = extraBufferCapacity,
    onBufferOverflow = onBufferOverflow,
)

private class MviStoreImpl<State : StateUi, Intent : IntentUi, Effect : EffectUi>(
    initialState: State,
    extraBufferCapacity: Int,
    onBufferOverflow: BufferOverflow,
) : MviStore<State, Intent, Effect> {

    private val mutableUiState = MutableStateFlow(initialState)
    override val uiState: StateFlow<State> = mutableUiState.asStateFlow()

    private val mutableUiEffects = MutableSharedFlow<Effect>(
        replay = 0,
        extraBufferCapacity = extraBufferCapacity,
        onBufferOverflow = onBufferOverflow,
    )

    override val uiEffects: Flow<Effect> = mutableUiEffects.asSharedFlow()

    override fun onIntent(intent: Intent) {
        MviConfig.notifyIntent(intent)
    }

    override fun updateState(transform: State.() -> State): State {
        return mutableUiState.updateAndGet { currentState ->
            currentState.transform()
        }
    }

    override suspend fun emitEffect(effect: Effect) {
        mutableUiEffects.emit(effect)
        MviConfig.notifyEffect(effect)
    }

    override fun tryEmitEffect(effect: Effect): Boolean {
        val accepted = mutableUiEffects.tryEmit(effect)
        if (accepted) {
            MviConfig.notifyEffect(effect)
        }
        return accepted
    }
}
