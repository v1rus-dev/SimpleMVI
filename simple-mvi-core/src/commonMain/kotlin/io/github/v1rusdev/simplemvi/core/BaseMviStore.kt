package io.github.v1rusdev.simplemvi.core

import kotlinx.coroutines.channels.BufferOverflow

/**
 * Base SimpleMVI store with guaranteed global intent observability.
 *
 * Extend this class when a non-Compose store owns its intent handling. Implement [handleIntent]
 * and route every UI action through [onIntent].
 */
abstract class BaseMviStore<State : StateUi, Intent : IntentUi, Effect : EffectUi> private constructor(
    private val store: MviStore<State, Intent, Effect>,
) : MviStore<State, Intent, Effect> by store {

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
     * Creates a store with the default effect buffer.
     *
     * The default buffer keeps one extra effect and drops the oldest item on overflow.
     */
    constructor(initialState: State) : this(
        initialState = initialState,
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    final override fun onIntent(intent: Intent) {
        store.onIntent(intent)
        handleIntent(intent)
    }

    /**
     * Handles an intent after global SimpleMVI observability hooks have been notified.
     */
    protected abstract fun handleIntent(intent: Intent)
}
