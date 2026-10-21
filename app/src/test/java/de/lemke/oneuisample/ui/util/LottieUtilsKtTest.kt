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

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.os.Looper
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.test.core.app.ApplicationProvider
import com.airbnb.lottie.LottieAnimationView
import com.airbnb.lottie.LottieCompositionFactory
import de.lemke.oneuisample.App
import de.lemke.oneuisample.R
import de.lemke.oneuisample.ui.util.DEFAULT_LOTTIE_DELAY
import io.kotest.matchers.shouldBe
import java.lang.ref.WeakReference
import java.util.concurrent.TimeUnit
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowLooper

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LottieUtilsKtTest {
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
    fun play_withAnimation_doesNotCrash() {
        view.play(animation = "sad_face.json")
    }

    @Test
    fun play_withDelay_noLifecycleOwner_doesNotCrash() {
        view.play(delay = DEFAULT_LOTTIE_DELAY)
    }

    @Test
    fun play_withDelay_withLifecycleOwner_playsAfterDelay() {
        val owner =
            SimpleLifecycleOwner().also {
                it.registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }
        val v = attachedView(owner)
        val starts = v.countAnimationStarts()
        v.play(delay = DEFAULT_LOTTIE_DELAY)
        starts.size shouldBe 0
        ShadowLooper.shadowMainLooper().idleFor(DEFAULT_LOTTIE_DELAY.inWholeMilliseconds + 100, TimeUnit.MILLISECONDS)
        starts.size shouldBe 1
    }

    @Test
    fun launchDelayedPlay_withNullWeakReference_skipsPlayAnimation() {
        val owner = SimpleLifecycleOwner().also { it.registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME) }
        val nullRef: WeakReference<LottieAnimationView> = WeakReference(null)
        launchDelayedPlay(owner, nullRef, DEFAULT_LOTTIE_DELAY)
        ShadowLooper.shadowMainLooper().idleFor(DEFAULT_LOTTIE_DELAY.inWholeMilliseconds + 100, TimeUnit.MILLISECONDS)
    }

    @Test
    fun play_secondCall_cancelsPendingJob() {
        val owner =
            SimpleLifecycleOwner().also {
                it.registry.handleLifecycleEvent(Lifecycle.Event.ON_RESUME)
            }
        val v = attachedView(owner)
        val starts = v.countAnimationStarts()
        v.play(delay = DEFAULT_LOTTIE_DELAY)
        v.play(delay = DEFAULT_LOTTIE_DELAY)
        starts.size shouldBe 0
        ShadowLooper.shadowMainLooper().idleFor(DEFAULT_LOTTIE_DELAY.inWholeMilliseconds + 100, TimeUnit.MILLISECONDS)
        starts.size shouldBe 1
    }

    // LottieDrawable defers playAnimation until the drawable is visible, and ImageView marks it visible
    // only while the view is attached to a visible window.
    private fun attachedView(owner: LifecycleOwner): LottieAnimationView {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().get()
        return view.also {
            activity.setContentView(it)
            shadowOf(Looper.getMainLooper()).idle()
            it.setViewTreeLifecycleOwner(owner)
            it.setComposition(checkNotNull(LottieCompositionFactory.fromRawResSync(it.context, R.raw.lottie_swipe_up_anim).value))
        }
    }

    private fun LottieAnimationView.countAnimationStarts(): List<Animator> =
        mutableListOf<Animator>().also { starts ->
            addAnimatorListener(
                object : AnimatorListenerAdapter() {
                    override fun onAnimationStart(animation: Animator) {
                        starts += animation
                    }
                },
            )
        }

    private class SimpleLifecycleOwner : LifecycleOwner {
        val registry = LifecycleRegistry(this)
        override val lifecycle: Lifecycle get() = registry
    }
}
