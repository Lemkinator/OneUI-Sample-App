package de.lemke.oneuisample.ui

import android.content.Intent
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import io.kotest.matchers.shouldBe
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@HiltAndroidTest
@LargeTest
@RunWith(AndroidJUnit4::class)
class MainActivityTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Test
    fun activityLaunchesWithoutCrash() {
        ActivityScenario
            .launch<MainActivity>(
                Intent(ApplicationProvider.getApplicationContext(), MainActivity::class.java),
            ).use { scenario ->
                scenario.state.isAtLeast(Lifecycle.State.CREATED) shouldBe true
            }
    }
}
