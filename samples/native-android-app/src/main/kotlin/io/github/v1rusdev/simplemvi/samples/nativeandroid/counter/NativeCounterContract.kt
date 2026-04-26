package io.github.v1rusdev.simplemvi.samples.nativeandroid.counter

import io.github.v1rusdev.simplemvi.core.EffectUi
import io.github.v1rusdev.simplemvi.core.IntentUi
import io.github.v1rusdev.simplemvi.core.StateUi

data class NativeCounterState(
    val count: Int,
    val step: Int,
) : StateUi

sealed interface NativeCounterIntent : IntentUi {
    data object Increment : NativeCounterIntent
    data object Decrement : NativeCounterIntent
    data object Reset : NativeCounterIntent
    data class SelectStep(val step: Int) : NativeCounterIntent
}

sealed interface NativeCounterEffect : EffectUi {
    data class Message(val text: String) : NativeCounterEffect
}
