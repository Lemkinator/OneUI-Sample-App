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

import android.content.SharedPreferences
import androidx.core.content.edit
import dev.oneuiproject.oneui.layout.ToolbarLayout.SearchOnActionMode
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/*
 * Advanced delegation, allowing for custom keys and defaults, and type safety. A little more verbose at the usage site.
 * https://www.youtube.com/watch?v=KFgb6l1PUJI&t=600s
 */

/** Returns a [SharedPreferenceDelegates] factory backed by this [SharedPreferences] instance. */
val SharedPreferences.delegates get() = SharedPreferenceDelegates(this)

/**
 * Wraps this delegate so [sanitize] is applied to every value on both read and write — e.g., clamping a size to a
 * valid range, or capping a list to a max length. Sanitizing on writing as well as reading keeps the stored value itself
 * valid, so every future read is inexpensive, and every consumer (not just this property) sees an already-sanitized value.
 */
fun <R, T> ReadWriteProperty<R, T>.sanitized(sanitize: (T) -> T): ReadWriteProperty<R, T> =
    object : ReadWriteProperty<R, T> {
        override fun getValue(
            thisRef: R,
            property: KProperty<*>,
        ): T = sanitize(this@sanitized.getValue(thisRef, property))

        override fun setValue(
            thisRef: R,
            property: KProperty<*>,
            value: T,
        ) = this@sanitized.setValue(thisRef, property, sanitize(value))
    }

/** Parses [raw] as a comma-joined int list; null (→ delegate falls back to its default) if null, empty, or unparsable. */
private fun parseIntList(raw: String?): List<Int>? =
    raw?.let { it.split(",").mapNotNull { part -> part.toIntOrNull() }.takeIf { list -> list.isNotEmpty() } }

/** Factory for type-safe [ReadWriteProperty] delegates backed by [SharedPreferences]. */
class SharedPreferenceDelegates(
    private val prefs: SharedPreferences,
) {
    /** Delegate that reads/writes a [Boolean] preference. */
    fun boolean(
        default: Boolean = false,
        key: String? = null,
    ): ReadWriteProperty<Any, Boolean> = create(default, key, prefs::getBoolean) { k, v -> prefs.edit { putBoolean(k, v) } }

    /** Delegate that reads/writes an [Int] preference. */
    fun int(
        default: Int = 0,
        key: String? = null,
    ): ReadWriteProperty<Any, Int> = create(default, key, prefs::getInt) { k, v -> prefs.edit { putInt(k, v) } }

    /** Delegate that reads/writes a [Float] preference. */
    fun float(
        default: Float = 0f,
        key: String? = null,
    ): ReadWriteProperty<Any, Float> = create(default, key, prefs::getFloat) { k, v -> prefs.edit { putFloat(k, v) } }

    /** Delegate that reads/writes a [Long] preference. */
    fun long(
        default: Long = 0L,
        key: String? = null,
    ): ReadWriteProperty<Any, Long> = create(default, key, prefs::getLong) { k, v -> prefs.edit { putLong(k, v) } }

    /** Delegate that reads/writes a [String] preference. */
    fun string(
        default: String = "",
        key: String? = null,
    ): ReadWriteProperty<Any, String> =
        create(default, key, { k, d -> prefs.getString(k, null) ?: d }, { k, v -> prefs.edit { putString(k, v) } })

    /** Delegate that reads/writes a [Set]<[String]> preference. */
    fun stringSet(
        default: Set<String> = emptySet(),
        key: String? = null,
    ): ReadWriteProperty<Any, Set<String>> =
        create(default, key, { k, d -> prefs.getStringSet(k, null) ?: d }, { k, v -> prefs.edit { putStringSet(k, v) } })

    /**
     * Delegate that reads/writes a [List]<[Int]> preference, stored as a comma-joined string. Writing [emptyList]
     * is stored as `""`, which reads back as [default] rather than `[]` (see [parseIntList]) — safe for call sites
     * whose default is itself empty, but a non-empty [default] will silently discard an empty-list write.
     */
    fun intList(
        default: List<Int> = emptyList(),
        key: String? = null,
    ): ReadWriteProperty<Any, List<Int>> =
        create(
            default,
            key,
            { k, d -> parseIntList(prefs.getString(k, null)) ?: d },
            { k, v -> prefs.edit { putString(k, v.joinToString(",")) } },
        )

    /** Delegate that reads/writes a dark mode flag stored as `"1"`/`"0"` for legacy HorizontalRadioPreference compatibility. */
    fun darkMode(
        default: Boolean = false,
        key: String? = null,
    ): ReadWriteProperty<Any, Boolean> =
        create(
            default,
            key,
            { k, d -> prefs.getString(k, if (d) "1" else "0") == "1" },
            { k, v -> prefs.edit { putString(k, if (v) "1" else "0") } },
        )

    /** Delegate that reads/writes a [SearchOnActionMode] preference. */
    fun searchOnActionMode(
        default: SearchOnActionMode = SearchOnActionMode.Dismiss,
        key: String? = null,
    ): ReadWriteProperty<Any, SearchOnActionMode> =
        create(
            default,
            key,
            { k, d ->
                when (prefs.getInt(k, searchOnActionModeOrdinal(d))) {
                    1 -> SearchOnActionMode.NoDismiss
                    2 -> SearchOnActionMode.Concurrent(null)
                    else -> SearchOnActionMode.Dismiss
                }
            },
            { k, v -> prefs.edit { putInt(k, searchOnActionModeOrdinal(v)) } },
        )

    private fun searchOnActionModeOrdinal(mode: SearchOnActionMode) =
        when (mode) {
            SearchOnActionMode.Dismiss -> 0
            SearchOnActionMode.NoDismiss -> 1
            is SearchOnActionMode.Concurrent -> 2
        }

    private fun <T> create(
        default: T,
        key: String?,
        getter: (key: String, default: T) -> T,
        setter: (key: String, value: T) -> Unit,
    ) = object : ReadWriteProperty<Any, T> {
        private fun key(property: KProperty<*>) = key ?: property.name

        override fun getValue(
            thisRef: Any,
            property: KProperty<*>,
        ): T = getter(key(property), default)

        override fun setValue(
            thisRef: Any,
            property: KProperty<*>,
            value: T,
        ) = setter(key(property), value)
    }
}
