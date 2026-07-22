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
package de.lemke.oneuisample

import android.content.Context
import android.content.SharedPreferences
import androidx.test.core.app.ApplicationProvider
import java.util.UUID

/**
 * Returns a [SharedPreferences] instance backed by a UUID-named file, fresh by construction:
 * no manual `.edit().clear()`, no caller-supplied names, no collision risk on a reused device.
 * The only canonical way to get a settings-backed store in a test.
 */
fun freshTestPreferences(context: Context = ApplicationProvider.getApplicationContext()): SharedPreferences =
    context.getSharedPreferences("test_${UUID.randomUUID()}", Context.MODE_PRIVATE)
