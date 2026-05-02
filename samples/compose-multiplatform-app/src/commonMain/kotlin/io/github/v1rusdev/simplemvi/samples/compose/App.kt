package io.github.v1rusdev.simplemvi.samples.compose

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.aakira.napier.Napier
import io.github.v1rusdev.simplemvi.core.SimpleMviConfig
import io.github.v1rusdev.simplemvi.samples.compose.navigation.SampleNavHost
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun App(
    viewModel: MainViewModel = koinViewModel(),
) {

    LaunchedEffect(Unit) {
        SimpleMviConfig.configure {
            onIntent { intent ->
                Napier.v("Intent $intent")
            }
            onEffect { effect ->
                Napier.v("Effect: $effect")
            }
        }
    }

    val themeState by viewModel.themeState.collectAsStateWithLifecycle()
    val colorScheme = if (themeState.isDarkTheme) {
        darkColorScheme()
    } else {
        lightColorScheme()
    }

    MaterialTheme(colorScheme = colorScheme) {
        SampleNavHost()
    }
}
