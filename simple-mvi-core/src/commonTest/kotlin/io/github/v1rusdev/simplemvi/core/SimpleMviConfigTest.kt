package io.github.v1rusdev.simplemvi.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class SimpleMviConfigTest {

    @Test
    fun configure_notifies_intent_before_store_handles_it() {
        val events = mutableListOf<String>()
        val store = TestStore(events)

        try {
            SimpleMviConfig.configure {
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
            SimpleMviConfig.reset()
        }
    }

    @Test
    fun configure_replaces_previous_callbacks() {
        val events = mutableListOf<String>()
        val store = TestStore(events)

        try {
            SimpleMviConfig.configure {
                onIntent { events += "first" }
            }
            SimpleMviConfig.configure {
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
            SimpleMviConfig.reset()
        }
    }

    @Test
    fun reset_restores_no_op_callbacks() {
        val events = mutableListOf<String>()
        val store = TestStore(events)

        SimpleMviConfig.configure {
            onIntent { events += "hook" }
        }
        SimpleMviConfig.reset()

        store.onIntent(TestIntent.Increment)

        assertEquals(
            listOf("handle:Increment"),
            events,
        )
    }

    @Test
    fun try_emit_effect_notifies_effect_hook_and_delivers_effect() = runTest {
        val events = mutableListOf<String>()
        val store = TestStore(events)
        val receivedEffect = async(start = CoroutineStart.UNDISPATCHED) {
            store.uiEffects.first()
        }

        try {
            SimpleMviConfig.configure {
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
            SimpleMviConfig.reset()
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
            SimpleMviConfig.configure {
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
            SimpleMviConfig.reset()
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
    ) : SimpleMviStore<TestState, TestIntent, TestEffect>(
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
