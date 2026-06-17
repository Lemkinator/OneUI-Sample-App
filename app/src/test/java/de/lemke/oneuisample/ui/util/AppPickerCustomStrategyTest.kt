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
package de.lemke.oneuisample.ui.util

import androidx.picker.features.composable.ComposableTypeSet
import androidx.picker.model.AppData.Companion.TYPE_ITEM_ACTION_BUTTON
import androidx.picker.model.AppData.Companion.TYPE_ITEM_CHECKBOX
import androidx.picker.model.AppData.Companion.TYPE_ITEM_RADIOBUTTON
import androidx.picker.model.AppData.Companion.TYPE_ITEM_SWITCH
import androidx.picker.model.viewdata.AppInfoViewData
import androidx.picker.model.viewdata.ViewData
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.every
import io.mockk.mockk

class AppPickerCustomStrategyTest : ShouldSpec(
    {
        val strategy = AppPickerCustomStrategy()

        should("getCustomFrameList returns empty list") {
            strategy.getCustomFrameList() shouldBe emptyList()
        }

        should("selectComposableType returns null for plain non-AppInfoViewData") {
            val viewData = object : ViewData {}
            strategy.selectComposableType(viewData) shouldBe null
        }

        should("selectComposableType returns CheckBox for TYPE_ITEM_CHECKBOX") {
            val viewData = mockk<AppInfoViewData>()
            every { viewData.itemType } returns TYPE_ITEM_CHECKBOX
            strategy.selectComposableType(viewData) shouldBe ComposableTypeSet.CheckBox
        }

        should("selectComposableType returns Radio for TYPE_ITEM_RADIOBUTTON") {
            val viewData = mockk<AppInfoViewData>()
            every { viewData.itemType } returns TYPE_ITEM_RADIOBUTTON
            strategy.selectComposableType(viewData) shouldBe ComposableTypeSet.Radio
        }

        should("selectComposableType returns ActionComposableType with null leftFrame for TYPE_ITEM_ACTION_BUTTON") {
            val viewData = mockk<AppInfoViewData>()
            every { viewData.itemType } returns TYPE_ITEM_ACTION_BUTTON
            val composable = strategy.selectComposableType(viewData)
            composable shouldNotBe null
            composable!!.leftFrame shouldBe null
            composable.iconFrame shouldNotBe null
            composable.titleFrame shouldNotBe null
            composable.widgetFrame shouldNotBe null
        }

        should("selectComposableType returns Switch for TYPE_ITEM_SWITCH") {
            val viewData = mockk<AppInfoViewData>()
            every { viewData.itemType } returns TYPE_ITEM_SWITCH
            strategy.selectComposableType(viewData) shouldBe ComposableTypeSet.Switch
        }

        should("selectComposableType returns TextOnly for unknown item type") {
            val viewData = mockk<AppInfoViewData>()
            every { viewData.itemType } returns 999
            strategy.selectComposableType(viewData) shouldBe ComposableTypeSet.TextOnly
        }
    },
)
