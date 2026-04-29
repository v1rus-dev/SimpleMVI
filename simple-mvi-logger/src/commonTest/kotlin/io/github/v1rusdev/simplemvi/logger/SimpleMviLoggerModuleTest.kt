package io.github.v1rusdev.simplemvi.logger

import io.github.v1rusdev.simplemvi.core.EmptySimpleMviLogger
import io.github.v1rusdev.simplemvi.core.SimpleMviConfig
import io.github.v1rusdev.simplemvi.core.SimpleMviLogger
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertNotSame
import kotlin.test.assertSame

class SimpleMviLoggerModuleTest {

    @AfterTest
    fun resetLogger() {
        SimpleMviLoggerModule.uninstall()
    }

    @Test
    fun install_replaces_empty_logger_with_platform_logger() {
        SimpleMviLoggerModule.install()

        assertSame(createDefaultSimpleMviLogger(), SimpleMviConfig.logger)
        assertNotSame(EmptySimpleMviLogger, SimpleMviConfig.logger)
    }

    @Test
    fun uninstall_restores_empty_logger() {
        SimpleMviLoggerModule.install()

        SimpleMviLoggerModule.uninstall()

        assertSame(EmptySimpleMviLogger, SimpleMviConfig.logger)
    }

    @Test
    fun create_default_logger_returns_stable_platform_logger() {
        val firstLogger: SimpleMviLogger = createDefaultSimpleMviLogger()
        val secondLogger: SimpleMviLogger = createDefaultSimpleMviLogger()

        assertSame(firstLogger, secondLogger)
    }
}
