package yegor.cheprasov.simplemvi.compose.android

import androidx.lifecycle.SavedStateHandle

/**
 * Returns a saved value for [key] or stores and returns [defaultValue] when the key is missing.
 *
 * Use this helper for values supported by [SavedStateHandle], such as route arguments or small UI flags.
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
