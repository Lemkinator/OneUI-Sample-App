package de.lemke.oneuisample

import io.kotest.core.listeners.AfterTestListener
import io.kotest.core.listeners.BeforeTestListener
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class TestDispatcherListener : BeforeTestListener, AfterTestListener {
    override suspend fun beforeTest(testCase: TestCase) {
        Dispatchers.setMain(UnconfinedTestDispatcher())
    }

    override suspend fun afterTest(
        testCase: TestCase,
        result: TestResult,
    ) {
        Dispatchers.resetMain()
    }
}
