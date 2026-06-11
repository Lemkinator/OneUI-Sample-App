package de.lemke.oneuisample.data

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
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
class SharedPreferenceDelegatesTest {
    private lateinit var prefs: SharedPreferences
    private lateinit var delegates: SharedPreferenceDelegates

    @Before
    fun setup() {
        prefs =
            ApplicationProvider
                .getApplicationContext<Application>()
                .getSharedPreferences("test_delegates", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
        delegates = SharedPreferenceDelegates(prefs)
    }

    // float

    @Test
    fun `float default returns 0f`() {
        val h =
            object {
                var f: Float by delegates.float(0f)
            }
        h.f shouldBe 0f
    }

    @Test
    fun `float custom default returned before write`() {
        val h =
            object {
                var f: Float by delegates.float(1.5f)
            }
        h.f shouldBe 1.5f
    }

    @Test
    fun `float round-trip`() {
        val h =
            object {
                var f: Float by delegates.float(0f)
            }
        h.f = 3.14f
        h.f shouldBe 3.14f
    }

    // long

    @Test
    fun `long default returns 0L`() {
        val h =
            object {
                var l: Long by delegates.long(0L)
            }
        h.l shouldBe 0L
    }

    @Test
    fun `long round-trip`() {
        val h =
            object {
                var l: Long by delegates.long(0L)
            }
        h.l = 9_999_999_999L
        h.l shouldBe 9_999_999_999L
    }

    // stringSet

    @Test
    fun `stringSet default returns empty set`() {
        val h =
            object {
                var ss: Set<String> by delegates.stringSet()
            }
        h.ss shouldBe emptySet()
    }

    @Test
    fun `stringSet round-trip`() {
        val h =
            object {
                var ss: Set<String> by delegates.stringSet()
            }
        h.ss = setOf("a", "b", "c")
        h.ss shouldBe setOf("a", "b", "c")
    }

    @Test
    fun `stringSet empty set round-trip`() {
        val h =
            object {
                var ss: Set<String> by delegates.stringSet(default = setOf("default"))
            }
        h.ss = emptySet()
        h.ss shouldBe emptySet()
    }

    // darkMode

    @Test
    fun `darkMode default returns false`() {
        val h =
            object {
                var dm: Boolean by delegates.darkMode(false)
            }
        h.dm shouldBe false
    }

    @Test
    fun `darkMode true round-trip`() {
        val h =
            object {
                var dm: Boolean by delegates.darkMode(false)
            }
        h.dm = true
        h.dm shouldBe true
    }

    @Test
    fun `darkMode false round-trip`() {
        val h =
            object {
                var dm: Boolean by delegates.darkMode(true)
            }
        h.dm = false
        h.dm shouldBe false
    }

    @Test
    fun `darkMode stores as string 1 or 0 rather than boolean`() {
        val h =
            object {
                var dm: Boolean by delegates.darkMode(false)
            }
        h.dm = true
        prefs.getString("dm", null) shouldBe "1"
        h.dm = false
        prefs.getString("dm", null) shouldBe "0"
    }

    // searchOnActionMode

    @Test
    fun `searchOnActionMode default is Dismiss`() {
        val h =
            object {
                var mode: SearchOnActionMode by delegates.searchOnActionMode()
            }
        h.mode shouldBe SearchOnActionMode.Dismiss
    }

    @Test
    fun `searchOnActionMode round-trip NoDismiss`() {
        val h =
            object {
                var mode: SearchOnActionMode by delegates.searchOnActionMode()
            }
        h.mode = SearchOnActionMode.NoDismiss
        h.mode shouldBe SearchOnActionMode.NoDismiss
    }

    @Test
    fun `searchOnActionMode round-trip Concurrent`() {
        val h =
            object {
                var mode: SearchOnActionMode by delegates.searchOnActionMode()
            }
        h.mode = SearchOnActionMode.Concurrent(null)
        h.mode shouldBe SearchOnActionMode.Concurrent(null)
    }

    @Test
    fun `searchOnActionMode round-trip Dismiss after change`() {
        val h =
            object {
                var mode: SearchOnActionMode by delegates.searchOnActionMode()
            }
        h.mode = SearchOnActionMode.NoDismiss
        h.mode = SearchOnActionMode.Dismiss
        h.mode shouldBe SearchOnActionMode.Dismiss
    }

    // custom key

    @Test
    fun `custom key overrides property name for storage`() {
        val h =
            object {
                var a: Boolean by delegates.boolean(false, key = "sharedKey")
                var b: Boolean by delegates.boolean(true, key = "sharedKey")
            }
        h.a = true
        h.b shouldBe true
    }

    @Test
    fun `property name used as default key keeps fields independent`() {
        val h =
            object {
                var alpha: Boolean by delegates.boolean(false)
                var beta: Boolean by delegates.boolean(false)
            }
        h.alpha = true
        h.beta shouldBe false
    }

    // no-arg factory calls exercise the Kotlin $default synthetic wrapper's "use default for first param" branch

    @Test
    fun `float no-arg factory uses 0f default`() {
        val h =
            object {
                var f: Float by delegates.float()
            }
        h.f shouldBe 0f
    }

    @Test
    fun `long no-arg factory uses 0L default`() {
        val h =
            object {
                var l: Long by delegates.long()
            }
        h.l shouldBe 0L
    }

    @Test
    fun `boolean no-arg factory uses false default`() {
        val h =
            object {
                var b: Boolean by delegates.boolean()
            }
        h.b shouldBe false
    }

    @Test
    fun `int no-arg factory uses 0 default`() {
        val h =
            object {
                var i: Int by delegates.int()
            }
        h.i shouldBe 0
    }

    @Test
    fun `string no-arg factory uses empty string default`() {
        val h =
            object {
                var s: String by delegates.string()
            }
        h.s shouldBe ""
    }

    @Test
    fun `darkMode no-arg factory uses false default`() {
        val h =
            object {
                var dm: Boolean by delegates.darkMode()
            }
        h.dm shouldBe false
    }
}
