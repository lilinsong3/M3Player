package com.github.lilinsong3.m3player.data.repository

import androidx.media3.common.MediaItem

interface PlayListRepository {
    suspend fun getMediaItem(mediaId: String): MediaItem?
    suspend fun getMediaItems(page: Int, pageSize: Int): List<MediaItem>
    suspend fun countMatchedSongs(keyword: String): Int
    suspend fun searchSongs(keyword: String, page: Int, pageSize: Int): List<MediaItem>
    suspend fun add(ids: List<Int>): List<Int>
}