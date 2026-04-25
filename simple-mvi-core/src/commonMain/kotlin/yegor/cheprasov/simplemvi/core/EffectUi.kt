package yegor.cheprasov.simplemvi.core

/**
 * Marker interface for one-time UI effects such as navigation, snackbars, or dialogs.
 *
 * Effects are emitted through [SimpleMVI.uiEffects] and should not be stored in [StateUi].
 *
 * Example:
 * ```
 * sealed interface ProfileEffect : EffectUi {
 *     data class ShowMessage(val text: String) : ProfileEffect
 *     data object CloseScreen : ProfileEffect
 * }
 * ```
 */
interface EffectUi
