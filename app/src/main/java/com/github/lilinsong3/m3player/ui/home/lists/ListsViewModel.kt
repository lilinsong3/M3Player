package com.github.lilinsong3.m3player.ui.home.lists

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.media3.common.MediaItem
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ListsViewModel @Inject constructor() : ViewModel() {
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