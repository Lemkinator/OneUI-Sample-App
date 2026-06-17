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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import de.lemke.oneuisample.R

class FaceWidgetReceiver : BroadcastReceiver() {
    override fun onReceive(
        context: Context,
        intent: Intent,
    ) {
        if ("com.samsung.android.intent.action.REQUEST_SERVICEBOX_REMOTEVIEWS" == intent.action) {
            // code will be executed when the system ask for an update
            with(Intent("com.samsung.android.intent.action.RESPONSE_SERVICEBOX_REMOTEVIEWS")) {
                setPackage("com.android.systemui")
                putExtra("package", context.packageName) // your app packageName
                putExtra("pageId", intent.getStringExtra("pageId")) // the pageId which you received in the BroadcastReceiver
                putExtra("show", true)
                putExtra("origin", RemoteViews(context.packageName, R.layout.face_widget)) // the RemoteViews for the lockscreen
                putExtra("aod", RemoteViews(context.packageName, R.layout.face_widget_aod)) // the RemoteViews for the AOD
                context.sendBroadcast(this@with)
            }
        }
    }
}
