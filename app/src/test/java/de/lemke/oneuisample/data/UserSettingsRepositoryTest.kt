package de.lemke.oneuisample.data

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import app.cash.turbine.test
import dev.oneuiproject.oneui.layout.ToolbarLayout.SearchOnActionMode
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class UserSettingsRepositoryTest {
    private lateinit var testScope: TestScope
    private lateinit var prefs: SharedPreferences
    private lateinit var repo: UserSettingsRepository

    @Before
    fun setup() {
        testScope = TestScope()
        prefs =
            ApplicationProvider
                .getApplicationContext<Application>()
                .getSharedPreferences("test_user_settings", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        repo = UserSettingsRepository(prefs, testScope)
    }

    @After
    fun tearDown() {
        testScope.cancel()
    }

    @Test
    fun `returns defaults on fresh store`() {
        repo.darkMode shouldBe false
        repo.autoDarkMode shouldBe true
        repo.lastVersionCode shouldBe -1
        repo.lastVersionName shouldBe "0.0"
        repo.acceptedTosVersion shouldBe -1
        repo.devModeEnabled shouldBe false
        repo.appPickerType shouldBe 0
        repo.sampleSwitchBar shouldBe false
    }

    @Test
    fun `darkMode round-trip`() {
        repo.darkMode = true
        repo.darkMode shouldBe true
        repo.darkMode = false
        repo.darkMode shouldBe false
    }

    @Test
    fun `autoDarkMode round-trip`() {
        repo.autoDarkMode = false
        repo.autoDarkMode shouldBe false
    }

    @Test
    fun `lastVersionCode round-trip`() {
        repo.lastVersionCode = 42
        repo.lastVersionCode shouldBe 42
    }

    @Test
    fun `acceptedTosVersion round-trip`() {
        repo.acceptedTosVersion = 3
        repo.acceptedTosVersion shouldBe 3
    }

    @Test
    fun `devModeEnabled round-trip`() {
        repo.devModeEnabled = true
        repo.devModeEnabled shouldBe true
    }

    @Test
    fun `sampleSwitchBar round-trip`() {
        repo.sampleSwitchBar = true
        repo.sampleSwitchBar shouldBe true
    }

    @Test
    fun `searchOnActionMode round-trip for NoDismiss`() {
        repo.searchOnActionMode = SearchOnActionMode.NoDismiss
        repo.searchOnActionMode shouldBe SearchOnActionMode.NoDismiss
    }

    @Test
    fun `searchOnActionMode round-trip for Dismiss`() {
        repo.searchOnActionMode = SearchOnActionMode.Dismiss
        repo.searchOnActionMode shouldBe SearchOnActionMode.Dismiss
    }

    @Test
    fun `update applies transform atomically`() {
        repo.update { copy(devModeEnabled = true, sampleSwitchBar = true) }
        repo.devModeEnabled shouldBe true
        repo.sampleSwitchBar shouldBe true
    }

    @Test
    fun `update does not touch unchanged fields`() {
        repo.lastVersionCode = 10
        repo.update { copy(devModeEnabled = true) }
        repo.lastVersionCode shouldBe 10
    }

    @Test
    fun `flow emits snapshot on creation`() {
        val snapshot = repo.flow.value
        snapshot.darkMode shouldBe false
        snapshot.autoDarkMode shouldBe true
    }

    @Test
    fun `flow reflects property write`() =
        testScope.runTest {
            repo.flow.test {
                awaitItem() // consume initial snapshot
                repo.sampleSwitchBar = true
                awaitItem().sampleSwitchBar shouldBe true
            }
        }

    @Test
    fun `withListener returns Concurrent with new listener when mode is Concurrent`() {
        SearchOnActionMode.Concurrent(null).withListener(null) shouldBe SearchOnActionMode.Concurrent(null)
    }

    @Test
    fun `withListener returns Dismiss unchanged`() {
        SearchOnActionMode.Dismiss.withListener(null) shouldBe SearchOnActionMode.Dismiss
    }

    @Test
    fun `withListener returns NoDismiss unchanged`() {
        SearchOnActionMode.NoDismiss.withListener(null) shouldBe SearchOnActionMode.NoDismiss
    }

    @Test
    fun `lastVersionName round-trip`() {
        repo.lastVersionName = "2.5"
        repo.lastVersionName shouldBe "2.5"
    }

    @Test
    fun `appPickerType round-trip`() {
        repo.appPickerType = 2
        repo.appPickerType shouldBe 2
    }

    @Test
    fun `showIndexScroll round-trip`() {
        repo.showIndexScroll = false
        repo.showIndexScroll shouldBe false
    }

    @Test
    fun `indexScrollShowLetters round-trip`() {
        repo.indexScrollShowLetters = false
        repo.indexScrollShowLetters shouldBe false
    }

    @Test
    fun `indexScrollAutoHide round-trip`() {
        repo.indexScrollAutoHide = false
        repo.indexScrollAutoHide shouldBe false
    }

    @Test
    fun `actionModeShowCancel round-trip`() {
        repo.actionModeShowCancel = true
        repo.actionModeShowCancel shouldBe true
    }

    @Test
    fun `search round-trip`() {
        repo.search = "hello"
        repo.search shouldBe "hello"
    }

    @Test
    fun `searchActive round-trip`() {
        repo.searchActive = true
        repo.searchActive shouldBe true
    }

    @Test
    fun `searchOnActionMode round-trip for Concurrent`() {
        repo.searchOnActionMode = SearchOnActionMode.Concurrent(null)
        repo.searchOnActionMode shouldBe SearchOnActionMode.Concurrent(null)
    }

    @Test
    fun `update applies all fields`() {
        repo.update {
            copy(
                darkMode = true,
                autoDarkMode = false,
                lastVersionCode = 100,
                lastVersionName = "5.0",
                acceptedTosVersion = 5,
                devModeEnabled = true,
                appPickerType = 3,
                sampleSwitchBar = true,
                showIndexScroll = false,
                indexScrollShowLetters = false,
                indexScrollAutoHide = false,
                actionModeShowCancel = true,
                searchOnActionMode = SearchOnActionMode.NoDismiss,
                search = "test",
                searchActive = true,
            )
        }
        repo.darkMode shouldBe true
        repo.autoDarkMode shouldBe false
        repo.lastVersionCode shouldBe 100
        repo.lastVersionName shouldBe "5.0"
        repo.acceptedTosVersion shouldBe 5
        repo.devModeEnabled shouldBe true
        repo.appPickerType shouldBe 3
        repo.sampleSwitchBar shouldBe true
        repo.showIndexScroll shouldBe false
        repo.indexScrollShowLetters shouldBe false
        repo.indexScrollAutoHide shouldBe false
        repo.actionModeShowCancel shouldBe true
        repo.searchOnActionMode shouldBe SearchOnActionMode.NoDismiss
        repo.search shouldBe "test"
        repo.searchActive shouldBe true
    }
}
