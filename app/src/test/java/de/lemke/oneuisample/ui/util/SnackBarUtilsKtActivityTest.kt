package de.lemke.oneuisample.ui.util

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.view.View
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.App
import de.lemke.oneuisample.R
import de.lemke.oneuisample.bypassOobe
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
import com.google.android.material.R as MaterialR

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class, sdk = [36])
class SnackBarUtilsKtActivityTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private val prefs get() = context.getSharedPreferences(UserSettingsRepository.PREFS_NAME, Context.MODE_PRIVATE)

    @Before
    fun setup() {
        prefs.bypassOobe()
    }

    private fun withActivity(block: (MainActivity) -> Unit) {
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity(block)
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun `suggestiveSnackBar String shows snackbar with default args`() {
        withActivity { activity ->
            val snackbar = activity.suggestiveSnackBar("Test message")
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `suggestiveSnackBar String with actionText covers non-null let branch`() {
        withActivity { activity ->
            val snackbar = activity.suggestiveSnackBar("Test message", actionText = "Undo")
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `suggestiveSnackBar String with explicit view and duration`() {
        withActivity { activity ->
            val view = activity.window.decorView
            val snackbar = activity.suggestiveSnackBar("Test message", view = view, duration = 5000)
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `suggestiveSnackBar StringRes delegates to String overload`() {
        withActivity { activity ->
            val snackbar = activity.suggestiveSnackBar(R.string.app_name)
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `suggestiveSnackBar StringRes with actionText and explicit view`() {
        withActivity { activity ->
            val view = activity.window.decorView
            val snackbar = activity.suggestiveSnackBar(R.string.ok, view = view, actionText = "Dismiss")
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `suggestiveSnackBar String with explicit action covers non-default action path`() {
        withActivity { activity ->
            val snackbar = activity.suggestiveSnackBar("msg", actionText = "Act", action = { })
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `suggestiveSnackBar StringRes with explicit action covers non-default action path`() {
        withActivity { activity ->
            val snackbar = activity.suggestiveSnackBar(R.string.app_name, actionText = "Ok", action = { dismiss() })
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `suggestiveSnackBar String action button click invokes custom action`() {
        withActivity { activity ->
            var actionCalled = false
            val snackbar = activity.suggestiveSnackBar("msg", actionText = "Act", action = { actionCalled = true })
            shadowOf(Looper.getMainLooper()).idle()
            val actionButton = snackbar.view.findViewById<View>(MaterialR.id.snackbar_action)
            actionButton shouldNotBe null
            actionButton!!.performClick()
            shadowOf(Looper.getMainLooper()).idle()
            actionCalled shouldBe true
        }
    }

    @Test
    fun `suggestiveSnackBar String action button click invokes default dismiss action`() {
        withActivity { activity ->
            val snackbar = activity.suggestiveSnackBar("msg", actionText = "Dismiss")
            shadowOf(Looper.getMainLooper()).idle()
            val actionButton = snackbar.view.findViewById<View>(MaterialR.id.snackbar_action)
            actionButton shouldNotBe null
            actionButton!!.performClick()
            shadowOf(Looper.getMainLooper()).idle()
            snackbar.isShown shouldBe false
        }
    }

    @Test
    fun `suggestiveSnackBar StringRes action button click invokes default dismiss action`() {
        withActivity { activity ->
            val snackbar = activity.suggestiveSnackBar(R.string.ok, actionText = "Dismiss")
            shadowOf(Looper.getMainLooper()).idle()
            val actionButton = snackbar.view.findViewById<View>(MaterialR.id.snackbar_action)
            actionButton shouldNotBe null
            actionButton!!.performClick()
            shadowOf(Looper.getMainLooper()).idle()
            snackbar.isShown shouldBe false
        }
    }
}
