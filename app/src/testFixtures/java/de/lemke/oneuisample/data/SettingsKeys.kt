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

// Mirror of common-utils' lib/src/testFixtures/.../SettingsKeys.kt (this app has no common-utils dependency). Keep byte-identical apart from package/imports.

/**
 * Pins the exact set of `SharedPreferences` keys [settingsClass] declares - one per `by preferences.delegates.*`
 * property, whose key defaults to the property name. Only properties declared on [settingsClass] itself count; a
 * superclass pins its own. Fails on any added, removed, or renamed property until [expected] is updated, so a rename
 * is a conscious choice rather than a silently orphaned stored value.
 */
fun assertDelegatedKeys(
    settingsClass: Class<*>,
    expected: Set<String>,
) {
    val actual =
        settingsClass.declaredFields
            .map { it.name }
            .filter { it.endsWith("\$delegate") }
            .map { it.removeSuffix("\$delegate") }
            .toSet()
    val unexpected = actual - expected
    val missing = expected - actual
    if (unexpected.isNotEmpty() || missing.isNotEmpty()) {
        error(
            "${settingsClass.simpleName} delegated keys drifted - declared but not expected: ${unexpected.sorted()}; " +
                "expected but not declared: ${missing.sorted()}. A renamed property changes its stored key; update the " +
                "expected set only for a deliberate add, remove, or rename.",
        )
    }
}
