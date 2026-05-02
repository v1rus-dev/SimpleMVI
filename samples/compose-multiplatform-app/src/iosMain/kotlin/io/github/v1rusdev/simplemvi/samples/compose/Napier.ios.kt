package io.github.v1rusdev.simplemvi.samples.compose

import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier

actual fun initNapier() {
    Napier.base(DebugAntilog())
}