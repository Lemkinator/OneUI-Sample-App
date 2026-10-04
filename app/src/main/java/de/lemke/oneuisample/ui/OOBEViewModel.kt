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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lemke.oneuisample.BuildConfig
import de.lemke.oneuisample.domain.CompleteOnboardingUseCase
import de.lemke.oneuisample.ui.util.EXTRA_VERSION_CODE
import de.lemke.oneuisample.ui.util.EXTRA_VERSION_NAME
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** The terms-of-service acceptance. The activity opens the main screen on [Accepted] and then reports it handled. */
enum class TosAcceptance {
    Idle,
    Accepting,
    Accepted,
    Navigated,
}

@HiltViewModel
class OOBEViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val completeOnboarding: CompleteOnboardingUseCase,
) : ViewModel() {
    private val versionCode = savedStateHandle.get<Int>(EXTRA_VERSION_CODE) ?: BuildConfig.VERSION_CODE
    private val versionName = savedStateHandle.get<String>(EXTRA_VERSION_NAME) ?: BuildConfig.VERSION_NAME

    val tosAcceptance: StateFlow<TosAcceptance>
        field = MutableStateFlow(TosAcceptance.Idle)

    fun onAcceptTos() {
        if (tosAcceptance.value != TosAcceptance.Idle) return
        tosAcceptance.value = TosAcceptance.Accepting
        viewModelScope.launch {
            completeOnboarding(versionCode, versionName)
            delay(500.milliseconds)
            tosAcceptance.value = TosAcceptance.Accepted
        }
    }

    fun onTosAcceptedHandled() {
        tosAcceptance.update { if (it == TosAcceptance.Accepted) TosAcceptance.Navigated else it }
    }
}
