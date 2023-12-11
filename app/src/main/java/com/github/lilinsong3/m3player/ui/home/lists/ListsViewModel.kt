package com.github.lilinsong3.m3player.ui.home.lists

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaBrowser
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ListsViewModel @Inject constructor(private val browseFuture: ListenableFuture<MediaBrowser>) : ViewModel() {
    private lateinit var browser: MediaBrowser

    init {
        viewModelScope.launch(Dispatchers.Main) {
            // browseFuture will be cancelled with coroutine together
            browser = browseFuture.await()
        }
    }
}

data class MusicItemUiState(
    val item: MediaItem,
    val thumbnail: Bitmap,
    val duration: String,
    val isPlaying: Boolean,
    val onItemClick: () -> Unit
)

data class MusicListUiState(
    val list: MediaItem,
    val children: List<MusicItemUiState>,
    val onListClick: () -> Unit
)

sealed interface ListsUiState {
    data object Loading : ListsUiState

    data class Success(
        val lists: List<MusicListUiState> = listOf()
    ) : ListsUiState

    data class Error(val msg: String) : ListsUiState
}