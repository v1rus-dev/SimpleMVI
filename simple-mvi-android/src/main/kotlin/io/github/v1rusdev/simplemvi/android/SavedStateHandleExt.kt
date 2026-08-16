package io.github.v1rusdev.simplemvi.android

import androidx.lifecycle.SavedStateHandle

/**
 * Returns a saved value for [key] or stores and returns [defaultValue] when the key is missing.
 *
 * Use this helper for values supported by [SavedStateHandle], such as route arguments or small UI
 * flags.
 *
 * A stored `null` is indistinguishable from a missing key, so for a nullable [Value] this
 * recomputes and rewrites [defaultValue] on every call. Use a non-null type, or read the handle
 * directly, when `null` is a meaningful stored value.
 *
 * Example:
 * ```
 * val profileId = savedStateHandle.getOrPut("profile_id") {
 *     "me"
 * }
 * ```
 */
inline fun <reified Value> SavedStateHandle.getOrPut(
    key: String,
    defaultValue: () -> Value,
): Value {
    return get<Value>(key) ?: defaultValue().also { value ->
        this[key] = value
    }
}
