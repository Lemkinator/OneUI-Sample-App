package de.lemke.oneuisample.data

import android.content.SharedPreferences
import androidx.core.content.edit
import dev.oneuiproject.oneui.layout.ToolbarLayout.SearchOnActionMode
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Advanced delegation, allowing for custom keys and defaults, and type safety. A little more verbose at the usage site.
 * https://www.youtube.com/watch?v=KFgb6l1PUJI&t=600s
 */
val SharedPreferences.delegates get() = SharedPreferenceDelegates(this)

/** Factory for type-safe [ReadWriteProperty] delegates backed by [SharedPreferences]. */
@Suppress("unused")
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
        create(default, key, { k, d -> prefs.getString(k, d) ?: d }) { k, v -> prefs.edit { putString(k, v) } }

    /** Delegate that reads/writes a [Set]<[String]> preference. */
    fun stringSet(
        default: Set<String> = emptySet(),
        key: String? = null,
    ): ReadWriteProperty<Any, Set<String>> =
        create(default, key, { k, d -> prefs.getStringSet(k, d) ?: d }, { k, v -> prefs.edit { putStringSet(k, v) } })

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
        key: String? = null,
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
