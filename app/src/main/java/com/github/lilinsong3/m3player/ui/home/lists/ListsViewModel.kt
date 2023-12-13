package com.github.lilinsong3.m3player.ui.home.lists

import android.provider.MediaStore
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaBrowser
import com.github.lilinsong3.m3player.R
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListsViewModel @Inject constructor(private val browseFuture: ListenableFuture<MediaBrowser>) :
    ViewModel() {

    companion object {
        private const val DEFAULT_PAGE_SIZE = 30
    }


    private lateinit var browser: MediaBrowser

    init {
        viewModelScope.launch(Dispatchers.Main) {
            // browseFuture will be cancelled with coroutine together
            browser = browseFuture.await()
        }
    }

    private val _uiState = MutableStateFlow(ListsUiState())
    val uiState = _uiState

    fun loadMoreMusicItems(parentId: String) {
        _uiState.update { it.copy(loadState = LoadState.Loading) }
        getLoadedMusicList(parentId)?.let {
            val page = it.children.size / DEFAULT_PAGE_SIZE + 1
            val itIndex = _uiState.value.lists.indexOf(it)
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val newItemsResult =
                        browser.getChildren(parentId, page, DEFAULT_PAGE_SIZE, null).await()
                    if (newItemsResult.resultCode == LibraryResult.RESULT_SUCCESS) {
                        val updatedMusicList =
                            it.copy(children = it.children + newItemsResult.value!!.map { item ->
                                MusicItemUiState(
                                    item,
                                    item.mediaMetadata.extras?.getString(MediaStore.Audio.Media.DURATION)
                                        ?: ""
                                )
                            })
                        _uiState.update { state ->
                            state.copy(
                                lists = state.lists.toMutableList().also { list ->
                                    list[itIndex] = updatedMusicList
                                }, loadState = LoadState.Success
                            )
                        }
                    }
                } catch (_: Exception) {
                    _uiState.update { state ->
                        state.copy(loadState = LoadState.Error())
                    }
                }

            }
        }
    }

    private fun getLoadedMusicList(parentId: String): MusicListUiState? =
        _uiState.value.lists.firstOrNull {
            it.list.mediaId == parentId
        }
}

data class MusicItemUiState(
    val item: MediaItem,
    val duration: String,
    val isPlaying: Boolean = false,
    val onItemClick: () -> Unit = {}
)

data class MusicListUiState(
    val list: MediaItem, val children: List<MusicItemUiState>, val onListClick: () -> Unit
)

sealed interface LoadState {
    data object Loading : LoadState
    data object Success : LoadState
    data class Error(@StringRes val msg: Int = R.string.err_hint) : LoadState
}

data class ListsUiState(
    val lists: List<MusicListUiState> = listOf(), val loadState: LoadState = LoadState.Loading
)