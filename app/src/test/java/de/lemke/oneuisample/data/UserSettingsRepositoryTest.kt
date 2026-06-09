package de.lemke.oneuisample.data

import android.app.Application
import android.content.Context
import androidx.test.core.app.ApplicationProvider
import dev.oneuiproject.oneui.layout.ToolbarLayout.SearchOnActionMode
import io.kotest.matchers.shouldBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class UserSettingsRepositoryTest {
    private lateinit var repo: UserSettingsRepository

    @Before
    fun setup() {
        val prefs =
            ApplicationProvider
                .getApplicationContext<Application>()
                .getSharedPreferences("test_user_settings", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        repo = UserSettingsRepository(prefs)
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
    fun `flow reflects property write`() {
        repo.sampleSwitchBar = true
        repo.flow.value.sampleSwitchBar shouldBe true
    }
}
