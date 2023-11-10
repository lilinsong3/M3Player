package com.github.lilinsong3.m3player.data.repository

import androidx.media3.common.MediaItem

interface PlayListRepository {
    suspend fun getMediaItemStream(mediaId: String) : MediaItem
}