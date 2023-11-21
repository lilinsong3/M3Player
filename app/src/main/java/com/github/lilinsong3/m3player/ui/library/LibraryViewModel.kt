package com.github.lilinsong3.m3player.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.lilinsong3.m3player.data.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.IOException
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(private val songRepository: SongRepository) :
    ViewModel() {
    private val _mutableSongsFlow: MutableStateFlow<LibraryState> = MutableStateFlow(
        LibraryState.Loading
    )
    val songsFlow: StateFlow<LibraryState> = _mutableSongsFlow.asStateFlow()

    companion object {
        private const val pageSize = 10
    }

    init {
        viewModelScope.launch {
            try {
                _mutableSongsFlow.value = LibraryState.Success(
                    songRepository.getAllLocalSongs(
                        1,
                        pageSize
                    )
                )
            } catch (ioe: IOException) {
                _mutableSongsFlow.value = LibraryState.Error("出错了")
            }
        }
    }

    fun send(e: LibraryEvent) {
        TODO("to impl this")
    }
}