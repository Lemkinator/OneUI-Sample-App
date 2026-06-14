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

import android.app.Application
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.App
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class FaceWidgetReceiverTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private val receiver = FaceWidgetReceiver()

    @Test
    fun onReceive_requestAction_sendsBroadcast() {
        receiver.onReceive(
            context,
            Intent("com.samsung.android.intent.action.REQUEST_SERVICEBOX_REMOTEVIEWS").apply {
                putExtra("pageId", "test_page")
            },
        )
    }

    @Test
    fun onReceive_unknownAction_doesNothing() {
        receiver.onReceive(context, Intent("some.other.action"))
    }
}
