package io.github.v1rusdev.simplemvi.core

import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertSame
import kotlin.test.assertTrue
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest

class SimpleMVILoggerTest {

    @AfterTest
    fun resetLogger() {
        SimpleMviConfig.logger = EmptySimpleMviLogger
    }

    @Test
    fun config_uses_empty_logger_by_default() {
        assertSame(EmptySimpleMviLogger, SimpleMviConfig.logger)
    }

    @Test
    fun update_state_does_not_log_when_state_does_not_change() {
        val logger = RecordingLogger()
        SimpleMviConfig.logger = logger
        val store = mvi<TestState, TestIntent, TestEffect>(
            initialState = TestState(count = 1),
        )

        store.updateState { this }

        assertTrue(logger.stateChanges.isEmpty())
    }

    @Test
    fun update_state_logs_when_state_changes() {
        val logger = RecordingLogger()
        SimpleMviConfig.logger = logger
        val store = mvi<TestState, TestIntent, TestEffect>(
            initialState = TestState(count = 0),
        )

        val newState = store.updateState {
            copy(count = 1)
        }

        assertEquals(TestState(count = 1), newState)
        assertEquals(
            listOf(
                StateLogRecord(
                    tag = "SimpleMVI",
                    oldState = TestState(count = 0),
                    newState = TestState(count = 1),
                ),
            ),
            logger.stateChanges,
        )
    }

    @Test
    fun handle_intent_logs_before_running_block() {
        val operations = mutableListOf<String>()
        val logger = RecordingLogger(operations)
        SimpleMviConfig.logger = logger
        val store = LoggedStore(operations)

        store.onIntent(TestIntent.Increment)

        assertEquals(
            listOf(
                "log:LoggedStore:Increment",
                "handled:Increment",
            ),
            operations.take(2),
        )
    }

    @Test
    fun old_on_intent_code_without_handle_intent_still_updates_state() {
        val store = LegacyStore()

        store.onIntent(TestIntent.Increment)

        assertEquals(TestState(count = 1), store.uiState.value)
    }

    @Test
    fun reset_to_empty_logger_stops_logging() {
        val logger = RecordingLogger()
        val store = mvi<TestState, TestIntent, TestEffect>(
            initialState = TestState(count = 0),
        )
        SimpleMviConfig.logger = logger

        store.updateState { copy(count = 1) }
        SimpleMviConfig.logger = EmptySimpleMviLogger
        store.updateState { copy(count = 2) }

        assertEquals(1, logger.stateChanges.size)
        assertEquals(TestState(count = 1), logger.stateChanges.single().newState)
    }

    @Test
    fun logger_does_not_break_effect_flow() = runTest {
        SimpleMviConfig.logger = RecordingLogger()
        val store = mvi<TestState, TestIntent, TestEffect>(
            initialState = TestState(),
        )
        val receivedEffect = async(start = CoroutineStart.UNDISPATCHED) {
            store.uiEffects.first()
        }

        val wasAccepted = store.tryEmitEffect(TestEffect.NavigateBack)

        assertTrue(wasAccepted)
        assertEquals(TestEffect.NavigateBack, receivedEffect.await())
    }

    private data class StateLogRecord(
        val tag: String,
        val oldState: Any,
        val newState: Any,
    )

    private class RecordingLogger(
        private val operations: MutableList<String>? = null,
    ) : SimpleMviLogger {
        val stateChanges = mutableListOf<StateLogRecord>()

        override fun logIntent(
            tag: String,
            intent: Any,
        ) {
            operations?.add("log:$tag:$intent")
        }

        override fun logStateChange(
            tag: String,
            oldState: Any,
            newState: Any,
        ) {
            stateChanges += StateLogRecord(
                tag = tag,
                oldState = oldState,
                newState = newState,
            )
        }
    }

    private data class TestState(
        val count: Int = 0,
    ) : StateUi

    private sealed interface TestIntent : IntentUi {
        data object Increment : TestIntent
    }

    private sealed interface TestEffect : EffectUi {
        data object NavigateBack : TestEffect
    }

    private class LoggedStore(
        private val operations: MutableList<String>,
    ) : SimpleMVI<TestState, TestIntent, TestEffect> by mvi(
        initialState = TestState(),
    ) {
        override val loggerTagOwner: Any
            get() = this

        override fun onIntent(intent: TestIntent) = handleIntent(intent) {
            operations += "handled:$intent"

            when (intent) {
                TestIntent.Increment -> updateState { copy(count = count + 1) }
            }
        }
    }

    private class LegacyStore : SimpleMVI<TestState, TestIntent, TestEffect> by mvi(
        initialState = TestState(),
    ) {
        override fun onIntent(intent: TestIntent) {
            when (intent) {
                TestIntent.Increment -> updateState { copy(count = count + 1) }
            }
        }
    }
}
