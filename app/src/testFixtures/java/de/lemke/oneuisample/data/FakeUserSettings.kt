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

import kotlinx.coroutines.CoroutineScope

/**
 * Real [UserSettings] over a [FakeSharedPreferences], for pure-JVM specs with no Context
 * (Hilt/Robolectric structurally unavailable). The spec owns [scope] and cancels it after each test; its dispatcher
 * must be unconfined so SharingStarted.Eagerly propagates synchronously.
 */
fun fakeUserSettings(scope: CoroutineScope): UserSettings = UserSettings(FakeSharedPreferences(), scope)
