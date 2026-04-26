package io.github.v1rusdev.simplemvi.samples.compose.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import io.github.v1rusdev.simplemvi.samples.compose.checkout.CheckoutRoute
import io.github.v1rusdev.simplemvi.samples.compose.counter.CounterRoute
import io.github.v1rusdev.simplemvi.samples.compose.home.HomeRoute
import io.github.v1rusdev.simplemvi.samples.compose.theme.ThemeRoute

private object Routes {
    const val Home = "home"
    const val Counter = "counter"
    const val Checkout = "checkout"
    const val Theme = "theme"
}

@Composable
fun SampleNavHost() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = Routes.Home,
    ) {
        composable(Routes.Home) {
            HomeRoute(
                onCounterClick = { navController.navigate(Routes.Counter) },
                onCheckoutClick = { navController.navigate(Routes.Checkout) },
                onThemeClick = { navController.navigate(Routes.Theme) },
            )
        }
        composable(Routes.Counter) {
            CounterRoute(
                onBackClick = { navController.popBackStack() },
            )
        }
        composable(Routes.Checkout) {
            CheckoutRoute(
                onBack = { navController.popBackStack() },
            )
        }
        composable(Routes.Theme) {
            ThemeRoute(
                onBackClick = { navController.popBackStack() },
            )
        }
    }
}
