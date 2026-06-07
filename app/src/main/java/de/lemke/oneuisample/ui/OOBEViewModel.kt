package de.lemke.oneuisample.ui

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lemke.oneuisample.BuildConfig
import de.lemke.oneuisample.domain.CompleteOnboardingUseCase
import de.lemke.oneuisample.ui.util.EXTRA_VERSION_CODE
import de.lemke.oneuisample.ui.util.EXTRA_VERSION_NAME
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class OOBEEvent {
    data object NavigateToMain : OOBEEvent()
}

@HiltViewModel
class OOBEViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val completeOnboarding: CompleteOnboardingUseCase,
) : ViewModel() {
    private val versionCode = savedStateHandle.get<Int>(EXTRA_VERSION_CODE) ?: BuildConfig.VERSION_CODE
    private val versionName = savedStateHandle.get<String>(EXTRA_VERSION_NAME) ?: BuildConfig.VERSION_NAME

    private val _events = MutableSharedFlow<OOBEEvent>(extraBufferCapacity = 1)
    val events: SharedFlow<OOBEEvent> = _events.asSharedFlow()

    private val _isAccepting = MutableStateFlow(false)
    val isAccepting: StateFlow<Boolean> = _isAccepting.asStateFlow()

    @Suppress("TooGenericExceptionCaught")
    fun onAcceptTos() {
        if (_isAccepting.value) return
        viewModelScope.launch {
            _isAccepting.value = true
            try {
                completeOnboarding(versionCode, versionName)
                _events.emit(OOBEEvent.NavigateToMain)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Onboarding failed — re-enabling button", e)
                _isAccepting.value = false
            }
        }
    }

    private companion object {
        const val TAG = "OOBEViewModel"
    }
}
