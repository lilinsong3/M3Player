package com.github.lilinsong3.m3player.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.media3.common.MediaItem
import com.github.lilinsong3.m3player.data.model.SongItemModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class DefaultSongRepository @Inject constructor(@ApplicationContext private val context: Context) :
    SongRepository {

    companion object {
        private val QUERY_URI = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Audio.Media.getContentUri(
            MediaStore.VOLUME_EXTERNAL
        ) else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        private val QUERY_COLUMNS = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION
        )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createPagingQueryBundle(page: Int, pageSize: Int) = Bundle().apply {
        putInt(
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) ContentResolver.QUERY_ARG_LIMIT
            else ContentResolver.QUERY_ARG_SQL_LIMIT,
            pageSize
        )
        putInt(
            ContentResolver.QUERY_ARG_OFFSET,
            (page - 1) * pageSize
        ) // rows=10, page=2, pageSize=4, limit=4, offset=(2-1)*4
    }




    private fun getPagingQueryCursor(page: Int, pageSize: Int): Cursor? = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) context.contentResolver.query(
            QUERY_URI,
            QUERY_COLUMNS,
            null,
            null,
            "LIMIT $pageSize OFFSET ${(page - 1) * pageSize}"
        ) else context.contentResolver.query(
            QUERY_URI,
            QUERY_COLUMNS,
            createPagingQueryBundle(page, pageSize),
            null
        )

    private fun getIdQueryCursor(id: String): Cursor? = context.contentResolver.query(
        QUERY_URI,
        QUERY_COLUMNS,
        "${MediaStore.Audio.Media._ID} = ?",
        arrayOf(id),
        null
    )

    override fun getLocalSongsStream(page: Int, pageSize: Int): Flow<List<SongItemModel>> = flow {
        val songs = mutableListOf<SongItemModel>()
        getPagingQueryCursor(page, pageSize)?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                songs += SongItemModel(
                    cursor.getLong(idCol),
                    cursor.getString(titleCol),
                    cursor.getString(artistCol),
                    cursor.getString(albumCol),
                    ContentUris.withAppendedId(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        cursor.getLong(albumIdCol)
                    ),
                    cursor.getLong(durationCol).milliseconds.toComponents { h, m, s -> "${h}:${m}:${s}" } // 转为时分秒格式
                )
            }
        }
        emit(songs)
    }.flowOn(Dispatchers.IO)

    override suspend fun getSongByMediaId(mediaId: String): MediaItem? = getIdQueryCursor(mediaId)?.use {  cursor ->
        cursor.run {
            if (cursor.moveToNext()) {
//                SongItemModel(
//                    getLong(getColumnIndexOrThrow(MediaStore.Audio.Media._ID)),
//                    getString(getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)),
//                    getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)),
//                    getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)),
//                    ContentUris.withAppendedId(
//                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
//                        getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
//                    ),
//                    getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)).milliseconds.toComponents { h, m, s -> "${h}:${m}:${s}" } // 转为时分秒格式
//                )
                TODO("return a full MediaItem")
            } else {
                null
            }
        }
    }
}