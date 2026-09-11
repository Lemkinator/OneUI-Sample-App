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
package de.lemke.oneuisample

import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.data.UserSettings
import javax.inject.Inject
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Guards against Hilt's KSP module aggregation silently dropping a @TestInstallIn module
 * declared in src/testFixtures for the Robolectric (src/test) side, leaving the production
 * PersistenceModule active undetected. Keep this test even though it currently passes - a
 * silent regression here produces no other failing test.
 */
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [36])
class TestFixturesModuleInstallationTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var settings: UserSettings

    @Before
    fun setUp() {
        hiltRule.inject()
    }

    @Test
    fun `injected settings do not write through to production SharedPreferences`() {
        val productionPrefs = PreferenceManager.getDefaultSharedPreferences(ApplicationProvider.getApplicationContext())
        productionPrefs.edit().remove("devModeEnabled").commit()
        settings.devModeEnabled = true
        assertFalse(
            "devModeEnabled leaked into production SharedPreferences - TestSettingsModule (src/testFixtures) was " +
                "NOT installed; production module won instead",
            productionPrefs.contains("devModeEnabled"),
        )
    }
}
