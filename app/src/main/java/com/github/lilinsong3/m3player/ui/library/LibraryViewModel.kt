package com.github.lilinsong3.m3player.ui.library

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.lilinsong3.m3player.data.model.SongItemModel
import com.github.lilinsong3.m3player.data.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(private val songRepository: SongRepository) :
    ViewModel() {

    private val _cachedSongItems: MutableStateFlow<List<SongItemModel>> = MutableStateFlow(listOf())

    @OptIn(ExperimentalCoroutinesApi::class)
    val libraryUiState: StateFlow<LibraryState> =
        _cachedSongItems.flatMapLatest<List<SongItemModel>, LibraryState> { oldItems ->
            songRepository.getLocalSongsStream(
                oldItems.size / DEFAULT_PAGE_SIZE + 1,
                DEFAULT_PAGE_SIZE
            ).map { LibraryState.Success(oldItems + it) }
        }.catch { emit(LibraryState.Error("出错了")) }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LibraryState.Loading)

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