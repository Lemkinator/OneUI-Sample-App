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

import android.app.Activity
import android.content.Intent
import android.view.View
import androidx.annotation.MainThread
import androidx.reflect.DeviceInfo
import dev.oneuiproject.oneui.ktx.activity
import dev.oneuiproject.oneui.ktx.startPopOverActivity
import dev.oneuiproject.oneui.popover.PopOverOptions
import dev.oneuiproject.oneui.utils.DeviceLayoutUtil

/**
 * Opens the page of [cls] from this drawer item or header button; see the [Activity] overload.
 */
@MainThread
fun View.launchDrawerPage(cls: Class<*>) {
    val activity = context.activity
    if (activity != null) {
        activity.launchDrawerPage(cls)
    } else {
        context.startActivity(Intent(context, cls))
    }
}

/**
 * Opens the page of [cls] from this activity's drawer.
 *
 * On a One UI device whose smallest width is at least 600dp, the page starts as a pop-over with
 * [Intent.FLAG_ACTIVITY_SINGLE_TOP]. Every other device gets a plain launch.
 */
@MainThread
fun Activity.launchDrawerPage(cls: Class<*>) {
    val intent = Intent(this, cls)
    if (showsPopOver()) {
        startPopOverActivity(intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), PopOverOptions.topCenterAnchored(this))
    } else {
        startActivity(intent)
    }
}

// One UI starts a pop-over only in a task whose smallest width is at least 600dp.
private fun Activity.showsPopOver(): Boolean = DeviceInfo.isOneUI() && DeviceLayoutUtil.isTabletLayout(resources)
