package io.github.v1rusdev.simplemvi.samples.compose.di

import io.github.v1rusdev.simplemvi.core.MviStore
import io.github.v1rusdev.simplemvi.samples.compose.MainViewModel
import io.github.v1rusdev.simplemvi.samples.compose.checkout.CheckoutViewModel
import io.github.v1rusdev.simplemvi.samples.compose.counter.CounterViewModel
import io.github.v1rusdev.simplemvi.samples.compose.theme.ThemeEffect
import io.github.v1rusdev.simplemvi.samples.compose.theme.ThemeIntent
import io.github.v1rusdev.simplemvi.samples.compose.theme.ThemeState
import io.github.v1rusdev.simplemvi.samples.compose.theme.ThemeStore
import org.koin.core.context.startKoin
import org.koin.core.module.dsl.viewModel
import org.koin.core.qualifier.named
import org.koin.dsl.module

const val ThemeStoreQualifier = "themeStore"

private val appModule = module {
    single<MviStore<ThemeState, ThemeIntent, ThemeEffect>>(named(ThemeStoreQualifier)) {
        ThemeStore()
    }
    viewModel {
        MainViewModel(
            themeStore = get(named(ThemeStoreQualifier)),
        )
    }
    viewModel { CounterViewModel() }
    viewModel { CheckoutViewModel() }
}

fun initKoin() {
    startKoin {
        modules(appModule)
    }
}
