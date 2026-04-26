package io.github.v1rusdev.simplemvi.core

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotSame
import kotlin.test.assertSame
import org.koin.core.qualifier.named
import org.koin.dsl.koinApplication
import org.koin.dsl.module

class SimpleMVIKoinTest {

    @Test
    fun store_keeps_initial_state() {
        val store = mvi<TestState, TestIntent, TestEffect>(
            initialState = TestState(value = 7),
        )

        assertEquals(TestState(value = 7), store.uiState.value)
    }

    @Test
    fun update_state_changes_only_current_store() {
        val firstStore = mvi<TestState, TestIntent, TestEffect>(
            initialState = TestState(value = 0),
        )
        val secondStore = mvi<TestState, TestIntent, TestEffect>(
            initialState = TestState(value = 100),
        )

        val updatedState = firstStore.updateState {
            copy(value = value + 1)
        }

        assertEquals(TestState(value = 1), updatedState)
        assertEquals(TestState(value = 1), firstStore.uiState.value)
        assertEquals(TestState(value = 100), secondStore.uiState.value)
    }

    @Test
    fun koin_returns_same_singleton_store_for_same_qualifier() {
        val app = koinApplication {
            modules(
                module {
                    single<SimpleMVI<TestState, TestIntent, TestEffect>>(named("firstStore")) {
                        TestStore(initialValue = 0)
                    }
                },
            )
        }

        val firstInstance = app.koin.get<SimpleMVI<TestState, TestIntent, TestEffect>>(
            named("firstStore"),
        )
        val secondInstance = app.koin.get<SimpleMVI<TestState, TestIntent, TestEffect>>(
            named("firstStore"),
        )

        assertSame(firstInstance, secondInstance)
    }

    @Test
    fun koin_named_singleton_stores_do_not_share_state() {
        val app = koinApplication {
            modules(
                module {
                    single<SimpleMVI<TestState, TestIntent, TestEffect>>(named("firstStore")) {
                        TestStore(initialValue = 0)
                    }
                    single<SimpleMVI<TestState, TestIntent, TestEffect>>(named("secondStore")) {
                        TestStore(initialValue = 100)
                    }
                },
            )
        }

        val firstStore = app.koin.get<SimpleMVI<TestState, TestIntent, TestEffect>>(
            named("firstStore"),
        )
        val secondStore = app.koin.get<SimpleMVI<TestState, TestIntent, TestEffect>>(
            named("secondStore"),
        )

        assertNotSame(firstStore, secondStore)

        firstStore.onIntent(TestIntent.Increment)
        secondStore.onIntent(TestIntent.Toggle)

        assertEquals(TestState(value = 1, enabled = false), firstStore.uiState.value)
        assertEquals(TestState(value = 100, enabled = true), secondStore.uiState.value)
    }

    @Test
    fun koin_stores_with_different_contracts_do_not_break_each_other() {
        val app = koinApplication {
            modules(
                module {
                    single<SimpleMVI<TestState, TestIntent, TestEffect>>(named("counterStore")) {
                        TestStore(initialValue = 0)
                    }
                    single<SimpleMVI<OtherState, OtherIntent, OtherEffect>>(named("textStore")) {
                        OtherStore(initialText = "initial")
                    }
                    single<SimpleMVI<FlagState, FlagIntent, FlagEffect>>(named("flagStore")) {
                        FlagStore(initialEnabled = false)
                    }
                },
            )
        }

        val counterStore = app.koin.get<SimpleMVI<TestState, TestIntent, TestEffect>>(
            named("counterStore"),
        )
        val textStore = app.koin.get<SimpleMVI<OtherState, OtherIntent, OtherEffect>>(
            named("textStore"),
        )
        val flagStore = app.koin.get<SimpleMVI<FlagState, FlagIntent, FlagEffect>>(
            named("flagStore"),
        )

        counterStore.onIntent(TestIntent.Increment)
        textStore.onIntent(OtherIntent.ChangeText("changed"))
        flagStore.onIntent(FlagIntent.Enable)

        assertEquals(TestState(value = 1), counterStore.uiState.value)
        assertEquals(OtherState(text = "changed"), textStore.uiState.value)
        assertEquals(FlagState(enabled = true), flagStore.uiState.value)
    }

    @Test
    fun koin_factories_with_same_store_contract_create_independent_instances() {
        val app = koinApplication {
            modules(
                module {
                    factory<SimpleMVI<TestState, TestIntent, TestEffect>>(named("counterFactory")) {
                        TestStore(initialValue = 0)
                    }
                },
            )
        }

        val firstStore = app.koin.get<SimpleMVI<TestState, TestIntent, TestEffect>>(
            named("counterFactory"),
        )
        val secondStore = app.koin.get<SimpleMVI<TestState, TestIntent, TestEffect>>(
            named("counterFactory"),
        )

        assertNotSame(firstStore, secondStore)

        firstStore.onIntent(TestIntent.Increment)
        firstStore.onIntent(TestIntent.Increment)

        assertEquals(TestState(value = 2), firstStore.uiState.value)
        assertEquals(TestState(value = 0), secondStore.uiState.value)
    }

    @Test
    fun koin_factories_with_different_store_contracts_resolve_expected_types() {
        val app = koinApplication {
            modules(
                module {
                    factory<SimpleMVI<TestState, TestIntent, TestEffect>>(named("counterFactory")) {
                        TestStore(initialValue = 10)
                    }
                    factory<SimpleMVI<OtherState, OtherIntent, OtherEffect>>(named("textFactory")) {
                        OtherStore(initialText = "initial")
                    }
                    factory<SimpleMVI<FlagState, FlagIntent, FlagEffect>>(named("flagFactory")) {
                        FlagStore(initialEnabled = false)
                    }
                },
            )
        }

        val counterStore = app.koin.get<SimpleMVI<TestState, TestIntent, TestEffect>>(
            named("counterFactory"),
        )
        val textStore = app.koin.get<SimpleMVI<OtherState, OtherIntent, OtherEffect>>(
            named("textFactory"),
        )
        val flagStore = app.koin.get<SimpleMVI<FlagState, FlagIntent, FlagEffect>>(
            named("flagFactory"),
        )

        counterStore.onIntent(TestIntent.Increment)
        textStore.onIntent(OtherIntent.ChangeText("factory"))
        flagStore.onIntent(FlagIntent.Enable)

        assertEquals(TestState(value = 11), counterStore.uiState.value)
        assertEquals(OtherState(text = "factory"), textStore.uiState.value)
        assertEquals(FlagState(enabled = true), flagStore.uiState.value)
    }

    private data class TestState(
        val value: Int,
        val enabled: Boolean = false,
    ) : StateUi

    private sealed interface TestIntent : IntentUi {
        data object Increment : TestIntent
        data object Toggle : TestIntent
    }

    private sealed interface TestEffect : EffectUi

    private class TestStore(
        initialValue: Int,
    ) : SimpleMVI<TestState, TestIntent, TestEffect> by mvi(
        initialState = TestState(value = initialValue),
    ) {
        override fun onIntent(intent: TestIntent) {
            when (intent) {
                TestIntent.Increment -> updateState { copy(value = value + 1) }
                TestIntent.Toggle -> updateState { copy(enabled = !enabled) }
            }
        }
    }

    private data class OtherState(
        val text: String,
    ) : StateUi

    private sealed interface OtherIntent : IntentUi {
        data class ChangeText(val value: String) : OtherIntent
    }

    private sealed interface OtherEffect : EffectUi

    private class OtherStore(
        initialText: String,
    ) : SimpleMVI<OtherState, OtherIntent, OtherEffect> by mvi(
        initialState = OtherState(text = initialText),
    ) {
        override fun onIntent(intent: OtherIntent) {
            when (intent) {
                is OtherIntent.ChangeText -> updateState { copy(text = intent.value) }
            }
        }
    }

    private data class FlagState(
        val enabled: Boolean,
    ) : StateUi

    private sealed interface FlagIntent : IntentUi {
        data object Enable : FlagIntent
    }

    private sealed interface FlagEffect : EffectUi

    private class FlagStore(
        initialEnabled: Boolean,
    ) : SimpleMVI<FlagState, FlagIntent, FlagEffect> by mvi(
        initialState = FlagState(enabled = initialEnabled),
    ) {
        override fun onIntent(intent: FlagIntent) {
            when (intent) {
                FlagIntent.Enable -> updateState { copy(enabled = true) }
            }
        }
    }
}
