package com.github.lilinsong3.m3player.data.repository

import androidx.media3.common.MediaItem
import com.github.lilinsong3.m3player.data.model.SongItemModel
import kotlinx.coroutines.flow.Flow

interface SongRepository {

    fun getLocalSongsStream(page: Int, pageSize: Int = 30) : Flow<List<SongItemModel>>
    suspend fun getSongByMediaId(mediaId: String) : MediaItem?

}