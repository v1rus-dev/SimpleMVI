package yegor.cheprasov.simplemvi.compose

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.channels.BufferOverflow
import yegor.cheprasov.simplemvi.core.EffectUi
import yegor.cheprasov.simplemvi.core.IntentUi
import yegor.cheprasov.simplemvi.core.SimpleMVI
import yegor.cheprasov.simplemvi.core.StateUi
import yegor.cheprasov.simplemvi.core.mvi

/**
 * Base [ViewModel] that delegates SimpleMVI state and effect handling.
 *
 * Extend this class for Compose screens, override `onIntent`, and expose [uiState] plus
 * [uiEffects] directly to the UI.
 *
 * Example:
 * ```
 * class ProfileViewModel : MviViewModel<ProfileState, ProfileIntent, ProfileEffect>(
 *     initialState = ProfileState.Loading,
 * ) {
 *     override fun onIntent(intent: ProfileIntent) = Unit
 * }
 * ```
 */
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
}
