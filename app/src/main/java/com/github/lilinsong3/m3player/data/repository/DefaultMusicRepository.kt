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
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class DefaultMusicRepository @Inject constructor(@ApplicationContext private val context: Context) :
    MusicRepository {

    companion object {
        private val MEDIA_URI =
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
        private val ENOUGH_MUSIC_COLUMNS = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ALBUM_ID, /* can be used to build artwork uri*/
            MediaStore.Audio.Media.TRACK, /*MediaStore.Audio.Media.CD_TRACK_NUMBER*/
            MediaStore.Audio.Media.YEAR,
            MediaStore.Audio.Media.COMPOSER
            //MediaStore.Audio.Media.DURATION,
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

        private val ALBUM_COLUMNS = arrayOf(
            MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ALBUM
        )
            get() = if (Build.VERSION.SDK_INT < Build.VERSION_CODES.R) field else field.plus(
                MediaStore.Audio.Media.ALBUM_ARTIST
            )

        private val ARTIST_COLUMNS = arrayOf(
            MediaStore.Audio.Media.ARTIST_ID, MediaStore.Audio.Media.ARTIST
        )


        @RequiresApi(Build.VERSION_CODES.R)
        private val GENRE_COLUMNS = arrayOf(
            MediaStore.Audio.Media.GENRE_ID, MediaStore.Audio.Media.GENRE
        )

        const val ROOT = "root"
        const val ALBUMS = "albums"
        const val ARTISTS = "artists"
        const val GENRES = "genres"
        const val PLAY_LIST = "playList"

        // TODO: 显示在屏幕的字符串改用字符串资源
        val musicDirMap = arrayOf(
            browsableDir(ROOT, "local library", MediaMetadata.MEDIA_TYPE_FOLDER_MIXED),
            browsableDir(ALBUMS, "albums", MediaMetadata.MEDIA_TYPE_FOLDER_ALBUMS),
            browsableDir(ARTISTS, "artists", MediaMetadata.MEDIA_TYPE_FOLDER_ARTISTS),
            browsableDir(GENRES, "genres", MediaMetadata.MEDIA_TYPE_FOLDER_GENRES),
            browsableDir(PLAY_LIST, "play list", MediaMetadata.MEDIA_TYPE_PLAYLIST)
        ).associateBy { it.mediaId }

        private fun browsableDir(mediaId: String, title: String, mediaType: Int): MediaItem =
            MediaItem.Builder().setMediaId(mediaId).setMediaMetadata(
                MediaMetadata.Builder().setTitle(title).setMediaType(mediaType).setIsBrowsable(true)
                    .setIsPlayable(false).build()
            ).build()
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
            MEDIA_URI,
            BRIEF_COLUMNS_IN_MEDIA,
            null,
            null,
            "LIMIT $pageSize OFFSET ${(page - 1) * pageSize}"
        ) else context.contentResolver.query(
            MEDIA_URI, BRIEF_COLUMNS_IN_MEDIA, createPagingQueryBundle(page, pageSize), null
        )

    private fun getPagingQueryCursor(
        queryCols: Array<String>,
        selectionColName: String? = null,
        selectionColArg: String? = null,
        page: Int,
        pageSize: Int
    ): Cursor? =
            /*if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O)*/
        // TODO: to test whether this will work or not when there is no limit for sdk version
        context.contentResolver.query(MEDIA_URI,
            queryCols,
            selectionColName?.let { "$it = ?" },
            selectionColArg?.let { arrayOf(it) },
            "LIMIT $pageSize OFFSET ${(page - 1) * pageSize}"
        ) /*else context.contentResolver.query(
            MEDIA_URI, BRIEF_COLUMNS_IN_MEDIA, createPagingQueryBundle(page, pageSize), null
        )*/

    private fun buildMusicItems(cursor: Cursor): List<MediaItem.Builder> = cursor.run {
        val items = listOf<MediaItem.Builder>()
        while (moveToNext()) {
            val metadataBuilder = MediaMetadata.Builder().setIsBrowsable(false).setIsPlayable(true)
                .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
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
            items + MediaItem.Builder().setUri(
                ContentUris.withAppendedId(
                    MEDIA_URI, getLong(getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
                )
            ).setMediaMetadata(
                metadataBuilder.build()
            )
        }
        items
    }

    private fun getMusicItemsById(
        id: String, page: Int = 1, pageSize: Int = 1
    ): List<MediaItem.Builder> = getPagingQueryCursor(
        ENOUGH_MUSIC_COLUMNS, MediaStore.Audio.Media._ID, id, page, pageSize
    )?.use(::buildMusicItems) ?: listOf()

    private fun buildAlbumItems(cursor: Cursor): List<MediaItem.Builder> = cursor.run {
        val items = listOf<MediaItem.Builder>()
        while (moveToNext()) {
            val metadataBuilder = MediaMetadata.Builder().setIsBrowsable(false).setIsPlayable(false)
                .setMediaType(MediaMetadata.MEDIA_TYPE_ALBUM)
                .setAlbumTitle(getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)))
                .setArtworkUri(
                    ContentUris.withAppendedId(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                    )
                )
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                metadataBuilder.setAlbumArtist(MediaStore.Audio.Media.ALBUM_ARTIST)
            }
            items + MediaItem.Builder().setUri(
                ContentUris.withAppendedId(
                    MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                    getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID))
                )
            ).setMediaMetadata(
                metadataBuilder.build()
            )
        }
        items
    }

    private fun getAlbumItemsById(
        id: String, page: Int = 1, pageSize: Int = 1
    ): List<MediaItem.Builder> = getPagingQueryCursor(
        ALBUM_COLUMNS, MediaStore.Audio.Media.ALBUM_ID, id, page, pageSize
    )?.use(::buildAlbumItems) ?: listOf()

    private fun buildArtistItems(cursor: Cursor): List<MediaItem.Builder> = cursor.run {
        val items = listOf<MediaItem.Builder>()
        while (moveToNext()) {
            val metadataBuilder = MediaMetadata.Builder().setIsBrowsable(false).setIsPlayable(false)
                .setMediaType(MediaMetadata.MEDIA_TYPE_ARTIST)
                .setArtist(getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)))
            items + MediaItem.Builder().setUri(
                ContentUris.withAppendedId(
                    MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI,
                    getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST_ID))
                )
            ).setMediaMetadata(
                metadataBuilder.build()
            )
        }
        items
    }

    private fun getArtistItemsById(
        id: String, page: Int = 1, pageSize: Int = 1
    ): List<MediaItem.Builder> = getPagingQueryCursor(
        ARTIST_COLUMNS, MediaStore.Audio.Media.ARTIST_ID, id, page, pageSize
    )?.use(::buildArtistItems) ?: listOf()


    @RequiresApi(Build.VERSION_CODES.R)
    private fun buildGenreItems(cursor: Cursor): List<MediaItem.Builder> = cursor.run {
        val items = listOf<MediaItem.Builder>()
        if (moveToNext()) {
            val metadataBuilder = MediaMetadata.Builder().setIsBrowsable(false).setIsPlayable(false)
                .setMediaType(MediaMetadata.MEDIA_TYPE_GENRE)
                .setArtist(getString(getColumnIndexOrThrow(MediaStore.Audio.Media.GENRE)))
            items + MediaItem.Builder().setUri(
                ContentUris.withAppendedId(
                    MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI,
                    getLong(getColumnIndexOrThrow(MediaStore.Audio.Media.GENRE_ID))
                )
            ).setMediaMetadata(
                metadataBuilder.build()
            )
        }
        items
    }

    @RequiresApi(Build.VERSION_CODES.R)
    private fun getGenreItemsById(
        id: String, page: Int = 1, pageSize: Int = 1
    ): List<MediaItem.Builder> = getPagingQueryCursor(
        GENRE_COLUMNS, MediaStore.Audio.Media.GENRE_ID, id, page, pageSize
    )?.use(::buildGenreItems) ?: listOf()

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

    /**
     * 根据媒体 ID 获取一个媒体项
     * @param mediaId id为固定的目录 ID （[ROOT], [ALBUMS], [ARTISTS], [GENRES], [PLAY_LIST]），或者标识单个媒体实体的组成 ID：idColName/MediaId，比如: MediaStore.Audio.Media._ID/13
     */
    override suspend fun getMediaItemByMediaId(mediaId: String): MediaItem? =
        withContext(Dispatchers.IO) {
            if (musicDirMap.contains(mediaId)) musicDirMap[mediaId] else {
                // mediaId: idCol/MediaId
                // for example: MediaStore.Audio.Media._ID/13
                val idColName = mediaId.substringBefore("/", MediaStore.Audio.Media._ID)
                val idValue = mediaId.substringAfter("/", "-1")
                when (idColName) {
                    MediaStore.Audio.Media._ID -> getMusicItemsById(idValue)
                    MediaStore.Audio.Media.ALBUM_ID -> getAlbumItemsById(idValue)
                    MediaStore.Audio.Media.ARTIST_ID -> getArtistItemsById(idValue)
                    else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && idColName == MediaStore.Audio.Media.GENRE_ID) getGenreItemsById(
                        idValue
                    ) else null
                }?.takeIf { it.size == 1 }?.get(0)?.setMediaId(mediaId)?.build()
            }
        }

    override suspend fun getPagingMediaChildrenByParentId(
        parentId: String, page: Int, pageSize: Int
    ): List<MediaItem> = withContext(Dispatchers.IO) {
        if (parentId == ROOT) musicDirMap.minus(ROOT).values.toList() else {
            // mediaId: idCol/MediaId
            // for example: MediaStore.Audio.Media._ID/13
            val idColName = parentId.substringBefore("/", MediaStore.Audio.Media._ID)
            val idValue = parentId.substringAfter("/", "-1")
            when (idColName) {
                MediaStore.Audio.Media._ID -> getMusicItemsById(idValue, page, pageSize)
                MediaStore.Audio.Media.ALBUM_ID -> getAlbumItemsById(idValue, page, pageSize)
                MediaStore.Audio.Media.ARTIST_ID -> getArtistItemsById(idValue, page, pageSize)
                else -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R && idColName == MediaStore.Audio.Media.GENRE_ID) getGenreItemsById(
                    idValue, page, pageSize
                ) else null
            }?.map { it.setMediaId(parentId).build() } ?: listOf()
        }
    }
}