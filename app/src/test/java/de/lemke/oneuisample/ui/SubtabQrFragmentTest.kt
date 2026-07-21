/*
 * Copyright 2022-2026 Leonard Lemke
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.lemke.oneuisample.ui

import android.app.Application
import android.content.DialogInterface.BUTTON_NEUTRAL
import android.content.pm.PackageManager
import android.os.Looper
import android.view.MenuItem
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.R
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.ui.fragments.SubtabQrFragment
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowDialog

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SubtabQrFragmentTest {
    @get:Rule(order = 0)
    val hiltRule = HiltAndroidRule(this)

    private val context get() = ApplicationProvider.getApplicationContext<Application>()

    @Inject
    lateinit var userSettings: UserSettings

    @Before
    fun setup() {
        hiltRule.inject()
        userSettings.bypassOobe()
    }

    private fun withFragment(block: SubtabQrFragment.() -> Unit) = withDesignSubtabFragment(context, QR_SUBTAB_INDEX, block)

    @Test
    fun onQrScanResult_withDecodedString_showsDialog() {
        withFragment {
            onQrScanResult("https://github.com/tribalfs/oneui-design")
            ShadowDialog.getLatestDialog() shouldNotBe null
        }
    }

    @Test
    fun onQrScanResult_null_doesNothing() {
        withFragment { onQrScanResult(null) }
    }

    @Test
    fun onQrScanResult_fragmentDetached_doesNothing() {
        lateinit var fragmentRef: SubtabQrFragment
        withDesignSubtabFragment<SubtabQrFragment>(
            context,
            QR_SUBTAB_INDEX,
            block = { fragmentRef = this },
            afterBlock = { scenario ->
                scenario.moveToState(Lifecycle.State.DESTROYED)
                shadowOf(Looper.getMainLooper()).idle()
                fragmentRef.onQrScanResult("https://github.com/tribalfs/oneui-design")
                ShadowDialog.getLatestDialog() shouldBe null
            },
        )
    }

    @Test
    fun onQrScanResult_clickingShareButton_firesShareIntent() {
        withFragment {
            onQrScanResult("https://github.com/tribalfs/oneui-design")
            val dialog = ShadowDialog.getLatestDialog() as AlertDialog
            dialog.getButton(BUTTON_NEUTRAL).performClick()
            shadowOf(Looper.getMainLooper()).idle()
            shadowOf(requireActivity()).nextStartedActivity shouldNotBe null
        }
    }

    @Test
    fun hasCameraHardware_noCameraFeature_returnsFalse() {
        withFragment { hasCameraHardware() shouldBe false }
    }

    @Test
    fun hasCameraHardware_withCameraFeature_returnsTrue() {
        withFragment {
            shadowOf(requireContext().packageManager).setSystemFeature(PackageManager.FEATURE_CAMERA, true)
            hasCameraHardware() shouldBe true
        }
    }

    @Test
    fun onScanMenuItemSelected_noCameraFeature_showsSnackBar() {
        withFragment {
            val item = mockk<MenuItem> { every { itemId } returns R.id.menu_item_scan_qr }
            onScanMenuItemSelected(item) shouldBe true
        }
    }

    @Test
    fun onScanMenuItemSelected_unknownItem_returnsFalse() {
        withFragment {
            val item = mockk<MenuItem> { every { itemId } returns -1 }
            onScanMenuItemSelected(item) shouldBe false
        }
    }

    companion object {
        private const val QR_SUBTAB_INDEX = 2
    }
}
