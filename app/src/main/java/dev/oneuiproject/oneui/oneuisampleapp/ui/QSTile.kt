package dev.oneuiproject.oneui.oneuisampleapp.ui

import android.content.Intent
import android.os.Build
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.widget.RemoteViews
import android.widget.Toast
import dev.oneuiproject.oneui.oneuisampleapp.R

@Suppress("redundantOverride","unused")
class QSTile : TileService() {

    override fun onCreate() {
        //Called by the system when the service is first created.
        super.onCreate()
    }

    override fun onStartListening() {
        //Called when this tile moves into a listening state.
        //When this tile is in a listening state it is expected to keep the UI up to date.
        //Any listeners or callbacks needed to keep this tile up to date should be registered here.
        super.onStartListening()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            qsTile.subtitle = getString(R.string.qs_tile_subtitle)
            qsTile.updateTile()
        }
    }

    override fun onStopListening() {
        //Called when this tile moves out of a listening state.
        //Any listeners or callbacks registered in onStartListening() should be unregistered here.
        super.onStopListening()
    }

    override fun onClick() {
        super.onClick()
        if (qsTile.state == Tile.STATE_ACTIVE) {
            qsTile.state = Tile.STATE_INACTIVE
            Toast.makeText(this, "Tile clicked: inactive", Toast.LENGTH_SHORT).show()
        } else {
            qsTile.state = Tile.STATE_ACTIVE
            Toast.makeText(this, "Tile clicked: active", Toast.LENGTH_SHORT).show()
        }
        qsTile.updateTile()
    }


    /** #### Detail view (samsung only) #### **/

    fun semGetSettingsIntent(): Intent = Intent(this, MainActivity::class.java)
    fun semIsToggleButtonExists(): Boolean = true
    fun semIsToggleButtonChecked(): Boolean = qsTile.state == Tile.STATE_ACTIVE
    fun semGetDetailViewTitle(): CharSequence = this.getString(R.string.app_name)
    fun semGetDetailView(): RemoteViews = RemoteViews(packageName, R.layout.qs_detail_view)
    fun semSetToggleButtonChecked(checked: Boolean) {
        Toast.makeText(this, "Toggle Button: $checked", Toast.LENGTH_SHORT).show()
        qsTile.state = if (checked) Tile.STATE_ACTIVE else Tile.STATE_INACTIVE
        qsTile.updateTile()
    }

    fun semGetDetailViewSettingButtonName(): CharSequence = "Setting Button Name" //unknown

}