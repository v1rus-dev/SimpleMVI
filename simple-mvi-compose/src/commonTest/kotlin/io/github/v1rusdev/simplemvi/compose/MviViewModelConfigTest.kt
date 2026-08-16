package io.github.v1rusdev.simplemvi.compose

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import io.github.v1rusdev.simplemvi.core.EffectUi
import io.github.v1rusdev.simplemvi.core.IntentUi
import io.github.v1rusdev.simplemvi.core.MviConfig
import io.github.v1rusdev.simplemvi.core.StateUi
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest

class MviViewModelConfigTest {

    @Test
    fun on_intent_notifies_global_hook_before_handle_intent() {
        val events = mutableListOf<String>()
        val viewModel = TestViewModel(events)

        try {
            MviConfig.configure {
                onIntent { intent ->
                    events += "hook:$intent"
                }
            }

            viewModel.onIntent(TestIntent.Increment)

            assertEquals(
                listOf(
                    "hook:Increment",
                    "handle:Increment",
                ),
                events,
            )
            assertEquals(TestState(value = 1), viewModel.uiState.value)
        } finally {
            MviConfig.reset()
        }
    }

    @Test
    fun send_effect_reports_rejection_and_skips_the_effect_hook() = runTest {
        val events = mutableListOf<String>()
        val viewModel = RejectingViewModel(events)
        val subscription = launch(start = CoroutineStart.UNDISPATCHED) {
            viewModel.uiEffects.collect { }
        }

        try {
            MviConfig.configure {
                onEffect { effect ->
                    events += "hook:$effect"
                }
            }

            assertFalse(viewModel.send(TestEffect.ShowMessage("Dropped")))

            assertEquals(emptyList<String>(), events)
        } finally {
            MviConfig.reset()
            subscription.cancel()
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

    private class TestViewModel(
        private val events: MutableList<String>,
    ) : MviViewModel<TestState, TestIntent, TestEffect>(
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

    /**
     * Uses a rendezvous effect buffer so that [sendEffect] is rejected while a collector is busy.
     */
    private class RejectingViewModel(
        private val events: MutableList<String>,
    ) : MviViewModel<TestState, TestIntent, TestEffect>(
        initialState = TestState(value = 0),
        extraBufferCapacity = 0,
        onBufferOverflow = BufferOverflow.SUSPEND,
    ) {
        fun send(effect: TestEffect): Boolean = sendEffect(effect)

        override fun handleIntent(intent: TestIntent) {
            events += "handle:$intent"
        }
    }
}
