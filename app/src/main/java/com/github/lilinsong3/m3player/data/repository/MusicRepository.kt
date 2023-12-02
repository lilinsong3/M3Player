package com.github.lilinsong3.m3player.data.repository

import androidx.media3.common.MediaItem
import com.github.lilinsong3.m3player.data.model.SongItemModel
import kotlinx.coroutines.flow.Flow

interface MusicRepository {

    fun getLocalSongsStream(page: Int, pageSize: Int = 30) : Flow<List<SongItemModel>>
    suspend fun getMediaItemByMediaId(mediaId: String) : MediaItem?
    suspend fun getPagingMediaChildrenByParentId(parentId: String, page: Int, pageSize: Int = 30) : List<MediaItem>

    suspend fun countChildrenByParentId(parentId: String) : Int

    suspend fun countSearch(query: String) : Int
    suspend fun search(query: String, page: Int, pageSize: Int) : List<MediaItem>
}