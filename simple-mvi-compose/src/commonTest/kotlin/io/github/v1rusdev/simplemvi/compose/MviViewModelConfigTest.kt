package io.github.v1rusdev.simplemvi.compose

import kotlin.test.Test
import kotlin.test.assertEquals
import io.github.v1rusdev.simplemvi.core.EffectUi
import io.github.v1rusdev.simplemvi.core.IntentUi
import io.github.v1rusdev.simplemvi.core.SimpleMviConfig
import io.github.v1rusdev.simplemvi.core.StateUi

class MviViewModelConfigTest {

    @Test
    fun on_intent_notifies_global_hook_before_handle_intent() {
        val events = mutableListOf<String>()
        val viewModel = TestViewModel(events)

        try {
            SimpleMviConfig.configure {
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
            SimpleMviConfig.reset()
        }
    }

    private data class TestState(
        val value: Int,
    ) : StateUi

    private sealed interface TestIntent : IntentUi {
        data object Increment : TestIntent
    }

    private sealed interface TestEffect : EffectUi

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
}
