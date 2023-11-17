package com.github.lilinsong3.m3player.data.repository

import android.content.Context
import android.os.Build
import android.provider.MediaStore
import com.github.lilinsong3.m3player.data.model.AudioItemModel
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class DefaultSongRepository @Inject constructor(@ApplicationContext private val context: Context): SongRepository {
    override suspend fun getAllLocalSongs() {
        val songs = mutableListOf<AudioItemModel>()
        context.contentResolver.query(
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            },
            arrayOf(
                MediaStore.Audio.Media._ID,
                MediaStore.Audio.Media.TITLE,
                MediaStore.Audio.Media.ARTIST,
                MediaStore.Audio.Media.ALBUM,
                MediaStore.Audio.Media.ALBUM_ID,
                MediaStore.Audio.Media.DURATION
            ),
            null,
            null,
            null
        )?.use { cursor ->
            val idCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val titleCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val artistCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val albumCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val albumIdCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID)
            val durationCol = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            while (cursor.moveToNext()) {
                songs += AudioItemModel(
                    cursor.getLong(idCol),
                    cursor.getString(titleCol),
                    cursor.getString(artistCol),
                    cursor.getString(albumCol),
                    cursor.getLong(albumIdCol),
                    cursor.getLong(durationCol).toString() // TODO: 转为分钟格式
                )
            }
        }
        TODO("Not yet implemented")
    }
}