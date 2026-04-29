package io.github.v1rusdev.simplemvi.samples.nativeandroid.counter

import androidx.lifecycle.SavedStateHandle
import io.github.v1rusdev.simplemvi.compose.MviViewModel
import io.github.v1rusdev.simplemvi.compose.android.getOrPut
import io.github.v1rusdev.simplemvi.core.handleIntent

class NativeCounterViewModel(
    // SavedStateHandle is optional for MviViewModel. This sample uses it only to keep
    // small counter state after Android recreates the ViewModel.
    private val savedStateHandle: SavedStateHandle,
) : MviViewModel<NativeCounterState, NativeCounterIntent, NativeCounterEffect>(
    initialState = NativeCounterState(
        count = savedStateHandle.getOrPut(KeyCount) { 0 },
        step = savedStateHandle.getOrPut(KeyStep) { 1 },
    ),
) {

    override fun onIntent(intent: NativeCounterIntent) = handleIntent(intent) {
        when (intent) {
            NativeCounterIntent.Decrement -> changeCount(delta = -uiState.value.step)
            NativeCounterIntent.Increment -> changeCount(delta = uiState.value.step)
            NativeCounterIntent.Reset -> {
                updateAndPersist { copy(count = 0) }
                sendEffect(NativeCounterEffect.Message("Counter reset"))
            }
            is NativeCounterIntent.SelectStep -> {
                updateAndPersist { copy(step = intent.step) }
                sendEffect(NativeCounterEffect.Message("Step changed to ${intent.step}"))
            }
        }
    }

    private fun changeCount(delta: Int) {
        val state = updateAndPersist { copy(count = count + delta) }
        sendEffect(NativeCounterEffect.Message("Count is ${state.count}"))
    }

    private fun updateAndPersist(
        transform: NativeCounterState.() -> NativeCounterState,
    ): NativeCounterState {
        val state = updateState(transform)
        savedStateHandle[KeyCount] = state.count
        savedStateHandle[KeyStep] = state.step
        return state
    }

    private companion object {
        const val KeyCount = "count"
        const val KeyStep = "step"
    }
}
