package io.github.v1rusdev.simplemvi.samples.compose.checkout

import io.github.v1rusdev.simplemvi.compose.MviViewModel

class CheckoutMviViewModel : MviViewModel<CheckoutState, CheckoutIntent, CheckoutEffect>(
    initialState = CheckoutState(),
) {

    override fun onIntent(intent: CheckoutIntent) {
        when (intent) {
            CheckoutIntent.ApplyPromo -> applyPromo()
            CheckoutIntent.BackClick -> sendEffect(CheckoutEffect.NavigateBack)
            is CheckoutIntent.ChangePromoCode -> updateState {
                copy(
                    promoCode = intent.code.take(12).uppercase(),
                    promoApplied = false,
                )
            }
            is CheckoutIntent.ChangeSeats -> updateState {
                copy(seats = intent.seats.coerceIn(1, 12))
            }
            CheckoutIntent.PayClick -> {
                val total = uiState.value.total
                sendEffect(CheckoutEffect.ShowMessage("Payment request for ${'$'}$total created"))
            }
            is CheckoutIntent.SelectPlan -> updateState {
                copy(selectedPlan = intent.plan)
            }
        }
    }

    private fun applyPromo() {
        val code = uiState.value.promoCode.trim()
        if (code == "MVI20") {
            updateState { copy(promoApplied = true) }
            sendEffect(CheckoutEffect.ShowMessage("Promo MVI20 applied"))
        } else {
            updateState { copy(promoApplied = false) }
            sendEffect(CheckoutEffect.ShowMessage("Use MVI20 to apply a sample discount"))
        }
    }
}
