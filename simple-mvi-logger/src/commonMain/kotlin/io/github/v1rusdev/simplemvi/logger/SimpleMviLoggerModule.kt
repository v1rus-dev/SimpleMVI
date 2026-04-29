package io.github.v1rusdev.simplemvi.logger

import io.github.v1rusdev.simplemvi.core.EmptySimpleMviLogger
import io.github.v1rusdev.simplemvi.core.SimpleMviConfig

/**
 * Installs or removes the optional SimpleMVI logger implementation.
 */
object SimpleMviLoggerModule {
    fun install() {
        SimpleMviConfig.logger = createDefaultSimpleMviLogger()
    }

    fun uninstall() {
        SimpleMviConfig.logger = EmptySimpleMviLogger
    }
}
