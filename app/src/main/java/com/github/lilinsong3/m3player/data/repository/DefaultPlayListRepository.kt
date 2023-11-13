package com.github.lilinsong3.m3player.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStore
import androidx.media3.common.MediaItem
import com.github.lilinsong3.m3player.data.local.dao.PlayListDao
import com.github.lilinsong3.m3player.ui.play.PlayingInfoModel
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

val Context.playingInfoDataStore: DataStore<Preferences> by preferencesDataStore(name = "PlayingInfo")

class DefaultPlayListRepository @Inject constructor(
    private val playingInfoDataStore: DataStore<Preferences>,
    private val playListDao: PlayListDao
) : PlayListRepository {
    override suspend fun getMediaItem(mediaId: Int): MediaItem? {
        TODO("Not yet implemented")
    }

    override suspend fun getMediaItems(page: Int, pageSize: Int): List<MediaItem> {
        TODO("Not yet implemented")
    }

    override suspend fun getAllItems(): List<MediaItem> {
        TODO("Not yet implemented")
    }

    override suspend fun countMatchedSongs(keyword: String): Int = playListDao.queryMatchedNum(keyword)

    override suspend fun countSongs(): Int = playListDao.queryNum()

    override suspend fun searchSongs(keyword: String, page: Int, pageSize: Int): List<MediaItem> {
        TODO("Not yet implemented")
    }

    override suspend fun add(ids: List<Int>): List<Int> {
        TODO("Not yet implemented")
    }

    override fun getPlayingInfoStream(): Flow<PlayingInfoModel> {
        TODO("Not yet implemented")
    }
}