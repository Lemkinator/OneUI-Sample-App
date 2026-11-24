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

import android.app.Activity
import android.app.Application
import android.content.DialogInterface.BUTTON_NEUTRAL
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Looper
import android.view.Window
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.test.core.app.ApplicationProvider
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import de.lemke.oneuisample.HiltTestRule
import de.lemke.oneuisample.R
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.ui.fragments.SubtabQrFragment
import dev.oneuiproject.oneui.qr.app.QrScanActivity
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import javax.inject.Inject
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.fakes.RoboMenuItem
import org.robolectric.shadows.ShadowDialog
import com.google.android.material.R as MaterialR

@HiltAndroidTest
@RunWith(RobolectricTestRunner::class)
@Config(application = HiltTestApplication::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class SubtabQrFragmentTest {
    @get:Rule(order = 0)
    val hiltRule = HiltTestRule(this)

    private val context get() = ApplicationProvider.getApplicationContext<Application>()

    @Inject
    lateinit var userSettings: UserSettings

    @Before
    fun setup() {
        hiltRule.inject()
        userSettings.bypassOobe()
    }

    private fun withFragment(block: SubtabQrFragment.() -> Unit) = withDesignSubtabFragment(context, QR_SUBTAB_INDEX, block)

    private fun SubtabQrFragment.selectMenuItem(itemId: Int): Boolean =
        requireActivity().onMenuItemSelected(Window.FEATURE_OPTIONS_PANEL, RoboMenuItem(itemId))

    private fun SubtabQrFragment.allowCamera() =
        shadowOf(requireContext().packageManager).setSystemFeature(PackageManager.FEATURE_CAMERA, true)

    private fun SubtabQrFragment.scanResult(
        resultCode: Int,
        qrContent: String?,
    ) {
        val activity = shadowOf(requireActivity())
        val request = activity.nextStartedActivityForResult.intent
        request.component?.className shouldBe QrScanActivity::class.java.name
        activity.receiveResult(request, resultCode, Intent().putExtra(QrScanActivity.EXTRA_QR_SCANNER_RESULT, qrContent))
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun scanMenuItem_noCamera_showsSnackBar() {
        withFragment {
            selectMenuItem(R.id.menu_item_scan_qr) shouldBe true
            shadowOf(Looper.getMainLooper()).idle()
            requireActivity().findViewById<TextView>(MaterialR.id.snackbar_text).text.toString() shouldBe "Camera not available"
            shadowOf(requireActivity()).nextStartedActivityForResult shouldBe null
        }
    }

    @Test
    fun unknownMenuItem_isNotHandled() {
        withFragment { selectMenuItem(UNKNOWN_ITEM_ID) shouldBe false }
    }

    @Test
    fun scanMenuItem_withCamera_launchesQrScan() {
        withFragment {
            allowCamera()
            selectMenuItem(R.id.menu_item_scan_qr) shouldBe true
            shadowOf(requireActivity())
                .nextStartedActivityForResult.intent.component
                ?.className shouldBe
                QrScanActivity::class.java.name
        }
    }

    @Test
    fun scanResult_withDecodedString_showsDialog() {
        withFragment {
            allowCamera()
            selectMenuItem(R.id.menu_item_scan_qr)
            scanResult(Activity.RESULT_OK, QR_CONTENT)
            val dialog = ShadowDialog.getLatestDialog() as AlertDialog
            dialog.findViewById<TextView>(androidx.appcompat.R.id.alertTitle)!!.text.toString() shouldBe "Scan result"
            dialog.findViewById<TextView>(android.R.id.message)!!.text.toString() shouldBe QR_CONTENT
        }
    }

    @Test
    fun scanResult_cancelled_showsNoDialog() {
        withFragment {
            allowCamera()
            selectMenuItem(R.id.menu_item_scan_qr)
            scanResult(Activity.RESULT_CANCELED, QR_CONTENT)
            ShadowDialog.getLatestDialog() shouldBe null
        }
    }

    @Test
    fun scanResult_clickingShareButton_firesShareIntent() {
        withFragment {
            allowCamera()
            selectMenuItem(R.id.menu_item_scan_qr)
            scanResult(Activity.RESULT_OK, QR_CONTENT)
            val dialog = ShadowDialog.getLatestDialog() as AlertDialog
            dialog.getButton(BUTTON_NEUTRAL).performClick()
            shadowOf(Looper.getMainLooper()).idle()
            shadowOf(requireActivity()).nextStartedActivity shouldNotBe null
        }
    }

    companion object {
        private const val QR_SUBTAB_INDEX = 2
        private const val UNKNOWN_ITEM_ID = -1
        private const val QR_CONTENT = "https://github.com/tribalfs/oneui-design"
    }
}
