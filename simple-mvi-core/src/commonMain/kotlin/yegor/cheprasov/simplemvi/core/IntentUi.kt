package yegor.cheprasov.simplemvi.core

/**
 * Marker interface for user actions or screen events handled by a SimpleMVI store.
 *
 * Keep intents small and named after what happened in the UI.
 *
 * Example:
 * ```
 * sealed interface ProfileIntent : IntentUi {
 *     data object RefreshClicked : ProfileIntent
 *     data object CloseClicked : ProfileIntent
 * }
 * ```
 */
interface IntentUi
