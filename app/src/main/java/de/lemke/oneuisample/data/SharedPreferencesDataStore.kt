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
import androidx.preference.PreferenceDataStore

/**
 * Routes `Preference` widget persistence into [prefs] instead of `PreferenceManager`'s default file. Set it via
 * `preferenceManager.preferenceDataStore = …` before inflating any preference XML.
 */
class SharedPreferencesDataStore(
    private val prefs: SharedPreferences,
) : PreferenceDataStore() {
    override fun putString(
        key: String,
        value: String?,
    ) = prefs.edit { putString(key, value) }

    override fun putStringSet(
        key: String,
        values: Set<String>?,
    ) = prefs.edit { putStringSet(key, values) }

    override fun putInt(
        key: String,
        value: Int,
    ) = prefs.edit { putInt(key, value) }

    override fun putLong(
        key: String,
        value: Long,
    ) = prefs.edit { putLong(key, value) }

    override fun putFloat(
        key: String,
        value: Float,
    ) = prefs.edit { putFloat(key, value) }

    override fun putBoolean(
        key: String,
        value: Boolean,
    ) = prefs.edit { putBoolean(key, value) }

    override fun getString(
        key: String,
        defValue: String?,
    ): String? = prefs.getString(key, defValue)

    override fun getStringSet(
        key: String,
        defValues: Set<String>?,
    ): Set<String>? = prefs.getStringSet(key, defValues)

    override fun getInt(
        key: String,
        defValue: Int,
    ): Int = prefs.getInt(key, defValue)

    override fun getLong(
        key: String,
        defValue: Long,
    ): Long = prefs.getLong(key, defValue)

    override fun getFloat(
        key: String,
        defValue: Float,
    ): Float = prefs.getFloat(key, defValue)

    override fun getBoolean(
        key: String,
        defValue: Boolean,
    ): Boolean = prefs.getBoolean(key, defValue)
}
