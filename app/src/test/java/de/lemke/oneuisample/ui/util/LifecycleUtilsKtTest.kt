package de.lemke.oneuisample.ui.util

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf

class LifecycleUtilsKtTest : ShouldSpec(
    {

        should("stateInViewModel initial value is returned before upstream emits") {
            val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
            val state = flowOf(99).stateInViewModel(scope, 0)
            state.value shouldBe 0
            scope.cancel()
        }

        should("stateInViewModel emits upstream value after subscription") {
            val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
            val state = flowOf(42).stateInViewModel(scope, 0)
            state.first { it != 0 } shouldBe 42
            scope.cancel()
        }
    },
)
