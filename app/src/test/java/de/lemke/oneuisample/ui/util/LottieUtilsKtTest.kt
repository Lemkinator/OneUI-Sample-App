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
package de.lemke.oneuisample.ui.util

import androidx.test.core.app.ApplicationProvider
import com.airbnb.lottie.LottieAnimationView
import de.lemke.oneuisample.App
import de.lemke.oneuisample.ui.util.DEFAULT_LOTTIE_DELAY
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LottieUtilsKtTest {
    // Application context → view has no view-tree lifecycle owner → launch branch skipped
    private val view get() = LottieAnimationView(ApplicationProvider.getApplicationContext<android.app.Application>())

    @Test
    fun play_withoutLifecycleOwner_doesNotCrash() {
        view.play()
    }

    @Test
    fun play_cancelFirstFalse_doesNotCrash() {
        view.play(cancelFirst = false)
    }

    @Test
    fun play_withDelay_noLifecycleOwner_doesNotCrash() {
        view.play(delay = DEFAULT_LOTTIE_DELAY)
    }
}
