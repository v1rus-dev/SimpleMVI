package yegor.cheprasov.simplemvi.compose.android

import androidx.lifecycle.SavedStateHandle

inline fun <reified Value> SavedStateHandle.getOrPut(
    key: String,
    defaultValue: () -> Value,
): Value {
    return get<Value>(key) ?: defaultValue().also { value ->
        this[key] = value
    }
}
