package io.github.v1rusdev.simplemvi.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.withTimeoutOrNull

class SimpleMVIEffectsTest {

    @Test
    fun try_emit_effect_delivers_effect_to_active_collector() = runTest {
        val store = mvi<TestState, TestIntent, TestEffect>(
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
        val store = mvi<TestState, TestIntent, TestEffect>(
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
        val store = mvi<TestState, TestIntent, TestEffect>(
            initialState = TestState,
        )

        assertTrue(store.tryEmitEffect(TestEffect.NavigateBack))

        val replayedEffect = withTimeoutOrNull(50) {
            store.uiEffects.first()
        }

        assertNull(replayedEffect)
    }

    private data object TestState : StateUi

    private sealed interface TestIntent : IntentUi

    private sealed interface TestEffect : EffectUi {
        data object NavigateBack : TestEffect
        data class ShowMessage(val text: String) : TestEffect
    }
}
