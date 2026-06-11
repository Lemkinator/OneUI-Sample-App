package de.lemke.oneuisample.ui.util

import android.app.Application
import android.content.ActivityNotFoundException
import android.content.ContextWrapper
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class URLUtilsKtRobolectricTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `openURL returns false for null url`() {
        context.openURL(null) shouldBe false
    }

    @Test
    fun `openURL returns false for blank url`() {
        context.openURL("   ") shouldBe false
    }

    @Test
    fun `openURL returns true for valid url`() {
        context.openURL("https://example.com") shouldBe true
    }

    @Test
    fun `openURL catches ActivityNotFoundException and returns false`() {
        val ctx =
            object : ContextWrapper(context) {
                override fun startActivity(intent: Intent?): Unit = throw ActivityNotFoundException("no browser")
            }
        ctx.openURL("https://example.com") shouldBe false
        ShadowToast.getLatestToast() shouldNotBe null
    }

    @Test
    @Suppress("TooGenericExceptionThrown")
    fun `openURL catches generic Exception and returns false`() {
        val ctx =
            object : ContextWrapper(context) {
                override fun startActivity(intent: Intent?): Unit = throw RuntimeException("unexpected")
            }
        ctx.openURL("https://example.com") shouldBe false
        ShadowToast.getLatestToast() shouldNotBe null
    }
}
