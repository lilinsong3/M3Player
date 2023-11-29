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
import androidx.media3.common.MediaMetadata
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
        private val QUERY_URI =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            ) else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        private val BRIEF_COLUMNS_IN_MEDIA = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ALBUM_ID,
            MediaStore.Audio.Media.DURATION
        )

        // @RequiresApi(Build.VERSION_CODES.R)
        private val ENOUGH_COLUMNS_IN_MEDIA = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ALBUM_ID, /* can be used to build artwork uri*/
            MediaStore.Audio.Media.TRACK, /*MediaStore.Audio.Media.CD_TRACK_NUMBER*/
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.COMPOSER,
            MediaStore.Audio.Media.DURATION
            // other fields of MediaMetaData probably needed:
            // subtitle
            // description
            // userRating
            // overallRating
            // recordingMonth
            // recordingDay
            // releaseYear
            // releaseMonth
            // releaseDay
            // conductor
            // totalDiscCount
            // station
        )
            get() = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) field else field.plus(
                arrayOf(
                    MediaStore.Audio.Media.ALBUM_ARTIST,
                    MediaStore.Audio.Media.NUM_TRACKS,
                    MediaStore.Audio.Media.WRITER,
                    MediaStore.Audio.Media.DISC_NUMBER,
                    MediaStore.Audio.Media.GENRE,
                    MediaStore.Audio.Media.COMPILATION
                )
            )
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createPagingQueryBundle(page: Int, pageSize: Int) = Bundle().apply {
        putInt(
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) ContentResolver.QUERY_ARG_LIMIT
            else ContentResolver.QUERY_ARG_SQL_LIMIT, pageSize
        )
        putInt(
            ContentResolver.QUERY_ARG_OFFSET, (page - 1) * pageSize
        ) // rows=10, page=2, pageSize=4, limit=4, offset=(2-1)*4
    }


    private fun getPagingQueryCursor(page: Int, pageSize: Int): Cursor? =
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) context.contentResolver.query(
            QUERY_URI,
            BRIEF_COLUMNS_IN_MEDIA,
            null,
            null,
            "LIMIT $pageSize OFFSET ${(page - 1) * pageSize}"
        ) else context.contentResolver.query(
            QUERY_URI, BRIEF_COLUMNS_IN_MEDIA, createPagingQueryBundle(page, pageSize), null
        )

    private fun getIdQueryCursor(id: String): Cursor? = context.contentResolver.query(
        QUERY_URI, ENOUGH_COLUMNS_IN_MEDIA, "${MediaStore.Audio.Media._ID} = ?", arrayOf(id), null
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
                songs += SongItemModel(cursor.getLong(idCol),
                    cursor.getString(titleCol),
                    cursor.getString(artistCol),
                    cursor.getString(albumCol),
                    ContentUris.withAppendedId(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, cursor.getLong(albumIdCol)
                    ),
                    cursor.getLong(durationCol).milliseconds.toComponents { h, m, s -> "${h}:${m}:${s}" } // 转为时分秒格式
                )
            }
        }
        emit(songs)
    }.flowOn(Dispatchers.IO)

    private fun extractMediaItem(cursor: Cursor): MediaItem? = cursor.run {
        if (moveToNext()) {
            val metadataBuilder = MediaMetadata.Builder()
                .setTitle(getString(getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)))
                .setArtist(getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)))
                .setAlbumTitle(getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)))
                //.setAlbumArtist(albumArtist)
                .setDisplayTitle(getString(getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)))
                //.setSubtitle(subtitle)
                //.setDescription(description)
                .setArtworkUri(
                    ContentUris.withAppendedId(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                    )
                ).setTrackNumber(getInt(getColumnIndexOrThrow(MediaStore.Audio.Media.TRACK)))
                .setRecordingYear(getInt(getColumnIndexOrThrow(MediaStore.Audio.Media.YEAR)))
                //.setRecordingMonth(recordingMonth)
                //.setRecordingDay(recordingDay)
                //.setReleaseYear(releaseYear)
                //.setReleaseMonth(releaseMonth)
                //.setReleaseDay(releaseDay)
                //.setWriter(writer)
                .setComposer(getString(getColumnIndexOrThrow(MediaStore.Audio.Media.COMPOSER)))
            //.setConductor(conductor)
            //.setDiscNumber(discNumber)
            //.setTotalDiscCount(totalDiscCount)
            //.setGenre(genre)
            //.setCompilation(compilation)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                metadataBuilder.setAlbumArtist(MediaStore.Audio.Media.ALBUM_ARTIST)
                    .setTotalTrackCount(getInt(getColumnIndexOrThrow(MediaStore.Audio.Media.NUM_TRACKS)))
                    .setWriter(getString(getColumnIndexOrThrow(MediaStore.Audio.Media.WRITER)))
                    .setDiscNumber(getInt(getColumnIndexOrThrow(MediaStore.Audio.Media.DISC_NUMBER)))
                    .setGenre(getString(getColumnIndexOrThrow(MediaStore.Audio.Media.GENRE)))
                    .setCompilation(getString(getColumnIndexOrThrow(MediaStore.Audio.Media.COMPILATION)))
            }
            MediaItem.Builder()
                .setMediaId(getLong(getColumnIndexOrThrow(MediaStore.Audio.Media._ID)).toString())
                .setUri(
                    ContentUris.withAppendedId(
                        QUERY_URI, getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                    )
                ).setMediaMetadata(
                    metadataBuilder.build()
                ).build()
        } else {
            null
        }
    }

    override suspend fun getSongByMediaId(mediaId: String): MediaItem? =
        getIdQueryCursor(mediaId)?.use(::extractMediaItem)
}