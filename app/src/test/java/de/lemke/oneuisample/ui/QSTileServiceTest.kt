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
import android.service.quicksettings.Tile
import de.lemke.oneuisample.R
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldStartWith
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowToast

@RunWith(RobolectricTestRunner::class)
@Config(application = Application::class, sdk = [36])
class QSTileServiceTest {
    private val service by lazy { Robolectric.buildService(QSTileService::class.java).create().get() }

    @Test
    fun `start listening shows the tile subtitle`() {
        service.onStartListening()

        service.qsTile.subtitle shouldBe "Subtitle"
    }

    @Test
    @Config(sdk = [28])
    fun `start listening below Android 10 leaves the tile untouched`() {
        service.qsTile.state = Tile.STATE_INACTIVE
        service.qsTile.label = "Label"

        service.onStartListening()

        service.qsTile.state shouldBe Tile.STATE_INACTIVE
        service.qsTile.label shouldBe "Label"
    }

    @Test
    fun `stop listening keeps the tile as start listening left it`() {
        service.qsTile.state = Tile.STATE_ACTIVE
        service.onStartListening()

        service.onStopListening()

        service.qsTile.subtitle shouldBe "Subtitle"
        service.qsTile.state shouldBe Tile.STATE_ACTIVE
    }

    @Test
    fun `clicking an active tile deactivates it`() {
        service.qsTile.state = Tile.STATE_ACTIVE

        service.onClick()

        service.qsTile.state shouldBe Tile.STATE_INACTIVE
        ShadowToast.getTextOfLatestToast() shouldBe "Tile clicked: inactive"
    }

    @Test
    fun `clicking an inactive tile activates it`() {
        service.qsTile.state = Tile.STATE_INACTIVE

        service.onClick()

        service.qsTile.state shouldBe Tile.STATE_ACTIVE
        ShadowToast.getTextOfLatestToast() shouldBe "Tile clicked: active"
    }

    @Test
    fun `detail view shows the app title, a settings button and the tile layout`() {
        service.semIsToggleButtonExists() shouldBe true
        service.semGetDetailViewTitle().toString() shouldStartWith "OneUI Sample App"
        service.semGetDetailViewSettingButtonName().toString() shouldBe "Settings"
        service.semGetDetailView().layoutId shouldBe R.layout.qs_detail_view
    }

    @Test
    fun `detail view settings intent opens the main activity`() {
        service.semGetSettingsIntent().component?.className shouldBe "de.lemke.oneuisample.ui.MainActivity"
    }

    @Test
    fun `detail view toggle mirrors the tile state`() {
        service.qsTile.state = Tile.STATE_ACTIVE
        service.semIsToggleButtonChecked() shouldBe true

        service.qsTile.state = Tile.STATE_INACTIVE
        service.semIsToggleButtonChecked() shouldBe false
    }

    @Test
    fun `checking the detail view toggle activates the tile`() {
        service.qsTile.state = Tile.STATE_INACTIVE

        service.semSetToggleButtonChecked(true)

        service.qsTile.state shouldBe Tile.STATE_ACTIVE
        ShadowToast.getTextOfLatestToast() shouldBe "Toggle Button: true"
    }

    @Test
    fun `unchecking the detail view toggle deactivates the tile`() {
        service.qsTile.state = Tile.STATE_ACTIVE

        service.semSetToggleButtonChecked(false)

        service.qsTile.state shouldBe Tile.STATE_INACTIVE
        ShadowToast.getTextOfLatestToast() shouldBe "Toggle Button: false"
    }
}
