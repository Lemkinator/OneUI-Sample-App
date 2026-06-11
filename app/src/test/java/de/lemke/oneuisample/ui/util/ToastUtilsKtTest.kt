package de.lemke.oneuisample.ui.util

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.R
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ToastUtilsKtTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()

    @Test
    fun `Context toast with string shows the message`() {
        context.toast("hello toast")
        ShadowToast.getTextOfLatestToast() shouldBe "hello toast"
    }

    @Test
    fun `Context toast with string res shows a toast`() {
        context.toast(R.string.ok)
        ShadowToast.getLatestToast() shouldNotBe null
    }
}
