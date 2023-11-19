package com.github.lilinsong3.m3player.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.database.Cursor
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import com.github.lilinsong3.m3player.data.model.SongItemModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class DefaultSongRepository @Inject constructor(@ApplicationContext private val context: Context) :
    SongRepository {

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createPageQueryBundle(page: Int, pageSize: Int) = Bundle().apply {
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


    private fun getQueryCursor(page: Int, pageSize: Int): Cursor? {
        val queryUri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            ) else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val queryColumns = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION
        )
        return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) context.contentResolver.query(
            queryUri,
            queryColumns,
            null,
            null,
            "LIMIT $pageSize OFFSET ${(page - 1) * pageSize}"
        ) else context.contentResolver.query(
            queryUri,
            queryColumns,
            createPageQueryBundle(page, pageSize),
            null
        )
    }

    override suspend fun getAllLocalSongs(page: Int, pageSize: Int): List<SongItemModel> {
        val songs = mutableListOf<SongItemModel>()
        getQueryCursor(page, pageSize)?.use { cursor ->
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
        return songs
    }
}