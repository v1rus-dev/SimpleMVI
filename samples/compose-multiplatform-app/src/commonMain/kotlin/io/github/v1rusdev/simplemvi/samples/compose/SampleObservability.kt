package io.github.v1rusdev.simplemvi.samples.compose

import io.github.aakira.napier.Napier
import io.github.v1rusdev.simplemvi.core.MviConfig

/**
 * Wires the global SimpleMVI observability hooks to Napier.
 *
 * The hooks are process-wide, so this belongs to the platform entry point and runs once during
 * startup, before the first store is created.
 */
fun initMviObservability() {
    MviConfig.configure {
        onIntent { intent ->
            Napier.v("Intent: $intent")
        }
        onEffect { effect ->
            Napier.v("Effect: $effect")
        }
        onError { error ->
            Napier.e("SimpleMVI observability hook failed", error)
        }
    }
}
