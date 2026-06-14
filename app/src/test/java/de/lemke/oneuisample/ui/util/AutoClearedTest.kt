package de.lemke.oneuisample.ui.util

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.App
import de.lemke.oneuisample.R
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.data.UserSettingsRepository
import de.lemke.oneuisample.ui.MainActivity
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import kotlin.reflect.KProperty
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class, sdk = [36])
class AutoClearedTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private val prefs get() = context.getSharedPreferences(UserSettingsRepository.PREFS_NAME, Context.MODE_PRIVATE)

    @Before
    fun setup() {
        prefs.bypassOobe()
    }

    private fun withFragment(block: (Fragment) -> Unit) {
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                val navHost = activity.supportFragmentManager.findFragmentById(R.id.navigationHost) as? NavHostFragment
                val fragment =
                    checkNotNull(navHost?.childFragmentManager?.fragments?.firstOrNull()) {
                        "NavHostFragment contained no fragments"
                    }
                block(fragment)
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun `getValue returns cached value on second call without reinitializing`() {
        withFragment { fragment ->
            var initCount = 0
            val delegate = fragment.autoCleared { initCount++ }
            val prop = mockk<KProperty<*>>()
            delegate.getValue(fragment, prop)
            delegate.getValue(fragment, prop)
            initCount shouldBe 1
        }
    }

    @Test
    fun `getValue skips caching and reinitializes when fragment has no view`() {
        val fragment = Fragment()
        var initCount = 0
        val delegate = fragment.autoCleared { initCount++ }
        val prop = mockk<KProperty<*>>()
        delegate.getValue(fragment, prop)
        delegate.getValue(fragment, prop)
        initCount shouldBe 2
    }

    @Test
    fun `getValue skips caching when viewLifecycleOwner lifecycle is DESTROYED`() {
        val lifecycle = mockk<Lifecycle>()
        every { lifecycle.currentState } returns Lifecycle.State.DESTROYED
        val lifecycleOwner = mockk<LifecycleOwner>()
        every { lifecycleOwner.lifecycle } returns lifecycle
        val fragment = mockk<Fragment>()
        every { fragment.view } returns mockk<View>()
        every { fragment.viewLifecycleOwner } returns lifecycleOwner
        var initCount = 0
        val delegate = fragment.autoCleared { initCount++ }
        val prop = mockk<KProperty<*>>()
        delegate.getValue(fragment, prop)
        delegate.getValue(fragment, prop)
        initCount shouldBe 2
    }
}
