package com.github.lilinsong3.m3player.data.repository

interface PlayListRepository {
    suspend fun getMediaItemStream(mediaId: String) : androidx.media3.common.MediaItem
}