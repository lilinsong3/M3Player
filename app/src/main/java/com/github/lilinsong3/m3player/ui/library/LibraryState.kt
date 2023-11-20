package com.github.lilinsong3.m3player.ui.library

import com.github.lilinsong3.m3player.data.model.SongItemModel

sealed class LibraryState(
    open val songItemModels: List<SongItemModel>  = listOf(),
    open val loading: Boolean
) {
    data class Success(
        override val songItemModels: List<SongItemModel> = listOf(),
        override val loading: Boolean = false
    ) : LibraryState(songItemModels, loading)

    data class Error(val msg: String, override val loading: Boolean = false) :
        LibraryState(loading = loading)
}
