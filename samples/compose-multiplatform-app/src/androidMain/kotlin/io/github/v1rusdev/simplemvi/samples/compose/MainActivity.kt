package io.github.v1rusdev.simplemvi.samples.compose

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import io.github.v1rusdev.simplemvi.samples.compose.di.initKoin

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initNapier()
        initKoin()
        setContent {
            App()
        }
    }
}
