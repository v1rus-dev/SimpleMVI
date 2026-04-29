package io.github.v1rusdev.simplemvi.logger

import io.github.v1rusdev.simplemvi.core.SimpleMviLogger

internal expect fun platformSimpleMviLogger(): SimpleMviLogger

/**
 * Creates the default platform logger used by [SimpleMviLoggerModule].
 */
fun createDefaultSimpleMviLogger(): SimpleMviLogger = platformSimpleMviLogger()
