package io.github.v1rusdev.simplemvi.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull

class MviStoreEffectsTest {

    @Test
    fun try_emit_effect_delivers_effect_to_active_collector() = runTest {
        val store = createStore<TestState, TestIntent, TestEffect>(
            initialState = TestState,
        )
        val receivedEffect = async(start = CoroutineStart.UNDISPATCHED) {
            store.uiEffects.first()
        }

        val wasAccepted = store.tryEmitEffect(TestEffect.NavigateBack)

        assertTrue(wasAccepted)
        assertEquals(TestEffect.NavigateBack, receivedEffect.await())
    }

    @Test
    fun emit_effect_delivers_effect_to_active_collector() = runTest {
        val store = createStore<TestState, TestIntent, TestEffect>(
            initialState = TestState,
        )
        val receivedEffect = async(start = CoroutineStart.UNDISPATCHED) {
            store.uiEffects.first()
        }

        store.emitEffect(TestEffect.ShowMessage("Saved"))

        assertEquals(TestEffect.ShowMessage("Saved"), receivedEffect.await())
    }

    @Test
    fun ui_effects_do_not_replay_previous_effects_to_new_collectors() = runTest {
        val store = createStore<TestState, TestIntent, TestEffect>(
            initialState = TestState,
        )

        assertTrue(store.tryEmitEffect(TestEffect.NavigateBack))

        val replayedEffect = withTimeoutOrNull(50) {
            store.uiEffects.first()
        }

        assertNull(replayedEffect)
    }

    @Test
    fun try_emit_effect_returns_false_when_a_suspending_buffer_rejects_the_effect() = runTest {
        val store = createStore<TestState, TestIntent, TestEffect>(
            initialState = TestState,
            extraBufferCapacity = 0,
            onBufferOverflow = BufferOverflow.SUSPEND,
        )
        val subscription = launch(start = CoroutineStart.UNDISPATCHED) {
            store.uiEffects.collect { }
        }

        assertFalse(store.tryEmitEffect(TestEffect.NavigateBack))

        subscription.cancel()
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun drop_oldest_buffer_keeps_the_latest_effect_for_a_suspended_collector() = runTest {
        val store = createStore<TestState, TestIntent, TestEffect>(
            initialState = TestState,
            extraBufferCapacity = 1,
            onBufferOverflow = BufferOverflow.DROP_OLDEST,
        )
        val gate = CompletableDeferred<Unit>()
        val received = mutableListOf<TestEffect>()
        val subscription = launch(start = CoroutineStart.UNDISPATCHED) {
            store.uiEffects.collect { effect ->
                received += effect
                if (received.size == 1) {
                    gate.await()
                }
            }
        }

        // Let the collector take the first effect and stall on the gate, so that the single
        // buffer slot holds "second" until "third" pushes it out.
        assertTrue(store.tryEmitEffect(TestEffect.ShowMessage("first")))
        runCurrent()

        assertTrue(store.tryEmitEffect(TestEffect.ShowMessage("second")))
        assertTrue(store.tryEmitEffect(TestEffect.ShowMessage("third")))

        gate.complete(Unit)
        advanceUntilIdle()
        subscription.cancel()

        assertEquals(
            listOf<TestEffect>(
                TestEffect.ShowMessage("first"),
                TestEffect.ShowMessage("third"),
            ),
            received,
        )
    }

    private data object TestState : StateUi

    private sealed interface TestIntent : IntentUi

    private sealed interface TestEffect : EffectUi {
        data object NavigateBack : TestEffect
        data class ShowMessage(val text: String) : TestEffect
    }
}
