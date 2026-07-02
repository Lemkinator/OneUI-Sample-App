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
import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.appcompat.widget.SearchView
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.R
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.data.UserSettingsRepository
import de.lemke.oneuisample.ui.MainActivity
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [36])
class SearchUtilsKtTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private val prefs get() = context.getSharedPreferences(UserSettingsRepository.PREFS_NAME, Context.MODE_PRIVATE)
    private lateinit var testScope: TestScope

    @Before
    fun setup() {
        testScope = TestScope(UnconfinedTestDispatcher())
        prefs.bypassOobe()
    }

    @After
    fun tearDown() {
        testScope.cancel()
    }

    private fun withFragmentAndActivity(block: (androidx.fragment.app.Fragment, MainActivity) -> Unit) {
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                val navHost = activity.supportFragmentManager.findFragmentById(R.id.navigationHost) as? NavHostFragment
                val fragment =
                    checkNotNull(navHost?.childFragmentManager?.fragments?.firstOrNull()) {
                        "NavHostFragment contained no fragments"
                    }
                block(fragment, activity)
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    private fun withFragment(block: (androidx.fragment.app.Fragment) -> Unit) = withFragmentAndActivity { fragment, _ -> block(fragment) }

    @Test
    fun `getSearchListener returns non-null listener`() {
        withFragment { fragment ->
            val testPrefs = context.getSharedPreferences("test_search_utils", Context.MODE_PRIVATE)
            testPrefs.edit().clear().commit()
            val repo = UserSettingsRepository(testPrefs, testScope)
            val listener = fragment.getSearchListener(repo)
            listener shouldNotBe null
        }
    }

    @Test
    fun `onQueryTextChange returns false when searchActive is false`() {
        withFragment { fragment ->
            val testPrefs = context.getSharedPreferences("test_search_change", Context.MODE_PRIVATE)
            testPrefs.edit().clear().commit()
            val repo = UserSettingsRepository(testPrefs, testScope)
            repo.searchActive = false
            val listener = fragment.getSearchListener(repo)
            listener.onQueryTextChange("hello") shouldBe false
        }
    }

    @Test
    fun `onQueryTextChange returns true and updates search when searchActive is true`() {
        withFragment { fragment ->
            val testPrefs = context.getSharedPreferences("test_search_active", Context.MODE_PRIVATE)
            testPrefs.edit().clear().commit()
            val repo = UserSettingsRepository(testPrefs, testScope)
            repo.searchActive = true
            val listener = fragment.getSearchListener(repo)
            listener.onQueryTextChange("hello") shouldBe true
            repo.search shouldBe "hello"
        }
    }

    @Test
    fun `onQueryTextChange with null query stores empty string`() {
        withFragment { fragment ->
            val testPrefs = context.getSharedPreferences("test_search_null", Context.MODE_PRIVATE)
            testPrefs.edit().clear().commit()
            val repo = UserSettingsRepository(testPrefs, testScope)
            repo.searchActive = true
            val listener = fragment.getSearchListener(repo)
            listener.onQueryTextChange(null) shouldBe true
            repo.search shouldBe ""
        }
    }

    @Test
    fun `onSearchModeToggle sets searchActive and restores search query when activating`() {
        withFragmentAndActivity { fragment, activity ->
            val testPrefs = context.getSharedPreferences("test_search_toggle", Context.MODE_PRIVATE)
            testPrefs.edit().clear().commit()
            val repo = UserSettingsRepository(testPrefs, testScope)
            repo.search = "prev"
            val searchView = SearchView(activity)
            val listener = fragment.getSearchListener(repo)
            listener.onSearchModeToggle(searchView, true)
            repo.searchActive shouldBe true
            searchView.query.toString() shouldBe "prev"
        }
    }

    @Test
    fun `onSearchModeToggle sets searchActive false when deactivating`() {
        withFragmentAndActivity { fragment, activity ->
            val testPrefs = context.getSharedPreferences("test_search_deactivate", Context.MODE_PRIVATE)
            testPrefs.edit().clear().commit()
            val repo = UserSettingsRepository(testPrefs, testScope)
            repo.searchActive = true
            val searchView = SearchView(activity)
            val listener = fragment.getSearchListener(repo)
            listener.onSearchModeToggle(searchView, false)
            repo.searchActive shouldBe false
        }
    }

    @Test
    fun `onQueryTextSubmit returns true and updates search when searchActive is true`() {
        withFragment { fragment ->
            val testPrefs = context.getSharedPreferences("test_search_submit", Context.MODE_PRIVATE)
            testPrefs.edit().clear().commit()
            val repo = UserSettingsRepository(testPrefs, testScope)
            repo.searchActive = true
            val listener = fragment.getSearchListener(repo)
            listener.onQueryTextSubmit("submitted") shouldBe true
            repo.search shouldBe "submitted"
        }
    }

    @Test
    fun `onQueryTextSubmit returns false when searchActive is false`() {
        withFragment { fragment ->
            val testPrefs = context.getSharedPreferences("test_search_submit_inactive", Context.MODE_PRIVATE)
            testPrefs.edit().clear().commit()
            val repo = UserSettingsRepository(testPrefs, testScope)
            repo.searchActive = false
            val listener = fragment.getSearchListener(repo)
            listener.onQueryTextSubmit("hello") shouldBe false
        }
    }

    @Test
    fun `onSearchModeToggle sets query hint when queryHint is provided`() {
        withFragmentAndActivity { fragment, activity ->
            val testPrefs = context.getSharedPreferences("test_search_hint", Context.MODE_PRIVATE)
            testPrefs.edit().clear().commit()
            val repo = UserSettingsRepository(testPrefs, testScope)
            val searchView = SearchView(activity)
            val listener = fragment.getSearchListener(repo, queryHint = R.string.app_name)
            listener.onSearchModeToggle(searchView, true)
            searchView.queryHint shouldBe context.getString(R.string.app_name)
        }
    }
}
