package io.github.v1rusdev.simplemvi.core

/**
 * Global SimpleMVI configuration shared by all stores.
 *
 * The default logger is [EmptySimpleMviLogger], so logging stays disabled until another module
 * installs a real implementation.
 */
object SimpleMviConfig {
    var logger: SimpleMviLogger = EmptySimpleMviLogger
}

/**
 * Minimal logging contract used by SimpleMVI core.
 *
 * Optional logging modules can replace [SimpleMviConfig.logger] with a real implementation.
 */
interface SimpleMviLogger {
    fun logIntent(
        tag: String,
        intent: Any,
    )

    fun logStateChange(
        tag: String,
        oldState: Any,
        newState: Any,
    )
}

/**
 * No-op logger used by default when no logger module is installed.
 */
object EmptySimpleMviLogger : SimpleMviLogger {
    override fun logIntent(
        tag: String,
        intent: Any,
    ) = Unit

    override fun logStateChange(
        tag: String,
        oldState: Any,
        newState: Any,
    ) = Unit
}

internal const val DefaultSimpleMviLoggerTag = "SimpleMVI"

public fun Any.resolveSimpleMviLoggerTag(): String {
    val simpleName = this::class.simpleName

    return when {
        simpleName.isNullOrBlank() -> DefaultSimpleMviLoggerTag
        simpleName == "SimpleMVIDelegate" -> DefaultSimpleMviLoggerTag
        else -> simpleName
    }
}
