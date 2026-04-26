package io.github.v1rusdev.simplemvi.samples.compose.checkout

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.v1rusdev.simplemvi.compose.CollectEffectsUiEvent
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun CheckoutRoute(
    onBack: () -> Unit,
    viewModel: CheckoutMviViewModel = koinViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    CollectEffectsUiEvent(viewModel.uiEffects) { effect ->
        when (effect) {
            CheckoutEffect.NavigateBack -> onBack()
            is CheckoutEffect.ShowMessage -> snackbarHostState.showSnackbar(effect.message)
        }
    }

    CheckoutScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CheckoutScreen(
    state: CheckoutState,
    snackbarHostState: SnackbarHostState,
    onIntent: (CheckoutIntent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("MVI Checkout") },
                navigationIcon = {
                    TextButton(onClick = { onIntent(CheckoutIntent.BackClick) }) {
                        Text("Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { paddingValues ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            color = MaterialTheme.colorScheme.background,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(14.dp),
                    ) {
                        Text(
                            text = "Plan",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Plan.entries.forEach { plan ->
                                FilterChip(
                                    selected = state.selectedPlan == plan,
                                    onClick = { onIntent(CheckoutIntent.SelectPlan(plan)) },
                                    label = { Text("${plan.title} ${'$'}${plan.price}") },
                                )
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        Text(
                            text = "Seats",
                            style = MaterialTheme.typography.titleMedium,
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    onIntent(CheckoutIntent.ChangeSeats(state.seats - 1))
                                },
                            ) {
                                Text("-")
                            }
                            Text(
                                modifier = Modifier
                                    .weight(1f)
                                    .padding(top = 10.dp),
                                text = state.seats.toString(),
                                textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleLarge,
                            )
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    onIntent(CheckoutIntent.ChangeSeats(state.seats + 1))
                                },
                            ) {
                                Text("+")
                            }
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                    ) {
                        OutlinedTextField(
                            modifier = Modifier.fillMaxWidth(),
                            value = state.promoCode,
                            onValueChange = { onIntent(CheckoutIntent.ChangePromoCode(it)) },
                            label = { Text("Promo code") },
                            supportingText = { Text("Try MVI20") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Text),
                        )
                        Button(
                            modifier = Modifier.fillMaxWidth(),
                            onClick = { onIntent(CheckoutIntent.ApplyPromo) },
                        ) {
                            Text("Apply promo")
                        }
                    }
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer,
                    ),
                ) {
                    Column(
                        modifier = Modifier.padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SummaryRow(label = "Subtotal", value = "${'$'}${state.subtotal}")
                        SummaryRow(label = "Discount", value = "-${'$'}${state.discount}")
                        SummaryRow(label = "Total", value = "${'$'}${state.total}")
                    }
                }

                Button(
                    modifier = Modifier.fillMaxWidth(),
                    onClick = { onIntent(CheckoutIntent.PayClick) },
                ) {
                    Text("Pay")
                }
            }
        }
    }
}

@Composable
private fun SummaryRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
    ) {
        Text(
            text = label,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
        Text(
            text = value,
            color = MaterialTheme.colorScheme.onPrimaryContainer,
        )
    }
}
