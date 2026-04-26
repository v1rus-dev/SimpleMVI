package io.github.v1rusdev.simplemvi.samples.compose

import androidx.compose.ui.window.ComposeUIViewController
import io.github.v1rusdev.simplemvi.samples.compose.di.initKoin

fun MainViewController() = ComposeUIViewController {
    initKoin()
    App()
}
