package io.github.v1rusdev.simplemvi.core

object SimpleMviConfig {
    private var config = Config()

    /**
     * Replaces the global SimpleMVI observability configuration.
     *
     * Call this once during application startup to attach logging, analytics, or debug hooks.
     */
    fun configure(block: Builder.() -> Unit) {
        val builder = Builder().apply(block)
        config = Config(
            onIntent = builder.onIntentCallback,
            onEffect = builder.onEffectCallback,
        )
    }

    /**
     * Restores the default no-op configuration.
     *
     * This is useful for tests or for applications that reinitialize their runtime.
     */
    fun reset() {
        config = Config()
    }

    internal fun notifyIntent(intent: IntentUi) {
        config.onIntent(intent)
    }

    internal fun notifyEffect(effect: EffectUi) {
        config.onEffect(effect)
    }

    class Builder internal constructor() {
        internal var onIntentCallback: (IntentUi) -> Unit = {}
            private set
        internal var onEffectCallback: (EffectUi) -> Unit = {}
            private set

        fun onIntent(block: (IntentUi) -> Unit) {
            onIntentCallback = block
        }

        fun onEffect(block: (EffectUi) -> Unit) {
            onEffectCallback = block
        }
    }

    private data class Config(
        val onIntent: (IntentUi) -> Unit = {},
        val onEffect: (EffectUi) -> Unit = {},
    )
}
