package com.github.lilinsong3.m3player.data.repository

import androidx.media3.common.MediaItem
import com.github.lilinsong3.m3player.data.model.PlayStateModel
import kotlinx.coroutines.flow.Flow

interface PlayListRepository {
    fun getAllItemsStream(): Flow<List<MediaItem>>
    suspend fun countSongs(): Int
    suspend fun add(ids: List<Long>): List<Long>
    suspend fun set(ids: List<Long>): List<Long>
    suspend fun getPagingSongIds(page: Int, pageSize: Int): List<Long>
    fun getPlayStateStream(): Flow<PlayStateModel>

    suspend fun savePlayState(playState: PlayStateModel)
}