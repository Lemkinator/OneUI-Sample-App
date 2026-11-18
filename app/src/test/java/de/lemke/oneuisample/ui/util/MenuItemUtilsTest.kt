/*
 * Copyright 2022-2026 Leonard Lemke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.lemke.oneuisample.ui.util

import android.app.Application
import android.content.Intent
import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.R
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.ui.MainActivity
import dev.oneuiproject.oneui.navigation.widget.DrawerNavigationView
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class MenuItemUtilsTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var userSettings: UserSettings

    private val context get() = ApplicationProvider.getApplicationContext<Application>()

    @Before
    fun setup() {
        hiltRule.inject()
        userSettings.bypassOobe()
    }

    private fun withDrawer(block: (DrawerNavigationView) -> Unit) {
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { block(it.findViewById(R.id.navigationView)) }
        }
    }

    @Test
    fun requireMenuItem_idInMenu_returnsItem() {
        withDrawer { it.requireMenuItem(R.id.popup_menu).itemId shouldBe R.id.popup_menu }
    }

    @Test
    fun requireMenuItem_idNotInMenu_throwsNamingId() {
        withDrawer { drawer ->
            shouldThrow<IllegalStateException> { drawer.requireMenuItem(R.id.navigationView) }
                .message shouldBe "Menu item ${context.packageName}:id/navigationView is missing from the drawer menu"
        }
    }

    @Test
    fun requireMenuItem_idWithoutResourceName_throwsNamingRawId() {
        withDrawer { drawer ->
            shouldThrow<IllegalStateException> { drawer.requireMenuItem(12345) }
                .message shouldBe "Menu item 12345 is missing from the drawer menu"
        }
    }
}
