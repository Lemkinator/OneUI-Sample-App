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
package de.lemke.oneuisample.ui

import app.cash.turbine.ReceiveTurbine
import app.cash.turbine.test
import de.lemke.oneuisample.data.UserSettings
import de.lemke.oneuisample.data.fakeUserSettings
import de.lemke.oneuisample.ui.util.ListTypes
import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.matchers.types.shouldNotBeSameInstanceAs
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.setMain

private const val OUT_OF_RANGE_TYPE = 999
private const val BACKGROUND_MILLIS = 60_000L

private suspend fun ReceiveTurbine<AppPickerUiState>.awaitLoaded(): AppPickerUiState {
    while (true) {
        val state = awaitItem()
        if (state.apps is AppListState.Loaded) return state
    }
}

private val AppPickerUiState.loaded get() = apps as AppListState.Loaded

private fun AppListState.Loaded.firstApp() = apps.first()

private fun AppListState.Loaded.packages() = apps.map { it.appInfo.packageName }

@OptIn(ExperimentalCoroutinesApi::class)
class AppPickerViewModelTest : ShouldSpec(
    {
        lateinit var settingsScope: CoroutineScope
        lateinit var settings: UserSettings
        lateinit var source: FakeAppListSource
        lateinit var mainScheduler: TestCoroutineScheduler
        lateinit var viewModel: AppPickerViewModel

        fun newViewModel(ioDispatcher: CoroutineDispatcher = UnconfinedTestDispatcher(mainScheduler)) =
            AppPickerViewModel(settings, source, ioDispatcher)

        beforeEach {
            mainScheduler = TestCoroutineScheduler()
            Dispatchers.setMain(UnconfinedTestDispatcher(mainScheduler))
            settingsScope = CoroutineScope(UnconfinedTestDispatcher())
            settings = fakeUserSettings(settingsScope)
            source = FakeAppListSource()
            viewModel = newViewModel()
        }

        afterEach { settingsScope.cancel() }

        should("nothing loads before the first collection") {
            viewModel.state.value.apps shouldBe AppListState.Loading
            source.loads.shouldBeEmpty()
        }

        should("initial state reflects settings appPickerType = 1") {
            settings.appPickerType = 1
            newViewModel().state.value shouldBe AppPickerUiState(listType = ListTypes.TYPE_LIST_ACTION_BUTTON)
        }

        should("initial state falls back to the first list type when the stored type is out of range") {
            settings.appPickerType = OUT_OF_RANGE_TYPE
            newViewModel().state.value shouldBe AppPickerUiState(listType = ListTypes.LIST_TYPE)
        }

        should("initial state reflects settings appPickerSelectLayoutMode = true") {
            settings.appPickerSelectLayoutMode = true
            newViewModel().state.value shouldBe AppPickerUiState(isSelectLayoutMode = true)
        }

        should("loads the list of the stored type on the first collection") {
            settings.appPickerType = ListTypes.TYPE_GRID.ordinal
            newViewModel().state.test {
                val state = awaitLoaded()
                state.listType shouldBe ListTypes.TYPE_GRID
                state.loaded.packages() shouldContainExactly listOf("app.TYPE_GRID")
            }
            source.loads shouldContainExactly listOf(ListTypes.TYPE_GRID)
        }

        should("loads the checkbox list when the select layout mode is stored") {
            settings.appPickerSelectLayoutMode = true
            settings.appPickerType = ListTypes.TYPE_GRID.ordinal
            newViewModel().state.test {
                awaitLoaded().loaded.packages() shouldContainExactly listOf("app.TYPE_LIST_CHECKBOX")
            }
            source.loads shouldContainExactly listOf(ListTypes.TYPE_LIST_CHECKBOX)
        }

        should("onPickerTypeChanged writes to settings and reloads the list with reset selections") {
            viewModel.state.test {
                val first = awaitLoaded().loaded
                first.firstApp().selected = true
                viewModel.onPickerTypeChanged(2)
                settings.appPickerType shouldBe 2
                val state = awaitLoaded()
                state.listType shouldBe ListTypes.TYPE_LIST_CHECKBOX
                state.loaded shouldNotBeSameInstanceAs first
                state.loaded.firstApp().selected shouldBe false
            }
            source.loads shouldContainExactly listOf(ListTypes.LIST_TYPE, ListTypes.TYPE_LIST_CHECKBOX)
        }

        should("picking the same type again keeps the list and its selections") {
            viewModel.state.test {
                val first = awaitLoaded().loaded
                first.firstApp().selected = true
                viewModel.onPickerTypeChanged(0)
                expectNoEvents()
                viewModel.state.value.loaded shouldBeSameInstanceAs first
                first.firstApp().selected shouldBe true
            }
            source.loads shouldContainExactly listOf(ListTypes.LIST_TYPE)
        }

        should("onSelectLayoutModeToggled flips settings value, returns the new value and reloads the checkbox list") {
            settings.appPickerSelectLayoutMode = false
            viewModel.state.test {
                val first = awaitLoaded().loaded
                first.firstApp().selected = true
                viewModel.onSelectLayoutModeToggled() shouldBe true
                settings.appPickerSelectLayoutMode shouldBe true
                val state = awaitLoaded()
                state.isSelectLayoutMode shouldBe true
                state.loaded shouldNotBeSameInstanceAs first
                state.loaded.packages() shouldContainExactly listOf("app.TYPE_LIST_CHECKBOX")
                state.loaded.firstApp().selected shouldBe false
            }
            source.loads shouldContainExactly listOf(ListTypes.LIST_TYPE, ListTypes.TYPE_LIST_CHECKBOX)
        }

        should("toggling the select layout mode off reloads the simple list with reset selections") {
            settings.appPickerSelectLayoutMode = true
            settings.appPickerType = ListTypes.TYPE_GRID.ordinal
            newViewModel().state.test {
                val selectLayout = awaitLoaded().loaded
                selectLayout.firstApp().selected = true
                settings.appPickerSelectLayoutMode = false
                val state = awaitLoaded()
                state.isSelectLayoutMode shouldBe false
                state.loaded shouldNotBeSameInstanceAs selectLayout
                state.loaded.packages() shouldContainExactly listOf("app.TYPE_GRID")
                state.loaded.firstApp().selected shouldBe false
            }
            source.loads shouldContainExactly listOf(ListTypes.TYPE_LIST_CHECKBOX, ListTypes.TYPE_GRID)
        }

        should("toggling the select layout mode off shows Loading, then the simple list of the stored type with reset selections") {
            settings.appPickerSelectLayoutMode = true
            settings.appPickerType = ListTypes.TYPE_GRID.ordinal
            newViewModel(StandardTestDispatcher(mainScheduler)).state.test {
                awaitItem() shouldBe AppPickerUiState(listType = ListTypes.TYPE_GRID, isSelectLayoutMode = true)
                mainScheduler.runCurrent()
                val selectLayout = awaitItem().loaded
                selectLayout.firstApp().selected = true
                settings.appPickerSelectLayoutMode = false
                awaitItem() shouldBe AppPickerUiState(listType = ListTypes.TYPE_GRID, isSelectLayoutMode = false)
                expectNoEvents()
                source.loads shouldContainExactly listOf(ListTypes.TYPE_LIST_CHECKBOX)
                mainScheduler.runCurrent()
                val state = awaitItem()
                state.isSelectLayoutMode shouldBe false
                state.listType shouldBe ListTypes.TYPE_GRID
                state.loaded shouldNotBeSameInstanceAs selectLayout
                state.loaded.packages() shouldContainExactly listOf("app.TYPE_GRID")
                state.loaded.firstApp().selected shouldBe false
            }
            source.loads shouldContainExactly listOf(ListTypes.TYPE_LIST_CHECKBOX, ListTypes.TYPE_GRID)
        }

        should("no state pairs the select layout mode with the simple list while the checkbox list loads") {
            viewModel.state.test {
                val simple = awaitLoaded().loaded
                settings.appPickerSelectLayoutMode = true
                val transition =
                    buildList {
                        do {
                            add(awaitItem())
                        } while (last().apps !is AppListState.Loaded)
                    }
                transition.filter { it.isSelectLayoutMode }.forEach { it.apps shouldNotBeSameInstanceAs simple }
                transition.last().loaded.packages() shouldContainExactly listOf("app.TYPE_LIST_CHECKBOX")
            }
        }

        should("keeps the list and its selections for a new collector, as after a recreate or a return from the background") {
            lateinit var first: AppListState.Loaded
            viewModel.state.test {
                first = awaitLoaded().loaded
                first.firstApp().selected = true
            }
            viewModel.state.test {
                val second = awaitLoaded().loaded
                second shouldBeSameInstanceAs first
                second.firstApp().selected shouldBe true
            }
            source.loads shouldContainExactly listOf(ListTypes.LIST_TYPE)
        }

        should("keeps the list and its selections when the last collector left for longer than 5 s") {
            lateinit var first: AppListState.Loaded
            viewModel.state.test {
                first = awaitLoaded().loaded
                first.firstApp().selected = true
            }
            mainScheduler.advanceTimeBy(BACKGROUND_MILLIS)
            mainScheduler.runCurrent()
            viewModel.state.test {
                val second = awaitLoaded().loaded
                second shouldBeSameInstanceAs first
                second.firstApp().selected shouldBe true
                viewModel.state.value.loaded shouldBeSameInstanceAs first
            }
            source.loads shouldContainExactly listOf(ListTypes.LIST_TYPE)
        }

        should("a picker type change in the select layout mode updates the type without reloading") {
            settings.appPickerSelectLayoutMode = true
            newViewModel().state.test {
                val first = awaitLoaded().loaded
                settings.appPickerType = ListTypes.TYPE_GRID.ordinal
                val state = awaitLoaded()
                state.listType shouldBe ListTypes.TYPE_GRID
                state.loaded shouldBeSameInstanceAs first
            }
            source.loads shouldContainExactly listOf(ListTypes.TYPE_LIST_CHECKBOX)
        }
    },
)
