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
package de.lemke.oneuisample.data

import android.content.Context
import android.content.SharedPreferences
import android.content.res.XmlResourceParser
import android.os.Bundle
import androidx.annotation.XmlRes
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.freshTestPreferences
import java.util.UUID
import org.robolectric.Robolectric
import org.xmlpull.v1.XmlPullParser

// Local copy of common-utils' PreferenceXmlParity.kt (this app has no common-utils dependency, per
// CLAUDE.md's "My Apps/Projects" note) - keep behaviourally identical to that copy. See its KDoc for
// the full rationale; this file only carries the adaptation to this app's package.

private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"

/**
 * Raw-XML scan (not the inflated tree) for every `android:key` whose element also declares
 * `android:defaultValue`. Deliberately independent of the inflated widgets' runtime persistence
 * behaviour: several widgets (e.g. `HorizontalRadioPreference`) only call `persistX()` when the
 * resolved value differs from the widget's own in-memory starting field, so a *declared* default
 * that happens to equal that starting field never gets written - checking `SharedPreferences`
 * post-inflation would misreport it as "no default declared". `android:key` may be a literal or a
 * `@string` reference; only presence of `android:defaultValue` is checked, so its own value form
 * (literal or reference) never needs resolving.
 */
private fun collectDeclaredDefaultValueKeys(
    context: Context,
    @XmlRes xmlRes: Int,
): Set<String> {
    val parser: XmlResourceParser = context.resources.getXml(xmlRes)
    val keys = mutableSetOf<String>()
    try {
        var event = parser.eventType
        while (event != XmlPullParser.END_DOCUMENT) {
            if (event == XmlPullParser.START_TAG && parser.getAttributeValue(ANDROID_NS, "defaultValue") != null) {
                val keyResId = parser.getAttributeResourceValue(ANDROID_NS, "key", 0)
                val key = if (keyResId != 0) context.getString(keyResId) else parser.getAttributeValue(ANDROID_NS, "key")
                key?.let { keys += it }
            }
            event = parser.next()
        }
    } finally {
        parser.close()
    }
    return keys
}

/**
 * Headless host: [onCreatePreferences] runs during `Fragment.onCreate`, no container view required.
 * Public (not `private`) because `FragmentManager` requires fragment classes to be public to recreate
 * them from saved instance state.
 *
 * Points [PreferenceFragmentCompat]'s own `PreferenceManager` at [sharedPreferencesName] *before*
 * inflating [xmlRes], so every widget's real `dispatchSetInitialValue()` - the same code path
 * production runs - persists its `android:defaultValue` into that isolated store, through the host
 * Activity's real theme (unlike `PreferenceManager.setDefaultValues()`, which inflates against a bare,
 * unthemed `Context` and NPEs on theme-dependent custom widgets such as `HorizontalRadioPreference`).
 */
class PreferenceXmlParityFragment : PreferenceFragmentCompat() {
    var xmlRes: Int = 0
    var sharedPreferencesName: String = ""

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
    ) {
        preferenceManager.sharedPreferencesName = sharedPreferencesName
        setPreferencesFromResource(xmlRes, rootKey)
    }
}

/** Recursively partitions an inflated preference tree into value-bearing widgets and pure navigation/container nodes. */
private fun walk(
    group: PreferenceGroup,
    valueBearing: MutableList<Preference>,
    containers: MutableList<Preference>,
) {
    for (i in 0 until group.preferenceCount) {
        val pref = group.getPreference(i)
        when {
            // PreferenceCategory and nested PreferenceScreen are always structural, never value-bearing.
            pref is PreferenceGroup -> {
                containers += pref
                walk(pref, valueBearing, containers)
            }

            // A bare androidx.preference.Preference (no subclass) is a plain click target - it never
            // calls persistXxx/getPersistedXxx, regardless of its `persistent`/`key` attributes.
            pref.javaClass == Preference::class.java -> {
                containers += pref
            }

            // Explicit opt-out for a decorated-but-non-storing Preference subclass (e.g. a custom
            // widget with its own onClick/widgetLayoutResource but no value concept) - set
            // android:persistent="false" on it to mark it structural rather than value-bearing.
            !pref.isPersistent -> {
                containers += pref
            }

            else -> {
                valueBearing += pref
            }
        }
    }
}

/** Finds `UserSettings`' generated Kotlin-property getter for [key] (`darkMode` -> `getDarkMode()`), or null. */
private fun findGetter(
    settingsClass: Class<*>,
    key: String,
): java.lang.reflect.Method? {
    val getterName = "get" + key.replaceFirstChar(Char::uppercaseChar)
    return settingsClass.methods.firstOrNull { it.name == getterName && it.parameterCount == 0 }
}

/**
 * Verifies every persisting preference in [xmlRes] is correctly bound to a `UserSettings`-shaped class:
 *
 * 1. Every value-bearing widget's `android:key` matches a property on the settings class produced by [factory]
 *    - catches a typo'd key or a property renamed without updating the XML.
 * 2. No purely-navigational key (category, click-target `PreferenceScreen`/`Preference`, or any
 *    `android:persistent="false"` widget) collides with a property name.
 * 3. Every value-bearing widget declares `android:defaultValue` in the XML itself (checked via
 *    [collectDeclaredDefaultValueKeys], not via post-inflation `SharedPreferences` contents - some widgets
 *    only call `persistX()` when the resolved value differs from their own in-memory starting field, so a
 *    write-based check would misreport a declared-but-coincidentally-unwritten default as missing).
 * 4. That declared default, once materialised into an otherwise-empty store through the widget's own real
 *    `dispatchSetInitialValue()`, leaves the corresponding property reading exactly what it reads on a
 *    completely empty store - catches a declared default that disagrees with the delegate's own default.
 *
 * The value comparison in (4) is type-agnostic - it compares whatever [factory] exposes, so `.mapped()`
 * properties are covered without this helper needing to know their wire type. A widget's persistence wire type
 * not matching its backing delegate's storage type (e.g. a `DropDownPreference`, which persists `String`, bound
 * to an `Int` delegate with no `.mapped()` bridge) surfaces as a [ClassCastException] out of [factory] - that
 * failure is deliberately left uncaught here, so the test output points straight at the mismatched delegate.
 *
 * Known gap: if a declared default happens to equal a widget's own in-memory starting field *and* disagrees
 * with the delegate's default, (3) sees a declared default (correctly) and (4) sees no persisted key to compare
 * (the widget never wrote), so the two can't cross-check each other for that one combination. Narrower than the
 * bug class this helper exists for (an *omitted* default), and not hit by any widget/delegate pairing in this
 * codebase today.
 */
fun <T> assertPreferenceXmlBoundToSettings(
    @XmlRes xmlRes: Int,
    factory: (SharedPreferences) -> T,
) {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val defaultsName = "preferenceXmlParityDefaults_${xmlRes}_${UUID.randomUUID()}"

    val activity = Robolectric.buildActivity(AppCompatActivity::class.java).setup().get()
    val fragment =
        PreferenceXmlParityFragment().apply {
            this.xmlRes = xmlRes
            this.sharedPreferencesName = defaultsName
        }
    activity.supportFragmentManager
        .beginTransaction()
        .add(fragment, "preferenceXmlParity")
        .commitNow()

    val valueBearing = mutableListOf<Preference>()
    val containers = mutableListOf<Preference>()
    walk(fragment.preferenceScreen, valueBearing, containers)

    val empty = factory(freshTestPreferences(context))
    val settingsClass = empty!!::class.java

    for (pref in containers) {
        val key = pref.key ?: continue
        if (findGetter(settingsClass, key) != null) {
            error(
                "Non-persisting preference \"$key\" (${pref.javaClass.simpleName}, xml $xmlRes) collides with a " +
                    "${settingsClass.simpleName} property name - rename the XML key or the property.",
            )
        }
    }

    val keyToGetter =
        valueBearing.associate { pref ->
            val key = pref.key ?: error("Persisting preference of type ${pref.javaClass.simpleName} (xml $xmlRes) has no android:key.")
            val getter =
                findGetter(settingsClass, key)
                    ?: error(
                        "Preference key \"$key\" (${pref.javaClass.simpleName}, xml $xmlRes) has no matching " +
                            "${settingsClass.simpleName} property - typo'd key, or the property was renamed without " +
                            "updating the XML.",
                    )
            key to getter
        }

    val declaredDefaultKeys = collectDeclaredDefaultValueKeys(context, xmlRes)
    for (key in keyToGetter.keys) {
        if (key !in declaredDefaultKeys) {
            error(
                "Preference \"$key\" (xml $xmlRes) has no android:defaultValue - it must be declared and equal " +
                    "${settingsClass.simpleName}.$key's delegate default.",
            )
        }
    }

    val defaultsPrefs = context.getSharedPreferences(defaultsName, Context.MODE_PRIVATE)
    val withDefaults = factory(defaultsPrefs)
    for ((key, getter) in keyToGetter) {
        val expected = getter.invoke(empty)
        val actual = getter.invoke(withDefaults)
        if (expected != actual) {
            error(
                "Preference \"$key\" (xml $xmlRes): android:defaultValue resolves to \"$actual\" but " +
                    "${settingsClass.simpleName}.$key's delegate default is \"$expected\" - keep them in sync.",
            )
        }
    }
}
