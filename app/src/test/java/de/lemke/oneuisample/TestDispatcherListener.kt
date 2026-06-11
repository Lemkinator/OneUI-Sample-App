package de.lemke.oneuisample

import io.kotest.core.listeners.AfterTestListener
import io.kotest.core.listeners.BeforeTestListener
import io.kotest.core.test.TestCase
import io.kotest.engine.test.TestResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain

@OptIn(ExperimentalCoroutinesApi::class)
class TestDispatcherListener : BeforeTestListener, AfterTestListener {
    companion object {
        var scheduler: TestCoroutineScheduler = TestCoroutineScheduler()
            private set
    }

    override suspend fun beforeTest(testCase: TestCase) {
        scheduler = TestCoroutineScheduler()
        Dispatchers.setMain(UnconfinedTestDispatcher(scheduler))
    }

    override suspend fun afterTest(
        testCase: TestCase,
        result: TestResult,
    ) {
        Dispatchers.resetMain()
    }
}
