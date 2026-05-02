package io.github.v1rusdev.simplemvi.compose

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.BufferOverflow
import io.github.v1rusdev.simplemvi.core.EffectUi
import io.github.v1rusdev.simplemvi.core.IntentUi
import io.github.v1rusdev.simplemvi.core.SimpleMVI
import io.github.v1rusdev.simplemvi.core.StateUi
import io.github.v1rusdev.simplemvi.core.mvi

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
    private val simpleMvi: SimpleMVI<State, Intent, Effect>,
) : ViewModel(),
    SimpleMVI<State, Intent, Effect> by simpleMvi {

    constructor(
        initialState: State,
        extraBufferCapacity: Int,
        onBufferOverflow: BufferOverflow,
    ) : this(
        simpleMvi = mvi(
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
     * Returns `true` when the effect was accepted by the effect flow.
     */
    protected fun sendEffect(effect: Effect): Boolean {
        return tryEmitEffect(effect)
    }

    final override fun onIntent(intent: Intent) {
        simpleMvi.onIntent(intent)
        handleIntent(intent)
    }

    /**
     * Handles an intent after global SimpleMVI observability hooks have been notified.
     */
    protected abstract fun handleIntent(intent: Intent)
}
