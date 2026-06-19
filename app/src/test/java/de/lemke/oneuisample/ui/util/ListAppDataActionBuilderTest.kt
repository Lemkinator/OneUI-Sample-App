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

import androidx.picker.model.AppData.Companion.TYPE_ITEM_ACTION_BUTTON
import androidx.picker.model.AppInfo
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class ListAppDataActionBuilderTest : ShouldSpec(
    {
        val appInfo = AppInfo(packageName = "com.example.test", activityName = "com.example.test.MainActivity")
        val builder = ListAppDataActionBuilder(appInfo)

        should("build returns AppInfoData with TYPE_ITEM_ACTION_BUTTON") {
            builder.build().itemType shouldBe TYPE_ITEM_ACTION_BUTTON
        }

        should("build uses the provided AppInfo") {
            builder.build().appInfo shouldBe appInfo
        }

        should("build returns null for all Drawable fields") {
            val result = builder.build()
            result.icon shouldBe null
            result.subIcon shouldBe null
            result.actionIcon shouldBe null
        }

        should("build returns false for selected and dimmed") {
            val result = builder.build()
            result.selected shouldBe false
            result.dimmed shouldBe false
            result.isValueInSubLabel shouldBe false
        }
    },
)
