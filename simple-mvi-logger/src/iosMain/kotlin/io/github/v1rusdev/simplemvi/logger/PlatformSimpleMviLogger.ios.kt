package io.github.v1rusdev.simplemvi.logger

import io.github.v1rusdev.simplemvi.core.SimpleMviLogger
import platform.Foundation.NSLog

internal actual fun platformSimpleMviLogger(): SimpleMviLogger = AppleSimpleMviLogger

private object AppleSimpleMviLogger : SimpleMviLogger {
    override fun logIntent(
        tag: String,
        intent: Any,
    ) {
        NSLog("[$tag] Intent received: $intent")
    }

    override fun logStateChange(
        tag: String,
        oldState: Any,
        newState: Any,
    ) {
        NSLog("[$tag] State changed: $oldState -> $newState")
    }
}
