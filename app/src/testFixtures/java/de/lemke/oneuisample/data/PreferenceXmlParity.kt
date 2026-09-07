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
import androidx.fragment.app.Fragment
import androidx.preference.EditTextPreference
import androidx.preference.ListPreference
import androidx.preference.MultiSelectListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceDataStore
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceGroup
import androidx.preference.SeekBarPreference
import androidx.preference.TwoStatePreference
import androidx.test.core.app.ApplicationProvider
import de.lemke.oneuisample.freshTestPreferences
import dev.oneuiproject.oneui.preference.ColorPickerPreference
import dev.oneuiproject.oneui.preference.HorizontalRadioPreference
import java.lang.reflect.Method
import org.robolectric.Robolectric
import org.robolectric.android.controller.ActivityController
import org.xmlpull.v1.XmlPullParser

private const val ANDROID_NS = "http://schemas.android.com/apk/res/android"

/**
 * Raw-XML scan (not the inflated tree) for every `android:key` whose element also declares `android:defaultValue`.
 * `android:key` may be a literal or a `@string` reference; only presence of `android:defaultValue` is checked.
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
 * Headless host: [onCreatePreferences] runs during `Fragment.onCreate`, no container view required. Public (not
 * `private`) because `FragmentManager` requires fragment classes to be public to recreate them from saved state.
 * Binds [dataStore] before inflating, so widgets initialise through the same seam production uses.
 */
class PreferenceXmlParityFragment : PreferenceFragmentCompat() {
    var xmlRes: Int = 0
    var dataStore: PreferenceDataStore? = null

    override fun onCreatePreferences(
        savedInstanceState: Bundle?,
        rootKey: String?,
    ) {
        preferenceManager.preferenceDataStore = dataStore
        setPreferencesFromResource(xmlRes, rootKey)
    }
}

/** Hosts [fragment] inside a freshly created, headless [AppCompatActivity] at CREATED. Caller must `destroy()` the controller. */
private fun hostFragmentAtCreated(fragment: Fragment): ActivityController<AppCompatActivity> {
    val controller = Robolectric.buildActivity(AppCompatActivity::class.java).create()
    controller
        .get()
        .supportFragmentManager
        .beginTransaction()
        .add(fragment, "preferenceXmlParity")
        .commitNow()
    return controller
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

/**
 * Finds the settings class's generated Kotlin getter for [key] (`darkMode` -> `getDarkMode()`), or null. A Kotlin
 * `var isFoo: Boolean` compiles its getter to `isFoo()`, not `getIsFoo()` - so an `is`-prefixed [key] also tries the
 * bare method name before giving up.
 */
private fun findGetter(
    settingsClass: Class<*>,
    key: String,
): Method? {
    val getterName = "get" + key.replaceFirstChar(Char::uppercaseChar)
    settingsClass.methods.firstOrNull { it.name == getterName && it.parameterCount == 0 }?.let { return it }
    if (!key.startsWith("is")) return null
    return settingsClass.methods.firstOrNull { it.name == key && it.parameterCount == 0 }
}

/** Setter twin of [findGetter]: `darkMode` -> `setDarkMode(...)`, `isFoo` -> `setFoo(...)`. */
private fun findSetter(
    settingsClass: Class<*>,
    key: String,
): Method? {
    val setterName = "set" + key.replaceFirstChar(Char::uppercaseChar)
    settingsClass.methods.firstOrNull { it.name == setterName && it.parameterCount == 1 }?.let { return it }
    if (!key.startsWith("is")) return null
    return settingsClass.methods.firstOrNull { it.name == "set" + key.removePrefix("is") && it.parameterCount == 1 }
}

/** The value a freshly inflated widget displays, in its persistence wire type. */
private fun Preference.displayedValue(): Any? =
    when (this) {
        is TwoStatePreference -> isChecked
        is ListPreference -> value
        is EditTextPreference -> text
        is MultiSelectListPreference -> values
        is SeekBarPreference -> value
        is HorizontalRadioPreference -> value
        is ColorPickerPreference -> value
        else -> error("No displayed-value probe for ${javaClass.name} - add a branch to Preference.displayedValue().")
    }

private fun Any?.describe(): String = if (this == null) "null" else "$this (${this::class.simpleName})"

/**
 * Verifies every persisting preference in each of [xmlRes] is correctly bound to the settings class produced by
 * [factory]:
 *
 * 1. Every value-bearing widget's `android:key` matches a property on the settings class - catches a typo'd key or a
 *    property renamed without updating the XML.
 * 2. No purely-navigational key (category, click-target `PreferenceScreen`/`Preference`, or any
 *    `android:persistent="false"` widget) collides with a property name.
 * 3. No two value-bearing widgets share a key.
 * 4. Every value-bearing widget declares `android:defaultValue` in the XML itself.
 * 5. The value the widget displays on an empty store equals what the property's delegate stores for its own
 *    default, compared in the widget's wire type - so a `.mapped()` bridge is transparent, a declared default that
 *    disagrees with the delegate default fails, and a wire-type mismatch (e.g. a `DropDownPreference` bound to an
 *    `Int` delegate without `.mapped()`) fails with both sides spelled out.
 *
 * Widgets inflate through the same `PreferenceDataStore` seam production uses, over a fresh store per XML.
 */
fun <T : Any> assertPreferenceXmlBoundToSettings(
    @XmlRes vararg xmlRes: Int,
    factory: (SharedPreferences) -> T,
) {
    xmlRes.forEach { checkXml(it, factory) }
}

private fun <T : Any> checkXml(
    @XmlRes xmlRes: Int,
    factory: (SharedPreferences) -> T,
) {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val fragment =
        PreferenceXmlParityFragment().apply {
            this.xmlRes = xmlRes
            dataStore = SharedPreferencesDataStore(freshTestPreferences(context))
        }
    val controller = hostFragmentAtCreated(fragment)
    try {
        val valueBearing = mutableListOf<Preference>()
        val containers = mutableListOf<Preference>()
        walk(fragment.preferenceScreen, valueBearing, containers)

        val empty = factory(freshTestPreferences(context))
        val settingsClass = empty::class.java

        for (pref in containers) {
            val key = pref.key ?: continue
            if (findGetter(settingsClass, key) != null) {
                error(
                    "Non-persisting preference \"$key\" (${pref.javaClass.simpleName}, xml $xmlRes) collides with a " +
                        "${settingsClass.simpleName} property name - rename the XML key or the property.",
                )
            }
        }

        val valueBearingKeys =
            valueBearing.map { pref ->
                pref.key ?: error("Persisting preference of type ${pref.javaClass.simpleName} (xml $xmlRes) has no android:key.")
            }
        val duplicateKeys =
            valueBearingKeys
                .groupingBy { it }
                .eachCount()
                .filterValues { it > 1 }
                .keys
        if (duplicateKeys.isNotEmpty()) {
            error(
                "Multiple value-bearing preferences in xml $xmlRes share key(s) $duplicateKeys - " +
                    "each android:key must be unique, or the collision hides a mis-bound widget.",
            )
        }

        val keyToGetter =
            valueBearing.associate { pref ->
                val key = pref.key!!
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

        val scratch = freshTestPreferences(context)
        val scratchSettings = factory(scratch)
        for (pref in valueBearing) {
            val key = pref.key!!
            val setter =
                findSetter(settingsClass, key)
                    ?: error("${settingsClass.simpleName}.$key (xml $xmlRes) has no setter - preference-backed properties must be `var`.")
            setter.invoke(scratchSettings, keyToGetter.getValue(key).invoke(empty))
            val stored = scratch.all[key]
            val shown = pref.displayedValue()
            if (stored != shown) {
                error(
                    "Preference \"$key\" (${pref.javaClass.simpleName}, xml $xmlRes) displays ${shown.describe()} on an " +
                        "empty store but ${settingsClass.simpleName}.$key stores ${stored.describe()} as its default - " +
                        "align android:defaultValue with the delegate default, or bridge the wire type with .mapped().",
                )
            }
        }
    } finally {
        controller.destroy()
    }
}
