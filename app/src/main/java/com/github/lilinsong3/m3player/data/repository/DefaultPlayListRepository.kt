package com.github.lilinsong3.m3player.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import com.github.lilinsong3.m3player.data.local.dao.PlayListDao
import com.github.lilinsong3.m3player.data.model.PlayStateModel
import com.github.lilinsong3.m3player.data.model.SongIdOnly
import com.github.lilinsong3.m3player.data.model.SongModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import java.io.IOException
import javax.inject.Inject

val Context.playStateDataStore: DataStore<Preferences> by preferencesDataStore(name = "PlayState")

class DefaultPlayListRepository @Inject constructor(
    private val playStateDataStore: DataStore<Preferences>, private val playListDao: PlayListDao
) : PlayListRepository {

    companion object {
        val SONG_ID = longPreferencesKey("song_id")
        val REPEAT_MODE = intPreferencesKey("repeat_mode")
        val SHUFFLE_MODE = booleanPreferencesKey("shuffle_mode")
        val CURRENT_POSITION = longPreferencesKey("curr_position")
    }

    override suspend fun getMediaItem(mediaId: Int): MediaItem? = withContext(Dispatchers.IO) {
        SongModel.toMediaItem(playListDao.querySongById(mediaId))
    }

    override suspend fun getMediaItems(page: Int, pageSize: Int): List<MediaItem> =
        withContext(Dispatchers.IO) {
            playListDao.querySongs(page, pageSize).mapNotNull { SongModel.toMediaItem(it) }
        }

    override fun getAllItemsStream(): Flow<List<MediaItem>> =
        playListDao.observableQueryAll().map { list -> list.mapNotNull(SongModel::toMediaItem) }

    override suspend fun countMatchedSongs(keyword: String): Int = withContext(Dispatchers.IO) {
        playListDao.queryMatchedNum(keyword)
    }

    override suspend fun countSongs(): Int = withContext(Dispatchers.IO) { playListDao.count() }

    override suspend fun searchSongs(keyword: String, page: Int, pageSize: Int): List<MediaItem> =
        withContext(Dispatchers.IO) {
            playListDao.searchSongs(keyword, page, pageSize)
                .mapNotNull { SongModel.toMediaItem(it) }
        }

    override suspend fun add(ids: List<Long>): List<Long> = withContext(Dispatchers.IO) {
        playListDao.upsertByIds(ids.map { SongIdOnly(it) })
    }

    override suspend fun getPagingSongIds(page: Int, pageSize: Int): List<Long> = withContext(Dispatchers.IO) {
        playListDao.queryPagingSongIds(page, pageSize)
    }

    override fun getPlayStateStream(): Flow<PlayStateModel> =
        playStateDataStore.data.catch { exception ->
            // dataStore.data throws an IOException when an error is encountered when reading data
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }.map { preferences ->
            PlayStateModel(
                preferences[SONG_ID] ?: 0L,
                preferences[REPEAT_MODE] ?: Player.REPEAT_MODE_ALL,
                preferences[SHUFFLE_MODE] ?: true,
                preferences[CURRENT_POSITION] ?: 0L,
            )
        }

    override suspend fun savePlayState(playState: PlayStateModel) {
        playStateDataStore.edit { prefs ->
            playState.run {
                prefs[SONG_ID] = songId
                prefs[REPEAT_MODE] = repeatMode
                prefs[SHUFFLE_MODE] = shuffleMode
                prefs[CURRENT_POSITION] = currentPosition
            }
        }
    }
}