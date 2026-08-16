package io.github.v1rusdev.simplemvi.compose

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.BufferOverflow
import io.github.v1rusdev.simplemvi.core.EffectUi
import io.github.v1rusdev.simplemvi.core.IntentUi
import io.github.v1rusdev.simplemvi.core.MviStore
import io.github.v1rusdev.simplemvi.core.StateUi
import io.github.v1rusdev.simplemvi.core.createStore

/**
 * Base [ViewModel] that delegates SimpleMVI state and effect handling.
 *
 * Extend this class for Compose screens, override `handleIntent`, and expose [uiState] plus
 * [uiEffects] directly to the UI.
 *
 * Example:
 * ```
 * class ProfileViewModel : MviViewModel<ProfileState, ProfileIntent, ProfileEffect>(
 *     initialState = ProfileState.Loading,
 * ) {
 *     override fun handleIntent(intent: ProfileIntent) = Unit
 * }
 * ```
 */
abstract class MviViewModel<State : StateUi, Intent : IntentUi, Effect : EffectUi> private constructor(
    private val store: MviStore<State, Intent, Effect>,
) : ViewModel(),
    MviStore<State, Intent, Effect> by store {

    constructor(
        initialState: State,
        extraBufferCapacity: Int,
        onBufferOverflow: BufferOverflow,
    ) : this(
        store = createStore(
            initialState = initialState,
            extraBufferCapacity = extraBufferCapacity,
            onBufferOverflow = onBufferOverflow,
        ),
    )

    /**
     * Creates a ViewModel with the default effect buffer.
     *
     * The default buffer keeps one extra effect and drops the oldest item on overflow.
     */
    constructor(initialState: State) : this(
        initialState = initialState,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    /**
     * Emits a one-time UI [effect] from a ViewModel without suspension.
     *
     * Returns `true` when the effect was accepted by the effect flow. A `false` result means the
     * effect was dropped and never reached the UI.
     */
    protected fun sendEffect(effect: Effect): Boolean {
        return tryEmitEffect(effect)
    }

    final override fun onIntent(intent: Intent) {
        store.onIntent(intent)
        handleIntent(intent)
    }

    /**
     * Handles an intent after global SimpleMVI observability hooks have been notified.
     */
    protected abstract fun handleIntent(intent: Intent)
}
