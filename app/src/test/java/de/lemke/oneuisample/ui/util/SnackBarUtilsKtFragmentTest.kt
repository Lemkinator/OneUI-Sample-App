package de.lemke.oneuisample.ui.util

import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Looper
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.App
import de.lemke.oneuisample.R
import de.lemke.oneuisample.ui.MainActivity
import io.kotest.matchers.shouldNotBe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class, sdk = [36])
class SnackBarUtilsKtFragmentTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private val prefs get() = context.getSharedPreferences("user_settings", Context.MODE_PRIVATE)

    @Before
    fun setup() {
        prefs
            .edit()
            .putInt("lastVersionCode", Int.MAX_VALUE)
            .putInt("acceptedTosVersion", Int.MAX_VALUE)
            .commit()
    }

    private fun withFragment(block: (androidx.fragment.app.Fragment) -> Unit) {
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                val navHost = activity.supportFragmentManager.findFragmentById(R.id.navigationHost) as? NavHostFragment
                val fragment = navHost?.childFragmentManager?.fragments?.firstOrNull()
                if (fragment != null) block(fragment)
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun `Fragment suggestiveSnackBar String shows snackbar via requireActivity`() {
        withFragment { fragment ->
            val snackbar = fragment.suggestiveSnackBar("Test message")
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `Fragment suggestiveSnackBar String with actionText covers non-null let branch`() {
        withFragment { fragment ->
            val snackbar = fragment.suggestiveSnackBar("Test message", actionText = "Undo")
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `Fragment suggestiveSnackBar StringRes delegates to String overload`() {
        withFragment { fragment ->
            val snackbar = fragment.suggestiveSnackBar(R.string.app_name)
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `Fragment suggestiveSnackBar StringRes with actionText covers let branch`() {
        withFragment { fragment ->
            val snackbar = fragment.suggestiveSnackBar(R.string.ok, actionText = "Dismiss")
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `Fragment suggestiveSnackBar String with explicit action covers non-default path`() {
        withFragment { fragment ->
            val snackbar = fragment.suggestiveSnackBar("msg", actionText = "Act", action = { dismiss() })
            snackbar shouldNotBe null
        }
    }

    @Test
    fun `Fragment suggestiveSnackBar StringRes with explicit action covers non-default path`() {
        withFragment { fragment ->
            val snackbar = fragment.suggestiveSnackBar(R.string.app_name, actionText = "Ok", action = { dismiss() })
            snackbar shouldNotBe null
        }
    }
}
