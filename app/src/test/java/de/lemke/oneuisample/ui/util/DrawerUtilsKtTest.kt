package de.lemke.oneuisample.ui.util

import android.os.Bundle
import android.view.MenuItem
import com.google.android.material.navigation.NavigationView
import dev.oneuiproject.oneui.navigation.widget.DrawerNavigationView
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class DrawerUtilsKtTest {
    @Test
    fun `saveSearchAndActionMode stores search mode flag when true`() {
        val bundle = Bundle()
        bundle.saveSearchAndActionMode(isSearchMode = true)
        bundle.getBoolean(KEY_IS_SEARCH_MODE) shouldBe true
    }

    @Test
    fun `saveSearchAndActionMode omits search mode key when false`() {
        val bundle = Bundle()
        bundle.saveSearchAndActionMode(isSearchMode = false)
        bundle.containsKey(KEY_IS_SEARCH_MODE) shouldBe false
    }

    @Test
    fun `saveSearchAndActionMode stores action mode flag and selected IDs`() {
        val bundle = Bundle()
        bundle.saveSearchAndActionMode(isActionMode = true, selectedIds = setOf(1L, 2L, 3L))
        bundle.getBoolean(KEY_IS_ACTION_MODE) shouldBe true
        bundle.getLongArray(KEY_SELECTED_IDS)?.toSet() shouldBe setOf(1L, 2L, 3L)
    }

    @Test
    fun `saveSearchAndActionMode omits action mode keys when false`() {
        val bundle = Bundle()
        bundle.saveSearchAndActionMode()
        bundle.containsKey(KEY_IS_ACTION_MODE) shouldBe false
        bundle.containsKey(KEY_SELECTED_IDS) shouldBe false
    }

    @Test
    fun `restoreSearchAndActionMode invokes bundleIsNull when bundle is null`() {
        var nullCalled = false
        null.restoreSearchAndActionMode(bundleIsNull = { nullCalled = true })
        nullCalled shouldBe true
    }

    @Test
    fun `restoreSearchAndActionMode does not invoke mode callbacks for null bundle`() {
        var searchCalled = false
        var actionCalled = false
        null.restoreSearchAndActionMode(
            onSearchMode = { searchCalled = true },
            onActionMode = { actionCalled = true },
        )
        searchCalled shouldBe false
        actionCalled shouldBe false
    }

    @Test
    fun `restoreSearchAndActionMode invokes onSearchMode when flag is set`() {
        val bundle = Bundle()
        bundle.saveSearchAndActionMode(isSearchMode = true)
        var searchCalled = false
        bundle.restoreSearchAndActionMode(onSearchMode = { searchCalled = true })
        searchCalled shouldBe true
    }

    @Test
    fun `restoreSearchAndActionMode invokes onActionMode with selected IDs`() {
        val bundle = Bundle()
        bundle.saveSearchAndActionMode(isActionMode = true, selectedIds = setOf(10L, 20L))
        var receivedIds: Set<Long> = emptySet()
        bundle.restoreSearchAndActionMode(onActionMode = { ids -> receivedIds = ids })
        receivedIds shouldBe setOf(10L, 20L)
    }

    @Test
    fun `restoreSearchAndActionMode passes empty set when action mode has no IDs`() {
        val bundle = Bundle()
        bundle.saveSearchAndActionMode(isActionMode = true)
        var receivedIds: Set<Long>? = null
        bundle.restoreSearchAndActionMode(onActionMode = { ids -> receivedIds = ids })
        receivedIds shouldBe emptySet()
    }

    @Test
    fun `restoreSearchAndActionMode does not invoke mode callbacks when no flags set`() {
        val bundle = Bundle()
        var searchCalled = false
        var actionCalled = false
        bundle.restoreSearchAndActionMode(
            onSearchMode = { searchCalled = true },
            onActionMode = { actionCalled = true },
        )
        searchCalled shouldBe false
        actionCalled shouldBe false
    }

    @Test
    fun `restoreSearchAndActionMode can invoke both callbacks when both flags set`() {
        val bundle = Bundle()
        bundle.saveSearchAndActionMode(isSearchMode = true, isActionMode = true, selectedIds = setOf(5L))
        var searchCalled = false
        var actionCalled = false
        bundle.restoreSearchAndActionMode(
            onSearchMode = { searchCalled = true },
            onActionMode = { actionCalled = true },
        )
        searchCalled shouldBe true
        actionCalled shouldBe true
    }

    @Test
    fun `restoreSearchAndActionMode uses default bundleIsNull lambda when not provided`() {
        // calls default no-op bundleIsNull — covers the default lambda
        null.restoreSearchAndActionMode()
    }

    @Test
    fun `restoreSearchAndActionMode passes empty set when KEY_SELECTED_IDS absent from bundle`() {
        // putBoolean only — no putLongArray, so getLongArray returns null → ?: emptySet() fires
        val bundle = Bundle()
        bundle.putBoolean(KEY_IS_ACTION_MODE, true)
        var receivedIds: Set<Long>? = null
        bundle.restoreSearchAndActionMode(onActionMode = { ids -> receivedIds = ids })
        receivedIds shouldBe emptySet()
    }

    @Test
    fun `restoreSearchAndActionMode uses default onActionMode lambda when isActionMode is set`() {
        // no onActionMode arg → $default provides {} → invokes default lambda with emptySet
        val bundle = Bundle()
        bundle.putBoolean(KEY_IS_ACTION_MODE, true)
        bundle.restoreSearchAndActionMode()
    }

    @Test
    fun `restoreSearchAndActionMode uses default onSearchMode lambda when isSearchMode is set`() {
        // no onSearchMode arg → $default provides {} → invokes default lambda
        val bundle = Bundle()
        bundle.putBoolean(KEY_IS_SEARCH_MODE, true)
        bundle.restoreSearchAndActionMode()
    }

    @Test
    fun `onNavigationSingleClick first click is allowed and rapid repeat is blocked`() {
        val navView = mockk<DrawerNavigationView>()
        val listenerSlot = slot<NavigationView.OnNavigationItemSelectedListener>()
        every { navView.setNavigationItemSelectedListener(capture(listenerSlot)) } answers { }
        val item = mockk<MenuItem>()
        var delegateCallCount = 0
        // interval = 1_000_000L ms (1000 seconds): any two calls within 1000 s are blocked
        // but first call (lastClick=0, currentTime≈1.7e12) is always allowed since 1.7e12 > 1_000_000
        navView.onNavigationSingleClick(interval = 1_000_000L) {
            delegateCallCount++
            true
        }
        listenerSlot.captured.onNavigationItemSelected(item) // first: allowed
        delegateCallCount shouldBe 1
        listenerSlot.captured.onNavigationItemSelected(item) // immediate repeat: blocked
        delegateCallCount shouldBe 1
    }
}
