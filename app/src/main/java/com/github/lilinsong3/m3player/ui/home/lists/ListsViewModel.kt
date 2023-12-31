package com.github.lilinsong3.m3player.ui.home.lists

import android.provider.MediaStore
import androidx.annotation.StringRes
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaBrowser
import com.github.lilinsong3.m3player.R
import com.github.lilinsong3.m3player.data.repository.DefaultMusicRepository
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

    fun loadMoreMusicItems(parentIndex: Int) {
        _uiState.update { it.copy(loadState = LoadState.Loading) }
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update { state ->
                val loadedListUiState = state.lists[parentIndex]
                val page = loadedListUiState.children.size / DEFAULT_PAGE_SIZE + 1
                try {
                    val newItemsResult = browser.getChildren(
                        loadedListUiState.list.mediaId,
                        page,
                        DEFAULT_PAGE_SIZE,
                        null
                    ).await()
                    if (newItemsResult.resultCode == LibraryResult.RESULT_SUCCESS) {
                        val result = newItemsResult.value!!
                        if (result.size == 0) return@update state.copy(loadState = LoadState.Success)
                        val updatedMusicList =
                            loadedListUiState.copy(children = loadedListUiState.children + result.map { item ->
                                MusicItemUiState(
                                    item,
                                    item.mediaMetadata.extras?.getString(MediaStore.Audio.Media.DURATION)
                                        ?: ""
                                )
                            })
                        state.copy(
                            lists = state.lists.toMutableList().also { mutableLists ->
                                mutableLists[parentIndex] = updatedMusicList
                            }, loadState = LoadState.Success
                        )
                    } else throw Exception()
                } catch (_: Exception) {
                    state.copy(loadState = LoadState.Error())
                }
            }
        }
    }

    fun onGroupExpand(groupIndex: Int) {
        if (_uiState.value.lists[groupIndex].children.isEmpty()) {
            loadMoreMusicItems(groupIndex)
        }
    }

    fun loadMoreMusicLists() {
        _uiState.update { it.copy(loadState = LoadState.Loading) }
        viewModelScope.launch(Dispatchers.IO) {
            _uiState.update {  state ->
                val page = state.lists.size / DEFAULT_PAGE_SIZE + 1
                try {
                    val newListsResult =
                        browser.getChildren(DefaultMusicRepository.ROOT, page, DEFAULT_PAGE_SIZE, null)
                            .await()
                    if (newListsResult.resultCode == LibraryResult.RESULT_SUCCESS) {
                        val result = newListsResult.value!!
                        if (result.size == 0) state.copy(loadState = LoadState.Success)
                        else state.copy(
                            lists = state.lists + result.map { MusicListUiState(it) },
                            loadState = LoadState.Success
                        )
                    } else throw Exception()
                } catch (_: Exception) {
                    state.copy(loadState = LoadState.Error())
                }
            }
        }
    }

    fun play(groupIndex: Int, childPosition: Int) {
        browser.setMediaItems(uiState.value.lists[groupIndex].children.map { it.item })
        _uiState.update {
            it.copy(
                lists = it.lists.toMutableList().also { parentList ->
                    parentList[groupIndex] = parentList[groupIndex].copy(
                        children = parentList[groupIndex].children.toMutableList().also { children ->
                            children[childPosition] = children[childPosition].copy(isPlaying = true)
                        }
                    )
                }
            )
        }
    }
}

data class MusicItemUiState(
    val item: MediaItem,
    val duration: String,
    val isPlaying: Boolean = false,
    val onItemClick: () -> Unit = {}
)

data class MusicListUiState(
    val list: MediaItem,
    val children: List<MusicItemUiState> = listOf(),
    val onListClick: () -> Unit = {}
)

sealed interface LoadState {
    data object Loading : LoadState
    data object Success : LoadState
    data class Error(@StringRes val msg: Int = R.string.err_hint) : LoadState
}

data class ListsUiState(
    val lists: List<MusicListUiState> = listOf(), val loadState: LoadState = LoadState.Loading
)