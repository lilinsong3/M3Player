package com.github.lilinsong3.m3player

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch

class MainViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    private val _event: MutableSharedFlow<Event> = MutableSharedFlow()
    val event = _event.asSharedFlow()
    private val _uiState = MutableStateFlow(MainUiState())

    fun send(e: Event) {
        viewModelScope.launch {
            _event.emit(e)
        }
    }
}

data class MainUiState(val showTopAppbar: Boolean = true)

sealed interface Event {
    data class AppBarVisibilityEvent(val shown: Boolean) : Event
    data class DrawerOpenEvent(val open: Boolean) : Event
}