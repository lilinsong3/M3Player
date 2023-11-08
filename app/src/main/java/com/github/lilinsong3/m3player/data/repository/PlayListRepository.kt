package com.github.lilinsong3.m3player.data.repository

import com.github.lilinsong3.m3player.data.model.PlayListModel
import com.google.common.util.concurrent.ListenableFuture

interface PlayListRepository {
    suspend fun getPlaylistStream() : PlayListModel
    fun getAudioLibraryRootStream() : ListenableFuture<androidx.media3.common.MediaItem>
}