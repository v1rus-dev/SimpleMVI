package io.github.v1rusdev.simplemvi.samples.compose.counter

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class CounterState(
    val count: Int = 0,
    val step: Int = 1,
    val autoResetEnabled: Boolean = false,
)

class CounterViewModel : ViewModel() {

    private val mutableState = MutableStateFlow(CounterState())
    val state: StateFlow<CounterState> = mutableState.asStateFlow()

    fun increment() {
        mutableState.update { state ->
            val nextCount = state.count + state.step
            state.copy(count = normalizeCount(nextCount, state.autoResetEnabled))
        }
    }

    fun decrement() {
        mutableState.update { state ->
            val nextCount = state.count - state.step
            state.copy(count = normalizeCount(nextCount, state.autoResetEnabled))
        }
    }

    fun reset() {
        mutableState.update { it.copy(count = 0) }
    }

    fun changeStep(step: Int) {
        mutableState.update { it.copy(step = step) }
    }

    fun setAutoResetEnabled(enabled: Boolean) {
        mutableState.update { state ->
            state.copy(
                count = normalizeCount(state.count, enabled),
                autoResetEnabled = enabled,
            )
        }
    }

    private fun normalizeCount(count: Int, autoResetEnabled: Boolean): Int {
        return if (autoResetEnabled && count >= 10) {
            0
        } else {
            count
        }
    }
}
