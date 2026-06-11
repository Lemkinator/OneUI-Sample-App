package de.lemke.oneuisample.domain

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.data.UserSettingsRepository
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ObserveIconListUseCaseTest {
    private lateinit var prefs: SharedPreferences
    private lateinit var repo: UserSettingsRepository
    private lateinit var useCase: ObserveIconListUseCase

    @Before
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Application>()
        prefs = context.getSharedPreferences("test_observe_icons", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        repo = UserSettingsRepository(prefs)
        useCase = ObserveIconListUseCase(context, repo)
    }

    @Test
    fun `returns full icon list with null search when searchActive is false`() {
        runBlocking {
            val (icons, search) = useCase().first()
            icons shouldBe useCase.iconsId
            search shouldBe null
        }
    }

    @Test
    fun `returns full icon list with null search when searchActive is true but search is blank`() {
        runBlocking {
            repo.searchActive = true
            repo.search = "   "
            val (icons, search) = useCase().first()
            icons shouldBe useCase.iconsId
            search shouldBe null
        }
    }

    @Test
    fun `returns filtered list and search string when searchActive is true and search is non-blank`() {
        runBlocking {
            repo.searchActive = true
            repo.search = "star"
            val (icons, search) = useCase().first()
            search shouldBe "star"
            icons.all { it.containsKeywords(setOf("star")) } shouldBe true
        }
    }

    @Test
    fun `filtered list is a subset of the full icon list`() {
        runBlocking {
            repo.searchActive = true
            repo.search = "star"
            val (filtered, _) = useCase().first()
            filtered.all { useCase.iconsId.contains(it) } shouldBe true
        }
    }

    @Test
    fun `search splits on spaces into keywords`() {
        runBlocking {
            repo.searchActive = true
            repo.search = "star moon"
            val (icons, search) = useCase().first()
            search shouldBe "star moon"
            icons.all { it.containsKeywords(setOf("star", "moon")) } shouldBe true
        }
    }

    @Test
    fun `returns null search when searchActive becomes false`() {
        runBlocking {
            repo.searchActive = true
            repo.search = "star"
            repo.searchActive = false
            val (_, search) = useCase().first()
            search shouldBe null
        }
    }
}
