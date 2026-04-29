package io.github.v1rusdev.simplemvi.core

import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Minimal MVI state container with observable state, one-time effects, and intent handling.
 *
 * Implement [onIntent] in your screen model and use [updateState] plus [emitEffect] or
 * [tryEmitEffect] to react to UI events.
 *
 * Example:
 * ```
 * override fun onIntent(intent: ProfileIntent) = handleIntent(intent) {
 *     when (intent) {
 *         ProfileIntent.RefreshClicked -> updateState { copy(isRefreshing = true) }
 *     }
 * }
 * ```
 */
interface SimpleMVI<State : StateUi, Intent : IntentUi, Effect : EffectUi> {
    /**
     * Owner used to derive the logger tag.
     *
     * Direct implementations can keep the default. Classes that delegate to `mvi(...)` can
     * override this property to expose their final runtime type to [handleIntent].
     */
    val loggerTagOwner: Any
        get() = this

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
     * Use this from coroutines when the caller can suspend.
     */
    suspend fun emitEffect(effect: Effect)

    /**
     * Tries to emit [effect] to [uiEffects] without suspension.
     *
     * Returns `true` when the effect was accepted by the underlying shared flow.
     */
    fun tryEmitEffect(effect: Effect): Boolean
}

/**
 * Logs [intent] through [SimpleMviConfig.logger] and then runs [block].
 *
 * Use this inside `onIntent(...)` when you want intent logging from the installed logger.
 */
inline fun <Intent : IntentUi> SimpleMVI<*, Intent, *>.handleIntent(
    intent: Intent,
    block: () -> Unit,
) {
    SimpleMviConfig.logger.logIntent(
        tag = loggerTagOwner.resolveSimpleMviLoggerTag(),
        intent = intent,
    )
    block()
}

/**
 * Creates a [SimpleMVI] store with a small default effect buffer.
 *
 * The default effect flow keeps one extra effect and drops the oldest item on overflow.
 *
 * Example:
 * ```
 * val store = mvi<ProfileState, ProfileIntent, ProfileEffect>(
 *     initialState = ProfileState.Loading,
 * )
 * ```
 *
 * You can also use it as a delegate inside your own class:
 * ```
 * class ProfileViewModel : ViewModel(),
 *     SimpleMVI<ProfileState, ProfileIntent, ProfileEffect> by mvi(
 *         initialState = ProfileState.Loading,
 *     )
 * ```
 */
fun <State : StateUi, Intent : IntentUi, Effect : EffectUi> mvi(
    initialState: State,
): SimpleMVI<State, Intent, Effect> = mvi(
    initialState = initialState,
    extraBufferCapacity = 1,
    onBufferOverflow = BufferOverflow.DROP_OLDEST,
)

/**
 * Creates a [SimpleMVI] store with custom effect buffering.
 *
 * Use this overload when one-time effects need a different [extraBufferCapacity] or
 * [onBufferOverflow] strategy.
 */
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
        while (true) {
            val currentState = mutableUiState.value
            val newState = currentState.transform()

            if (!mutableUiState.compareAndSet(currentState, newState)) {
                continue
            }

            if (currentState != newState) {
                SimpleMviConfig.logger.logStateChange(
                    tag = loggerTagOwner.resolveSimpleMviLoggerTag(),
                    oldState = currentState,
                    newState = newState,
                )
            }

            return newState
        }
    }

    override suspend fun emitEffect(effect: Effect) {
        mutableUiEffects.emit(effect)
    }

    override fun tryEmitEffect(effect: Effect): Boolean {
        return mutableUiEffects.tryEmit(effect)
    }
}
