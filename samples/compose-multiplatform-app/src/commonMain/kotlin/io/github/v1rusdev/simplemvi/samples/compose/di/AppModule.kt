package io.github.v1rusdev.simplemvi.samples.compose.di

import io.github.v1rusdev.simplemvi.core.SimpleMVI
import io.github.v1rusdev.simplemvi.samples.compose.MainViewModel
import io.github.v1rusdev.simplemvi.samples.compose.checkout.CheckoutMviViewModel
import io.github.v1rusdev.simplemvi.samples.compose.counter.CounterViewModel
import io.github.v1rusdev.simplemvi.samples.compose.theme.ThemeEffectUi
import io.github.v1rusdev.simplemvi.samples.compose.theme.ThemeIntentUi
import io.github.v1rusdev.simplemvi.samples.compose.theme.ThemeStateUi
import io.github.v1rusdev.simplemvi.samples.compose.theme.ThemeStore
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val ThemeStoreQualifier = "themeStore"

private val appModule = module {
    single<SimpleMVI<ThemeStateUi, ThemeIntentUi, ThemeEffectUi>>(named(ThemeStoreQualifier)) {
        ThemeStore()
    }
    viewModel {
        MainViewModel(
            themeStore = get(named(ThemeStoreQualifier)),
        )
    }
    viewModel { CounterViewModel() }
    viewModel { CheckoutMviViewModel() }
}

fun initKoin() {
    startKoin {
        modules(appModule)
    }
}
