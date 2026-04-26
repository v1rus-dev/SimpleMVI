package io.github.v1rusdev.simplemvi.samples.compose.checkout

import io.github.v1rusdev.simplemvi.core.EffectUi
import io.github.v1rusdev.simplemvi.core.IntentUi
import io.github.v1rusdev.simplemvi.core.StateUi

data class CheckoutState(
    val selectedPlan: Plan = Plan.Pro,
    val seats: Int = 3,
    val promoCode: String = "",
    val promoApplied: Boolean = false,
) : StateUi {
    val subtotal: Int = selectedPlan.price * seats
    val discount: Int = if (promoApplied) subtotal / 5 else 0
    val total: Int = subtotal - discount
}

enum class Plan(
    val title: String,
    val price: Int,
) {
    Starter("Starter", 12),
    Pro("Pro", 24),
    Team("Team", 39),
}

sealed interface CheckoutIntent : IntentUi {
    data class SelectPlan(val plan: Plan) : CheckoutIntent
    data class ChangeSeats(val seats: Int) : CheckoutIntent
    data class ChangePromoCode(val code: String) : CheckoutIntent
    data object ApplyPromo : CheckoutIntent
    data object PayClick : CheckoutIntent
    data object BackClick : CheckoutIntent
}

sealed interface CheckoutEffect : EffectUi {
    data class ShowMessage(val message: String) : CheckoutEffect
    data object NavigateBack : CheckoutEffect
}
