package io.github.v1rusdev.simplemvi.core

import kotlin.concurrent.Volatile

/**
 * Global observability hooks for every SimpleMVI store in the process.
 *
 * Configure it once during application startup to attach logging, analytics, or debug hooks.
 * The hooks are process-wide: there is no per-store override.
 */
object MviConfig {

    @Volatile
    private var current = Hooks()

    /**
     * Merges new observability hooks into the current configuration.
     *
     * Only the hooks you set inside [block] are replaced; the others keep their previous value.
     * Use [reset] to drop every hook at once.
     *
     * Example:
     * ```
     * MviConfig.configure {
     *     onIntent { intent -> println("intent: $intent") }
     * }
     * ```
     */
    fun configure(block: Builder.() -> Unit) {
        val snapshot = current
        val builder = Builder(
            onIntentCallback = snapshot.onIntent,
            onEffectCallback = snapshot.onEffect,
            onErrorCallback = snapshot.onError,
        )
        builder.block()
        current = Hooks(
            onIntent = builder.onIntentCallback,
            onEffect = builder.onEffectCallback,
            onError = builder.onErrorCallback,
        )
    }

    /**
     * Restores the default no-op configuration.
     *
     * This is useful for tests or for applications that reinitialize their runtime.
     */
    fun reset() {
        current = Hooks()
    }

    internal fun notifyIntent(intent: IntentUi) {
        val hooks = current
        try {
            hooks.onIntent(intent)
        } catch (error: Throwable) {
            notifyError(hooks, error)
        }
    }

    internal fun notifyEffect(effect: EffectUi) {
        val hooks = current
        try {
            hooks.onEffect(effect)
        } catch (error: Throwable) {
            notifyError(hooks, error)
        }
    }

    private fun notifyError(hooks: Hooks, error: Throwable) {
        try {
            hooks.onError(error)
        } catch (_: Throwable) {
            // An observability hook must never break the store it observes.
        }
    }

    /**
     * DSL receiver of [configure].
     *
     * Every callback starts from the currently configured hook, so setting one hook leaves the
     * others untouched.
     */
    class Builder internal constructor(
        internal var onIntentCallback: (IntentUi) -> Unit,
        internal var onEffectCallback: (EffectUi) -> Unit,
        internal var onErrorCallback: (Throwable) -> Unit,
    ) {
        /**
         * Observes every intent passed to a store, before the store handles it.
         */
        fun onIntent(block: (IntentUi) -> Unit) {
            onIntentCallback = block
        }

        /**
         * Observes every effect the effect flow accepted.
         *
         * Effects rejected by the effect buffer never reach this hook. Acceptance is not the same
         * as delivery: with no active collector the effect flow still accepts the effect and then
         * discards it, and this hook is notified.
         */
        fun onEffect(block: (EffectUi) -> Unit) {
            onEffectCallback = block
        }

        /**
         * Receives any [Throwable] raised by [onIntent] or [onEffect].
         *
         * Hooks are observability only, so an exception thrown by one is caught and routed here
         * instead of breaking the intent or effect path. An exception thrown by this hook itself
         * is ignored.
         */
        fun onError(block: (Throwable) -> Unit) {
            onErrorCallback = block
        }
    }

    private class Hooks(
        val onIntent: (IntentUi) -> Unit = {},
        val onEffect: (EffectUi) -> Unit = {},
        val onError: (Throwable) -> Unit = {},
    )
}
