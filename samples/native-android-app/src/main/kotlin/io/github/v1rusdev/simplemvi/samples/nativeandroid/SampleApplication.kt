package io.github.v1rusdev.simplemvi.samples.nativeandroid

import android.app.Application
import android.util.Log
import io.github.v1rusdev.simplemvi.core.MviConfig

private const val TAG = "SimpleMVI"

/**
 * Wires the global SimpleMVI observability hooks once, at process startup.
 *
 * The hooks are process-wide, so an [Application] is the right place for them: configuring them
 * from a composable or an activity would re-run on every recreation.
 */
class SampleApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        MviConfig.configure {
            onIntent { intent ->
                Log.d(TAG, "Intent: $intent")
            }
            onEffect { effect ->
                Log.d(TAG, "Effect: $effect")
            }
            onError { error ->
                Log.e(TAG, "Observability hook failed", error)
            }
        }
    }
}
