package de.lemke.oneuisample.domain

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.data.UserSettingsRepository
import io.kotest.matchers.shouldBe
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
        repo = UserSettingsRepository(prefs, testScope)
        useCase = ObserveIconListUseCase(context, repo)
    }

    @After
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun `returns full icon list with null search when searchActive is false`() {
        runTest {
            val (icons, search) = useCase().first()
            icons shouldBe useCase.iconsId
            search shouldBe null
        }
    }

    @Test
    fun `returns full icon list with null search when searchActive is true but search is blank`() {
        runTest {
            repo.searchActive = true
            repo.search = "   "
            val (icons, search) = useCase().first()
            icons shouldBe useCase.iconsId
            search shouldBe null
        }
    }

    @Test
    fun `returns filtered list and search string when searchActive is true and search is non-blank`() {
        runTest {
            repo.searchActive = true
            repo.search = "star"
            val (icons, search) = useCase().first()
            search shouldBe "star"
            icons.all { it.containsKeywords(setOf("star")) } shouldBe true
        }
    }

    @Test
    fun `filtered list is a subset of the full icon list`() {
        runTest {
            repo.searchActive = true
            repo.search = "star"
            val (filtered, _) = useCase().first()
            filtered.all { useCase.iconsId.contains(it) } shouldBe true
        }
    }

    @Test
    fun `search splits on spaces into keywords`() {
        runTest {
            repo.searchActive = true
            repo.search = "star moon"
            val (icons, search) = useCase().first()
            search shouldBe "star moon"
            icons.isNotEmpty() shouldBe true
            icons.all { it.containsKeywords(setOf("star", "moon")) } shouldBe true
        }
    }

    @Test
    fun `returns null search when searchActive becomes false`() {
        runTest {
            repo.searchActive = true
            repo.search = "star"
            repo.searchActive = false
            val (_, search) = useCase().first()
            search shouldBe null
        }
    }
}
