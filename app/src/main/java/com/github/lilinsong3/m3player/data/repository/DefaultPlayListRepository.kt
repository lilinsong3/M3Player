package com.github.lilinsong3.m3player.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.github.lilinsong3.m3player.data.local.dao.PlayListDao
import com.github.lilinsong3.m3player.data.model.PlayingInfoModel
import com.github.lilinsong3.m3player.data.model.SongIdOnly
import com.github.lilinsong3.m3player.data.model.SongModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

val Context.playingInfoDataStore: DataStore<Preferences> by preferencesDataStore(name = "PlayingInfo")

class DefaultPlayListRepository @Inject constructor(
    private val playingInfoDataStore: DataStore<Preferences>, private val playListDao: PlayListDao
) : PlayListRepository {

    companion object {
        val SONG_ID = longPreferencesKey("song_id")
        val REPEAT_MODE = intPreferencesKey("repeat_mode")
        val SHUFFLE_MODE = booleanPreferencesKey("shuffle_mode")
        val CURRENT_POSITION = longPreferencesKey("curr_position")
    }

    override suspend fun getMediaItem(mediaId: Int): MediaItem? =
        SongModel.toMediaItem(playListDao.querySongById(mediaId))

    override suspend fun getMediaItems(page: Int, pageSize: Int): List<MediaItem> =
        playListDao.querySongs(page, pageSize).mapNotNull { SongModel.toMediaItem(it) }

    override suspend fun getAllItems(): List<MediaItem> =
        playListDao.queryAll().mapNotNull { SongModel.toMediaItem(it) }

    override suspend fun countMatchedSongs(keyword: String): Int =
        playListDao.queryMatchedNum(keyword)

    override suspend fun countSongs(): Int = playListDao.queryNum()

    override suspend fun searchSongs(keyword: String, page: Int, pageSize: Int): List<MediaItem> =
        playListDao.searchSongs(keyword, page, pageSize).mapNotNull { SongModel.toMediaItem(it) }

    override suspend fun add(ids: List<Long>): List<Long> = withContext(Dispatchers.IO) {
        playListDao.upsertByIds(ids.map { SongIdOnly(it) })
    }

    override fun getPlayingInfoStream(): Flow<PlayingInfoModel> =
        playingInfoDataStore.data.catch { exception ->
                // dataStore.data throws an IOException when an error is encountered when reading data
                if (exception is IOException) {
                    emit(emptyPreferences())
                } else {
                    throw exception
                }
            }.map { preferences ->
                PlayingInfoModel(
                    preferences[SONG_ID] ?: 0L,
                    preferences[REPEAT_MODE] ?: Player.REPEAT_MODE_ALL,
                    preferences[SHUFFLE_MODE] ?: true,
                    preferences[CURRENT_POSITION] ?: 0L,
                )
            }

    override suspend fun savePlayingInfo(playingInfo: PlayingInfoModel) {
        playingInfoDataStore.edit { prefs ->
            playingInfo.run {
                prefs[SONG_ID]  = songId
                prefs[REPEAT_MODE]  = repeatMode
                prefs[SHUFFLE_MODE]  = shuffleMode
                prefs[CURRENT_POSITION]  = currentPosition
            }
        }
    }
}