package yegor.cheprasov.simplemvi.core

/**
 * Marker interface for immutable UI state used by a SimpleMVI store.
 *
 * Prefer sealed interfaces or data classes so every screen state is explicit.
 *
 * Example:
 * ```
 * sealed interface ProfileState : StateUi {
 *     data object Loading : ProfileState
 *     data class Content(val name: String) : ProfileState
 * }
 *
 * data class ProfileState(val name: String, val email: String) : StateUi
 * ```
 */
interface StateUi
