package com.github.lilinsong3.m3player.ui.library

import com.github.lilinsong3.m3player.data.model.SongItemModel

sealed interface LibraryState {

    data object Loading : LibraryState

    data class Success(
        val songItemModels: List<SongItemModel> = listOf()
    ) : LibraryState

    data class Error(val msg: String) : LibraryState
}
