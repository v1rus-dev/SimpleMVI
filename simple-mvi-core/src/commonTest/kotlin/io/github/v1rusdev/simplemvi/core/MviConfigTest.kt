package io.github.v1rusdev.simplemvi.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class MviConfigTest {

    @Test
    fun configure_notifies_intent_before_store_handles_it() {
        val events = mutableListOf<String>()
        val store = TestStore(events)

        try {
            MviConfig.configure {
                onIntent { intent ->
                    events += "hook:$intent"
                }
            }

            store.onIntent(TestIntent.Increment)

            assertEquals(
                listOf(
                    "hook:Increment",
                    "handle:Increment",
                ),
                events,
            )
            assertEquals(TestState(value = 1), store.uiState.value)
        } finally {
            MviConfig.reset()
        }
    }

    @Test
    fun configure_replaces_only_the_hooks_it_sets() {
        val events = mutableListOf<String>()
        val store = TestStore(events)

        try {
            MviConfig.configure {
                onIntent { events += "first" }
            }
            MviConfig.configure {
                onIntent { events += "second" }
            }

            store.onIntent(TestIntent.Increment)

            assertEquals(
                listOf(
                    "second",
                    "handle:Increment",
                ),
                events,
            )
        } finally {
            MviConfig.reset()
        }
    }

    @Test
    fun configuring_one_hook_keeps_the_other_hook() = runTest {
        val events = mutableListOf<String>()
        val store = TestStore(events)
        val receivedEffect = async(start = CoroutineStart.UNDISPATCHED) {
            store.uiEffects.first()
        }

        try {
            MviConfig.configure {
                onIntent { events += "intent-hook" }
            }
            MviConfig.configure {
                onEffect { events += "effect-hook" }
            }

            store.onIntent(TestIntent.Increment)
            store.tryEmitEffect(TestEffect.ShowMessage("Saved"))

            assertEquals(
                listOf(
                    "intent-hook",
                    "handle:Increment",
                    "effect-hook",
                ),
                events,
            )
            assertEquals(TestEffect.ShowMessage("Saved"), receivedEffect.await())
        } finally {
            MviConfig.reset()
        }
    }

    @Test
    fun reset_restores_no_op_callbacks() {
        val events = mutableListOf<String>()
        val store = TestStore(events)

        try {
            MviConfig.configure {
                onIntent { events += "hook" }
            }
            MviConfig.reset()

            store.onIntent(TestIntent.Increment)

            assertEquals(
                listOf("handle:Increment"),
                events,
            )
        } finally {
            MviConfig.reset()
        }
    }

    @Test
    fun try_emit_effect_notifies_effect_hook_and_delivers_effect() = runTest {
        val events = mutableListOf<String>()
        val store = TestStore(events)
        val receivedEffect = async(start = CoroutineStart.UNDISPATCHED) {
            store.uiEffects.first()
        }

        try {
            MviConfig.configure {
                onEffect { effect ->
                    events += "hook:$effect"
                }
            }

            store.tryEmitEffect(TestEffect.ShowMessage("Saved"))

            assertEquals(
                listOf("hook:ShowMessage(text=Saved)"),
                events,
            )
            assertEquals(TestEffect.ShowMessage("Saved"), receivedEffect.await())
        } finally {
            MviConfig.reset()
        }
    }

    @Test
    fun emit_effect_notifies_effect_hook_and_delivers_effect() = runTest {
        val events = mutableListOf<String>()
        val store = TestStore(events)
        val receivedEffect = async(start = CoroutineStart.UNDISPATCHED) {
            store.uiEffects.first()
        }

        try {
            MviConfig.configure {
                onEffect { effect ->
                    events += "hook:$effect"
                }
            }

            store.emitEffect(TestEffect.ShowMessage("Saved"))

            assertEquals(
                listOf("hook:ShowMessage(text=Saved)"),
                events,
            )
            assertEquals(TestEffect.ShowMessage("Saved"), receivedEffect.await())
        } finally {
            MviConfig.reset()
        }
    }

    @Test
    fun effect_hook_is_not_notified_when_the_effect_is_rejected() = runTest {
        val events = mutableListOf<String>()
        val store = createStore<TestState, TestIntent, TestEffect>(
            initialState = TestState(value = 0),
            extraBufferCapacity = 0,
            onBufferOverflow = BufferOverflow.SUSPEND,
        )
        val subscription = launch(start = CoroutineStart.UNDISPATCHED) {
            store.uiEffects.collect { }
        }

        try {
            MviConfig.configure {
                onEffect { effect ->
                    events += "hook:$effect"
                }
            }

            assertFalse(store.tryEmitEffect(TestEffect.ShowMessage("Dropped")))

            assertEquals(emptyList<String>(), events)
        } finally {
            MviConfig.reset()
            subscription.cancel()
        }
    }

    @Test
    fun raw_store_notifies_the_intent_hook() {
        val events = mutableListOf<String>()
        val store = createStore<TestState, TestIntent, TestEffect>(
            initialState = TestState(value = 0),
        )

        try {
            MviConfig.configure {
                onIntent { intent ->
                    events += "hook:$intent"
                }
            }

            store.onIntent(TestIntent.Increment)

            assertEquals(listOf("hook:Increment"), events)
        } finally {
            MviConfig.reset()
        }
    }

    @Test
    fun a_throwing_intent_hook_does_not_break_intent_handling() {
        val events = mutableListOf<String>()
        val store = TestStore(events)
        val failure = IllegalStateException("hook exploded")
        val errors = mutableListOf<Throwable>()

        try {
            MviConfig.configure {
                onIntent { throw failure }
                onError { error -> errors += error }
            }

            store.onIntent(TestIntent.Increment)

            assertEquals(listOf("handle:Increment"), events)
            assertEquals(TestState(value = 1), store.uiState.value)
            assertEquals(1, errors.size)
            assertSame(failure, errors.single())
        } finally {
            MviConfig.reset()
        }
    }

    @Test
    fun a_throwing_effect_hook_does_not_break_effect_delivery() = runTest {
        val events = mutableListOf<String>()
        val store = TestStore(events)
        val failure = IllegalStateException("hook exploded")
        val errors = mutableListOf<Throwable>()
        val receivedEffect = async(start = CoroutineStart.UNDISPATCHED) {
            store.uiEffects.first()
        }

        try {
            MviConfig.configure {
                onEffect { throw failure }
                onError { error -> errors += error }
            }

            assertTrue(store.tryEmitEffect(TestEffect.ShowMessage("Saved")))

            assertEquals(TestEffect.ShowMessage("Saved"), receivedEffect.await())
            assertSame(failure, errors.single())
        } finally {
            MviConfig.reset()
        }
    }

    @Test
    fun a_throwing_error_hook_is_ignored() {
        val events = mutableListOf<String>()
        val store = TestStore(events)

        try {
            MviConfig.configure {
                onIntent { throw IllegalStateException("hook exploded") }
                onError { throw IllegalStateException("error hook exploded too") }
            }

            store.onIntent(TestIntent.Increment)

            assertEquals(listOf("handle:Increment"), events)
        } finally {
            MviConfig.reset()
        }
    }

    private data class TestState(
        val value: Int,
    ) : StateUi

    private sealed interface TestIntent : IntentUi {
        data object Increment : TestIntent
    }

    private sealed interface TestEffect : EffectUi {
        data class ShowMessage(val text: String) : TestEffect
    }

    private class TestStore(
        private val events: MutableList<String>,
    ) : BaseMviStore<TestState, TestIntent, TestEffect>(
        initialState = TestState(value = 0),
    ) {
        override fun handleIntent(intent: TestIntent) {
            when (intent) {
                TestIntent.Increment -> {
                    events += "handle:$intent"
                    updateState { copy(value = value + 1) }
                }
            }
        }
    }
}
