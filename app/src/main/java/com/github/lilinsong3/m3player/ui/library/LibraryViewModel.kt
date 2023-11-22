package com.github.lilinsong3.m3player.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.lilinsong3.m3player.data.model.SongItemModel
import com.github.lilinsong3.m3player.data.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(private val songRepository: SongRepository) :
    ViewModel() {

    // TODO: consider using the method that is MutableStateFlow<uiState>.asStateFlow()

    private val _cachedSongItems: MutableStateFlow<List<SongItemModel>> = MutableStateFlow(listOf())

    val libraryUiState: StateFlow<LibraryState> = _cachedSongItems.map {
        LibraryState.Success(
            it + songRepository.getAllLocalSongs(it.size / DEFAULT_PAGE_SIZE + 1, DEFAULT_PAGE_SIZE)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), LibraryState.Loading)

    fun loadMoreItems() {
        if (libraryUiState.value is LibraryState.Success) {
            _cachedSongItems.update { (libraryUiState.value as LibraryState.Success).songItemModels }
        }
    }

    companion object {
        private const val DEFAULT_PAGE_SIZE = 30
    }
}

sealed interface LibraryState {

    data object Loading : LibraryState

    data class Success(
        val songItemModels: List<SongItemModel> = listOf()
    ) : LibraryState

    data class Error(val msg: String) : LibraryState
}