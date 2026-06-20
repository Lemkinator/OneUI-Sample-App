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
package de.lemke.oneuisample.domain

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.data.UserSettingsRepository
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ObserveIconListUseCaseTest {
    private lateinit var testScope: TestScope
    private lateinit var prefs: SharedPreferences
    private lateinit var repo: UserSettingsRepository
    private lateinit var useCase: ObserveIconListUseCase

    @Before
    fun setup() {
        testScope = TestScope(UnconfinedTestDispatcher())
        val context = ApplicationProvider.getApplicationContext<Application>()
        prefs = context.getSharedPreferences("test_observe_icons", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        repo = UserSettingsRepository(prefs, testScope.backgroundScope)
        useCase = ObserveIconListUseCase(context, repo)
    }

    @After
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun `returns full icon list with null search when searchActive is false`() =
        testScope.runTest {
            val (icons, search) = useCase().first()
            icons shouldBe useCase.iconsId
            search shouldBe null
        }

    @Test
    fun `returns full icon list with null search when searchActive is true but search is blank`() =
        testScope.runTest {
            repo.searchActive = true
            repo.search = "   "
            val (icons, search) = useCase().first()
            icons shouldBe useCase.iconsId
            search shouldBe null
        }

    @Test
    fun `returns filtered list and search string when searchActive is true and search is non-blank`() =
        testScope.runTest {
            repo.searchActive = true
            repo.search = "star"
            val (icons, search) = useCase().first()
            search shouldBe "star"
            icons.all { it.containsKeywords(setOf("star")) } shouldBe true
        }

    @Test
    fun `filtered list is a subset of the full icon list`() =
        testScope.runTest {
            repo.searchActive = true
            repo.search = "star"
            val (filtered, _) = useCase().first()
            filtered.all { useCase.iconsId.contains(it) } shouldBe true
        }

    @Test
    fun `search splits on spaces into keywords`() =
        testScope.runTest {
            repo.searchActive = true
            repo.search = "star moon"
            val (icons, search) = useCase().first()
            search shouldBe "star moon"
            icons.isNotEmpty() shouldBe true
            icons.all { it.containsKeywords(setOf("star", "moon")) } shouldBe true
        }

    @Test
    fun `returns null search when searchActive becomes false`() =
        testScope.runTest {
            repo.searchActive = true
            repo.search = "star"
            repo.searchActive = false
            val (_, search) = useCase().first()
            search shouldBe null
        }
}
