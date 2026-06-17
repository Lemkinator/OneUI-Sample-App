/*
 * Copyright 2024-2026 Leonard Lemke
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

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.RemoteViews
import de.lemke.oneuisample.R
import de.lemke.oneuisample.ui.util.toast

@Suppress("redundantOverride", "unused")
class QSTileService : TileService() {
    override fun onCreate() {
        // Called by the system when the service is first created.
        super.onCreate()
    }

    override fun onStartListening() {
        // Called when this tile moves into a listening state.
        // When this tile is in a listening state, it is expected to keep the UI up to date.
        // Any listeners or callbacks needed to keep this tile up to date should be registered here.
        super.onStartListening()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            qsTile.subtitle = getString(R.string.qs_tile_subtitle)
            qsTile.updateTile()
        }
    }

    override fun onStopListening() {
        // Called when this tile moves out of a listening state.
        // Any listeners or callbacks registered in onStartListening() should be unregistered here.
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()
        if (qsTile.state == Tile.STATE_ACTIVE) {
            qsTile.state = Tile.STATE_INACTIVE
            toast("Tile clicked: inactive")
        } else {
            qsTile.state = Tile.STATE_ACTIVE
            toast("Tile clicked: active")
        }
        qsTile.updateTile()
    }

    /** #### Detail view (samsung only) #### **/

    fun semGetSettingsIntent(): Intent = Intent(this, MainActivity::class.java)

    @Suppress("FunctionOnlyReturningConstant")
    fun semIsToggleButtonExists(): Boolean = true

    fun semIsToggleButtonChecked(): Boolean = qsTile.state == Tile.STATE_ACTIVE

    fun semGetDetailViewTitle(): CharSequence = getString(R.string.app_name)

    fun semGetDetailView(): RemoteViews = RemoteViews(packageName, R.layout.qs_detail_view)

    fun semSetToggleButtonChecked(checked: Boolean) {
        toast("Toggle Button: $checked")
        qsTile.state = if (checked) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }

    fun semGetDetailViewSettingButtonName(): CharSequence = getString(R.string.settings)
}
