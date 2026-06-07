package de.lemke.oneuisample.ui

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import de.lemke.oneuisample.BuildConfig
import de.lemke.oneuisample.domain.AcceptTosUseCase
import de.lemke.oneuisample.ui.util.EXTRA_VERSION_CODE
import de.lemke.oneuisample.ui.util.EXTRA_VERSION_NAME
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

sealed class OOBEEvent {
    data object NavigateToMain : OOBEEvent()
}

@HiltViewModel
class OOBEViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val acceptTos: AcceptTosUseCase,
) : ViewModel() {
    private val versionCode = savedStateHandle.get<Int>(EXTRA_VERSION_CODE) ?: BuildConfig.VERSION_CODE
    private val versionName = savedStateHandle.get<String>(EXTRA_VERSION_NAME) ?: BuildConfig.VERSION_NAME

    val events = Channel<OOBEEvent>(Channel.BUFFERED)

    fun onAcceptTos() {
        viewModelScope.launch {
            acceptTos(versionCode, versionName)
            delay(500.milliseconds)
            events.send(OOBEEvent.NavigateToMain)
        }
    }
}
