package com.github.lilinsong3.m3player.data.repository

import androidx.media3.common.MediaItem
import com.github.lilinsong3.m3player.ui.play.PlayingInfoModel
import kotlinx.coroutines.flow.Flow

interface PlayListRepository {
    suspend fun getMediaItem(mediaId: Int): MediaItem?
    suspend fun getMediaItems(page: Int, pageSize: Int): List<MediaItem>
    suspend fun getAllItems(): List<MediaItem>
    suspend fun countMatchedSongs(keyword: String): Int
    suspend fun countSongs(): Int
    suspend fun searchSongs(keyword: String, page: Int, pageSize: Int): List<MediaItem>
    suspend fun add(ids: List<Int>): List<Int>
    fun getPlayingInfoStream(): Flow<PlayingInfoModel>
}