package dev.oneuiproject.oneui.oneuisampleapp.ui

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import dev.oneuiproject.oneui.oneuisampleapp.R

class FaceWidgetReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if ("com.samsung.android.intent.action.REQUEST_SERVICEBOX_REMOTEVIEWS" == intent.action) {
            //code will be executed when the system ask for an update
            with(Intent("com.samsung.android.intent.action.RESPONSE_SERVICEBOX_REMOTEVIEWS")) {
                setPackage("com.android.systemui")
                putExtra("package", context.packageName) //your app packageName
                putExtra("pageId", intent.getStringExtra("pageId")) //the pageId which you received in the BroadcastReceiver
                putExtra("show", true)
                putExtra("origin", RemoteViews(context.packageName, R.layout.face_widget)) //the RemoteViews for the lockscreen
                putExtra("aod", RemoteViews(context.packageName, R.layout.face_widget_aod)) //the RemoteViews for the AOD
                context.sendBroadcast(this@with)
            }
        }
    }
}