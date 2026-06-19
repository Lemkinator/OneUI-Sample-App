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

import android.app.AlertDialog
import android.app.Application
import android.content.Context
import android.content.Intent
import android.os.Looper
import android.view.LayoutInflater
import android.view.MenuItem
import androidx.navigation.fragment.NavHostFragment
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.App
import de.lemke.oneuisample.R
import de.lemke.oneuisample.bypassOobe
import de.lemke.oneuisample.data.UserSettingsRepository
import de.lemke.oneuisample.databinding.DialogSettingsBinding
import de.lemke.oneuisample.domain.Icon
import de.lemke.oneuisample.ui.fragments.TabIconsFragment
import dev.oneuiproject.oneui.layout.ToolbarLayout
import io.mockk.every
import io.mockk.mockk
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowAlertDialog

@RunWith(RobolectricTestRunner::class)
@Config(application = App::class, sdk = [36])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class TabIconsFragmentTest {
    private val context get() = ApplicationProvider.getApplicationContext<Application>()
    private val prefs get() = context.getSharedPreferences(UserSettingsRepository.PREFS_NAME, Context.MODE_PRIVATE)

    @Before
    fun setup() {
        prefs.bypassOobe()
    }

    private fun withFragment(block: TabIconsFragment.() -> Unit) {
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                (activity.supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment)
                    .navController
                    .navigate(R.id.icons_dest)
            }
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                val fragment =
                    (activity.supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment)
                        .childFragmentManager
                        .primaryNavigationFragment as? TabIconsFragment
                fragment?.block()
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    private fun mockMenuItem(id: Int): MenuItem = mockk { every { itemId } returns id }

    @Test
    fun onIconTabMenuItemSelected_search_startsSearchMode() {
        withFragment { onIconTabMenuItemSelected(mockMenuItem(R.id.menu_item_search)) }
    }

    @Test
    fun onIconTabMenuItemSelected_settings_showsDialog() {
        withFragment { onIconTabMenuItemSelected(mockMenuItem(R.id.menu_item_settings)) }
    }

    @Test
    fun onIconTabMenuItemSelected_unknown_returnsFalse() {
        withFragment { onIconTabMenuItemSelected(mockMenuItem(-1)) }
    }

    @Test
    fun onActionModeMenuItemSelected_item1_showsSnackbar() {
        withFragment { onActionModeMenuItemSelected(mockMenuItem(R.id.menu_item_1)) }
    }

    @Test
    fun onActionModeMenuItemSelected_item2_showsSnackbar() {
        withFragment { onActionModeMenuItemSelected(mockMenuItem(R.id.menu_item_2)) }
    }

    @Test
    fun onActionModeMenuItemSelected_item3_showsSnackbar() {
        withFragment { onActionModeMenuItemSelected(mockMenuItem(R.id.menu_item_3)) }
    }

    @Test
    fun onActionModeMenuItemSelected_unknown_returnsFalse() {
        withFragment { onActionModeMenuItemSelected(mockMenuItem(-1)) }
    }

    @Test
    fun updateList_emptyList_showsNoEntryView() {
        withFragment {
            updateList(Pair(emptyList(), null))
            shadowOf(Looper.getMainLooper()).runToEndOfTasks()
        }
    }

    @Test
    fun updateList_nonEmptyList_showsIconList() {
        withFragment {
            val icon = Icon(R.drawable.ic_launcher, "ic_oui_settings")
            updateList(Pair(listOf(icon), null))
        }
    }

    @Test
    fun updateList_nonEmptyListWithHighlight_setsHighlight() {
        withFragment {
            val icon = Icon(R.drawable.ic_launcher, "ic_oui_settings")
            updateList(Pair(listOf(icon), "set"))
        }
    }

    @Test
    fun onIconItemClicked_notInActionMode_showsSnackBar() {
        withFragment {
            val icon = Icon(R.drawable.ic_launcher, "ic_oui_settings")
            onIconItemClicked(0, icon)
        }
    }

    @Test
    fun onIconItemClicked_inActionMode_togglesSelection() {
        withFragment {
            val icon = Icon(R.drawable.ic_launcher, "ic_oui_settings")
            updateList(Pair(listOf(icon), null))
            launchActionMode()
            onIconItemClicked(0, icon)
        }
    }

    @Test
    fun launchActionMode_startsActionMode() {
        withFragment { launchActionMode() }
    }

    @Test
    fun settingsDialog_positiveButton_appliesSettings() {
        ActivityScenario.launch<MainActivity>(Intent(context, MainActivity::class.java)).use { scenario ->
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                (activity.supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment)
                    .navController
                    .navigate(R.id.icons_dest)
            }
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity { activity ->
                val fragment =
                    (activity.supportFragmentManager.findFragmentById(R.id.navigationHost) as NavHostFragment)
                        .childFragmentManager.primaryNavigationFragment as? TabIconsFragment
                fragment?.onIconTabMenuItemSelected(mockMenuItem(R.id.menu_item_settings))
            }
            shadowOf(Looper.getMainLooper()).idle()
            scenario.onActivity {
                ShadowAlertDialog.getLatestAlertDialog()?.getButton(AlertDialog.BUTTON_POSITIVE)?.performClick()
            }
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun applySettings_dismiss_updatesSearchOnActionMode() {
        withFragment {
            applySettings(
                actionModeShowCancel = false,
                showIndexScroll = false,
                indexScrollShowLetters = false,
                indexScrollAutoHide = false,
                checkedSearchOnActionModeId = R.id.amsDismiss,
            )
        }
    }

    @Test
    fun applySettings_noDismiss_updatesSearchOnActionMode() {
        withFragment {
            applySettings(
                actionModeShowCancel = true,
                showIndexScroll = true,
                indexScrollShowLetters = true,
                indexScrollAutoHide = true,
                checkedSearchOnActionModeId = R.id.amsNoDismiss,
            )
        }
    }

    @Test
    fun applySettings_concurrent_updatesSearchOnActionMode() {
        withFragment {
            applySettings(
                actionModeShowCancel = false,
                showIndexScroll = true,
                indexScrollShowLetters = false,
                indexScrollAutoHide = true,
                checkedSearchOnActionModeId = -1,
            )
        }
    }

    @Test
    fun onShowIndexScrollChanged_true_enablesSubOptions() {
        withFragment {
            val dialogBinding = DialogSettingsBinding.inflate(LayoutInflater.from(requireContext()))
            onShowIndexScrollChanged(dialogBinding, true)
        }
    }

    @Test
    fun onShowIndexScrollChanged_false_disablesSubOptions() {
        withFragment {
            val dialogBinding = DialogSettingsBinding.inflate(LayoutInflater.from(requireContext()))
            onShowIndexScrollChanged(dialogBinding, false)
        }
    }

    @Test
    fun showSettingsDialog_withDismissSearchMode_checksCorrectRadio() {
        withFragment {
            userSettings.searchOnActionMode = ToolbarLayout.SearchOnActionMode.Dismiss
            showSettingsDialog()
        }
    }

    @Test
    fun showSettingsDialog_withNoDismissSearchMode_checksCorrectRadio() {
        withFragment {
            userSettings.searchOnActionMode = ToolbarLayout.SearchOnActionMode.NoDismiss
            showSettingsDialog()
        }
    }

    @Test
    fun showSettingsDialog_withConcurrentSearchMode_checksCorrectRadio() {
        withFragment {
            userSettings.searchOnActionMode = ToolbarLayout.SearchOnActionMode.Concurrent(null)
            showSettingsDialog()
        }
    }

    @Test
    fun onIconSwiped_startDirection_showsSnackBar() {
        withFragment {
            val icon = Icon(R.drawable.ic_launcher, "ic_oui_settings")
            updateList(Pair(listOf(icon), null))
            onIconSwiped(0, androidx.recyclerview.widget.ItemTouchHelper.START)
        }
    }

    @Test
    fun onIconSwiped_endDirection_showsSnackBar() {
        withFragment {
            val icon = Icon(R.drawable.ic_launcher, "ic_oui_settings")
            updateList(Pair(listOf(icon), null))
            onIconSwiped(0, androidx.recyclerview.widget.ItemTouchHelper.END)
        }
    }

    @Test
    fun observeIconList_withActiveSearch_emitsFilteredList() {
        withFragment {
            userSettings.searchActive = true
            userSettings.search = "settings"
            shadowOf(Looper.getMainLooper()).idle()
        }
    }

    @Test
    fun applySettingsFromDialog_withDefaultDialogBinding_appliesSettings() {
        withFragment {
            val dialogBinding = DialogSettingsBinding.inflate(LayoutInflater.from(requireContext()))
            applySettingsFromDialog(dialogBinding)
        }
    }

    @Test
    fun iconAdapter_toggleSelectAll_updatesAllSelectorState() {
        withFragment {
            val icon = Icon(R.drawable.ic_launcher, "ic_oui_settings")
            updateList(Pair(listOf(icon), null))
            shadowOf(Looper.getMainLooper()).idle()
            launchActionMode()
            shadowOf(Looper.getMainLooper()).idle()
            iconAdapter.onToggleSelectAll(true)
            shadowOf(Looper.getMainLooper()).idle()
        }
    }
}
