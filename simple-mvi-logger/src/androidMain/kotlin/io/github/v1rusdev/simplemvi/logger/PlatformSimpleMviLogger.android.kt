package io.github.v1rusdev.simplemvi.logger

import android.util.Log
import io.github.v1rusdev.simplemvi.core.SimpleMviLogger

internal actual fun platformSimpleMviLogger(): SimpleMviLogger = AndroidSimpleMviLogger

private object AndroidSimpleMviLogger : SimpleMviLogger {
    override fun logIntent(
        tag: String,
        intent: Any,
    ) {
        Log.d(tag.toAndroidLogTag(), "Intent received: $intent")
    }

    override fun logStateChange(
        tag: String,
        oldState: Any,
        newState: Any,
    ) {
        Log.d(tag.toAndroidLogTag(), "State changed: $oldState -> $newState")
    }
}

private fun String.toAndroidLogTag(): String {
    return ifBlank { "SimpleMVI" }.take(AndroidTagMaxLength)
}

private const val AndroidTagMaxLength = 23
