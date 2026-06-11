package de.lemke.oneuisample.ui.util

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.App
import de.lemke.oneuisample.BuildConfig
import de.lemke.oneuisample.data.UserSettingsRepository
import de.lemke.oneuisample.ui.MainActivity
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class, sdk = [36])
class OnboardingUtilsKtTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private val prefs get() = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)

    @Before
    fun setup() {
        prefs.edit().clear().commit()
    }

    // firstInstall: lastVersionCode = -1 → shouldShowOOBE = true → startActivity(OOBE) + finishWithFade + return null
    @Test
    fun `onboardIfNeeded starts OOBEActivity and returns null on first install`() {
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use {
            shadowOf(Looper.getMainLooper()).idle()
        }
        // version NOT updated because onboardIfNeeded returned null early
        prefs.getInt("lastVersionCode", -1) shouldBe -1
    }

    // upgrade: 0 < lastVersionCode < versionCode → FIRST_TIME_VERSION → shouldShowOOBE = false → updates + returns AppStart
    @Test
    fun `onboardIfNeeded updates version and returns AppStart on upgrade`() {
        prefs
            .edit()
            .putInt("lastVersionCode", 0)
            .putInt("acceptedTosVersion", Int.MAX_VALUE)
            .commit()
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use {
            shadowOf(Looper.getMainLooper()).idle()
        }
        prefs.getInt("lastVersionCode", -1) shouldBe BuildConfig.VERSION_CODE
    }

    // else branch: lastVersionCode == versionCode → NORMAL (no Log.w) → updates + returns AppStart
    @Test
    fun `onboardIfNeeded returns AppStart on same version launch`() {
        prefs
            .edit()
            .putInt("lastVersionCode", BuildConfig.VERSION_CODE)
            .putInt("acceptedTosVersion", Int.MAX_VALUE)
            .commit()
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use {
            shadowOf(Looper.getMainLooper()).idle()
        }
        prefs.getInt("lastVersionCode", -1) shouldBe BuildConfig.VERSION_CODE
    }

    // allowSkip=true + no EXTRA_SKIP_ONBOARDING → getBooleanExtra=false → !(true&&false)=true → shouldShowOOBE=true → OOBE
    @Test
    fun `onboardIfNeeded redirects to OOBE when allowSkip is true but extra not in intent`() {
        prefs
            .edit()
            .putInt("lastVersionCode", Int.MAX_VALUE)
            .putInt("acceptedTosVersion", Int.MAX_VALUE)
            .commit()
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                val testPrefs = context.getSharedPreferences("test_allowskip_noextra", Context.MODE_PRIVATE)
                testPrefs.edit().clear().commit()
                val repo = UserSettingsRepository(testPrefs)
                // No EXTRA_SKIP_ONBOARDING in intent → getBooleanExtra=false
                // allowSkip=true && false = false → !(false)=true → shouldShowOOBE=true → OOBE
                val result = activity.onboardIfNeeded(repo, 1, "1.0", allowSkip = true)
                result shouldBe null
            }
        }
    }

    // no allowSkip arg → $default wrapper provides false → behaves same as allowSkip=false
    @Test
    fun `onboardIfNeeded default allowSkip is false and returns AppStart when no OOBE needed`() {
        prefs
            .edit()
            .putInt("lastVersionCode", Int.MAX_VALUE)
            .putInt("acceptedTosVersion", Int.MAX_VALUE)
            .commit()
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                val testPrefs = context.getSharedPreferences("test_default_allowskip", Context.MODE_PRIVATE)
                testPrefs
                    .edit()
                    .putInt("lastVersionCode", 1)
                    .putInt("acceptedTosVersion", Int.MAX_VALUE)
                    .commit()
                val repo = UserSettingsRepository(testPrefs)
                // omit allowSkip — exercises the $default synthetic wrapper (default = false)
                val result = activity.onboardIfNeeded(repo, 1, "1.0")
                result shouldNotBe null
                repo.lastVersionCode shouldBe 1
            }
        }
    }

    // allowSkip path: !(allowSkip=true && EXTRA_SKIP_ONBOARDING=true) = false → skip OOBE even when shouldShowOOBE=true
    @Test
    fun `onboardIfNeeded skips OOBE when allowSkip is true and intent has EXTRA_SKIP_ONBOARDING`() {
        prefs
            .edit()
            .putInt("lastVersionCode", Int.MAX_VALUE)
            .putInt("acceptedTosVersion", Int.MAX_VALUE)
            .commit()
        val intent = Intent(context, MainActivity::class.java).putExtra(EXTRA_SKIP_ONBOARDING, true)
        ActivityScenario.launch<MainActivity>(intent).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                val testPrefs = context.getSharedPreferences("test_skip_prefs", Context.MODE_PRIVATE)
                testPrefs.edit().clear().commit()
                val repo = UserSettingsRepository(testPrefs)
                // fresh repo: lastVersionCode = -1 → shouldShowOOBE = true
                // but allowSkip=true + intent has EXTRA_SKIP_ONBOARDING=true → condition is false → no OOBE
                val result = activity.onboardIfNeeded(repo, 1, "1.0", allowSkip = true)
                result shouldNotBe null
                repo.lastVersionCode shouldBe 1
            }
        }
    }
}
