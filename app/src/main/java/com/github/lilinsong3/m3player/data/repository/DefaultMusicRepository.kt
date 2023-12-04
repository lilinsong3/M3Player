package com.github.lilinsong3.m3player.data.repository

import android.content.ContentResolver
import android.content.ContentUris
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import com.github.lilinsong3.m3player.data.model.SongItemModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.time.Duration.Companion.milliseconds

class DefaultMusicRepository @Inject constructor(
    private val contentResolver: ContentResolver,
    private val playListRepo: PlayListRepository
) : MusicRepository {

    companion object {
        private val MEDIA_URI =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Audio.Media.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            ) else MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

        private val ALBUMS_URI =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Audio.Albums.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            ) else MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI

        private val ARTISTS_URI =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Audio.Artists.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            ) else MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI

        private val GENRES_URI =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) MediaStore.Audio.Genres.getContentUri(
                MediaStore.VOLUME_EXTERNAL
            ) else MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI

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

        private val ALBUMS_COLUMNS = arrayOf(
            MediaStore.Audio.Albums._ID,
            MediaStore.Audio.Albums.ALBUM,
            MediaStore.Audio.Albums.ARTIST
        )

        private val ARTISTS_COLUMNS = arrayOf(
            MediaStore.Audio.Artists._ID, MediaStore.Audio.Artists.ARTIST
        )


        // @RequiresApi(Build.VERSION_CODES.R)
        private val GENRES_COLUMNS = arrayOf(
            MediaStore.Audio.Genres._ID, MediaStore.Audio.Genres.NAME
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
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) contentResolver.query(
            MEDIA_URI,
            BRIEF_COLUMNS_IN_MEDIA,
            null,
            null,
            "LIMIT $pageSize OFFSET ${(page - 1) * pageSize}"
        ) else contentResolver.query(
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
        contentResolver.query(MEDIA_URI,
            queryCols,
            selectionColName?.let { "$it = ?" },
            selectionColArg?.let { arrayOf(it) },
            "LIMIT $pageSize OFFSET ${(page - 1) * pageSize}"
        ) /*else contentResolver.query(
            MEDIA_URI, BRIEF_COLUMNS_IN_MEDIA, createPagingQueryBundle(page, pageSize), null
        )*/

    private fun buildMusicItems(cursor: Cursor): List<MediaItem> = cursor.run {
        val items = mutableListOf<MediaItem>()
        while (moveToNext()) {
            val metadataBuilder = MediaMetadata.Builder()
                .setIsBrowsable(false)
                .setIsPlayable(true)
                .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                .setTitle(getString(getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)))
                .setArtist(getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)))
                .setAlbumTitle(getString(getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)))
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
            //.setTotalDiscCount(totalDiscCount)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                metadataBuilder.setAlbumArtist(MediaStore.Audio.Media.ALBUM_ARTIST)
                    .setTotalTrackCount(getInt(getColumnIndexOrThrow(MediaStore.Audio.Media.NUM_TRACKS)))
                    .setWriter(getString(getColumnIndexOrThrow(MediaStore.Audio.Media.WRITER)))
                    .setDiscNumber(getInt(getColumnIndexOrThrow(MediaStore.Audio.Media.DISC_NUMBER)))
                    .setGenre(getString(getColumnIndexOrThrow(MediaStore.Audio.Media.GENRE)))
                    .setCompilation(getString(getColumnIndexOrThrow(MediaStore.Audio.Media.COMPILATION)))
            }
            val id = getLong(getColumnIndexOrThrow(MediaStore.Audio.Media._ID))
            items += MediaItem.Builder()
                .setMediaId("${MediaStore.Audio.Media._ID}/$id")
                .setUri(ContentUris.withAppendedId(MEDIA_URI, id))
                .setMediaMetadata(metadataBuilder.build())
                .build()
        }
        items
    }

    private fun getMusicItemsById(
        id: String, page: Int = 1, pageSize: Int = 1
    ): List<MediaItem> = contentResolver.query(
        MEDIA_URI,
        ENOUGH_MUSIC_COLUMNS,
        "${MediaStore.Audio.Media._ID} = ?",
        arrayOf(id),
        "LIMIT $pageSize OFFSET ${(page - 1) * pageSize}"
    )?.use { buildMusicItems(it) } ?: listOf()

    private fun buildAlbumItems(cursor: Cursor): List<MediaItem> = cursor.run {
        val items = mutableListOf<MediaItem>()
        while (moveToNext()) {
            val id = getLong(getColumnIndexOrThrow(MediaStore.Audio.Albums._ID))
            val metadataBuilder = MediaMetadata.Builder()
                .setIsBrowsable(false)
                .setIsPlayable(false)
                .setMediaType(MediaMetadata.MEDIA_TYPE_ALBUM)
                .setAlbumTitle(getString(getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM)))
                .setArtworkUri(
                    ContentUris.withAppendedId(
                        MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
                        id
                    )
                ).setAlbumArtist(MediaStore.Audio.Albums.ARTIST)
            items += MediaItem.Builder()
                .setMediaId("${MediaStore.Audio.Media.ALBUM_ID}/$id")
                .setUri(ContentUris.withAppendedId(MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, id))
                .setMediaMetadata(metadataBuilder.build())
                .build()
        }
        items
    }

    private fun getAlbumItemsById(
        id: String, page: Int = 1, pageSize: Int = 1
    ): List<MediaItem> = contentResolver.query(
        ALBUMS_URI,
        ALBUMS_COLUMNS,
        "${MediaStore.Audio.Albums._ID} = ?",
        arrayOf(id),
        "LIMIT $pageSize OFFSET ${(page - 1) * pageSize}"
    )?.use { buildAlbumItems(it) } ?: listOf()

    private fun buildArtistItems(cursor: Cursor): List<MediaItem> = cursor.run {
        val items = mutableListOf<MediaItem>()
        while (moveToNext()) {
            val metadataBuilder = MediaMetadata.Builder()
                .setIsBrowsable(false)
                .setIsPlayable(false)
                .setMediaType(MediaMetadata.MEDIA_TYPE_ARTIST)
                .setArtist(getString(getColumnIndexOrThrow(MediaStore.Audio.Artists.ARTIST)))
            val id = getLong(getColumnIndexOrThrow(MediaStore.Audio.Artists._ID))
            items += MediaItem.Builder()
                .setMediaId("${MediaStore.Audio.Media.ARTIST_ID}/$id")
                .setUri(ContentUris.withAppendedId(MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, id))
                .setMediaMetadata(metadataBuilder.build())
                .build()
        }
        items
    }

    private fun getArtistItemsById(
        id: String, page: Int = 1, pageSize: Int = 1
    ): List<MediaItem> = contentResolver.query(
        ARTISTS_URI,
        ARTISTS_COLUMNS,
        "${MediaStore.Audio.Artists._ID} = ?",
        arrayOf(id),
        "LIMIT $pageSize OFFSET ${(page - 1) * pageSize}"
    )?.use { buildArtistItems(it) } ?: listOf()

    private fun buildGenreItems(cursor: Cursor): List<MediaItem> = cursor.run {
        val items = mutableListOf<MediaItem>()
        if (moveToNext()) {
            val metadataBuilder = MediaMetadata.Builder().setIsBrowsable(false).setIsPlayable(false)
                .setMediaType(MediaMetadata.MEDIA_TYPE_GENRE)
                .setArtist(getString(getColumnIndexOrThrow(MediaStore.Audio.Genres.NAME)))
            val id = getLong(getColumnIndexOrThrow(MediaStore.Audio.Genres._ID))
            items += MediaItem.Builder()
                .setMediaId(/*MediaStore.Audio.Media.GENRE_ID*/"genre_id/$id")
                .setUri(ContentUris.withAppendedId(MediaStore.Audio.Genres.EXTERNAL_CONTENT_URI, id))
                .setMediaMetadata(metadataBuilder.build())
                .build()
        }
        items
    }

    private fun getGenreItemsById(
        id: String, page: Int = 1, pageSize: Int = 1
    ): List<MediaItem> = contentResolver.query(
        GENRES_URI,
        GENRES_COLUMNS,
        "${MediaStore.Audio.Genres._ID} = ?",
        arrayOf(id),
        "LIMIT $pageSize OFFSET ${(page - 1) * pageSize}"
    )?.use { buildGenreItems(it) } ?: listOf()

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
                    /*MediaStore.Audio.Media.GENRE_ID*/
                    "genre_id" -> getGenreItemsById(idValue)
                    else -> null
                }?.takeIf { it.size == 1 }?.get(0)
            }
        }

    override suspend fun getPagingMediaChildrenByParentId(
        parentId: String, page: Int, pageSize: Int
    ): List<MediaItem> = withContext(Dispatchers.IO) {
        when (parentId) {
            // 获取根目录下的子目录
            ROOT -> musicDirMap.minus(ROOT).values.toList()
            // 获取播放列表
            PLAY_LIST -> {
                val ids = playListRepo.getPagingSongIds(page, pageSize).map { it.toString() }.toTypedArray()
                contentResolver.query(
                    MEDIA_URI,
                    ENOUGH_MUSIC_COLUMNS,
                    "${MediaStore.Audio.Media._ID} IN (${ids.joinToString { "?" }})",
                    ids,
                    null
                )?.use { buildMusicItems(it) } ?: listOf()
            }
            // 获取专辑列表
            ALBUMS -> contentResolver.query(
                ALBUMS_URI,
                ALBUMS_COLUMNS,
                null,
                null,
                "LIMIT $pageSize OFFSET ${(page - 1) * pageSize}"
            )?.use { buildAlbumItems(it) } ?: listOf()
            // 获取艺术家列表
            ARTISTS -> contentResolver.query(
                ARTISTS_URI,
                ARTISTS_COLUMNS,
                null,
                null,
                "LIMIT $pageSize OFFSET ${(page - 1) * pageSize}"
            )?.use { buildArtistItems(it) } ?: listOf()
            GENRES -> contentResolver.query(
                GENRES_URI,
                GENRES_COLUMNS,
                null,
                null,
                "LIMIT $pageSize OFFSET ${(page - 1) * pageSize}"
            )?.use { buildGenreItems(it) } ?: listOf()
            else -> listOf()
        }
    }

    private fun queryCount(
        tableUri: Uri,
        whereClause: String? = null,
        clauseArgs: Array<String>? = null,
        sortOrder: String? = null
    ): Int = contentResolver.query(
        tableUri, arrayOf(MediaStore.MediaColumns._COUNT), whereClause, clauseArgs, sortOrder
    )?.use {
        if (it.moveToNext()) it.getInt(it.getColumnIndexOrThrow(MediaStore.MediaColumns._COUNT))
        else 0
    } ?: 0

    override suspend fun countChildrenByParentId(parentId: String): Int =
        withContext(Dispatchers.IO) {
            when (parentId) {
                ROOT -> musicDirMap.size - 1
                PLAY_LIST -> playListRepo.countSongs()
                ALBUMS -> queryCount(ALBUMS_URI)
                ARTISTS -> queryCount(ARTISTS_URI)
                GENRES -> queryCount(GENRES_URI)
                else -> 0
            }
        }

    private fun getSearchableClauses() : List<String> {
        val searchableClauseCols = mutableListOf(
            "${MediaStore.Audio.Media.TITLE} LIKE ?",
            "${MediaStore.Audio.Media.ARTIST} LIKE ?",
            "${MediaStore.Audio.Media.ALBUM} LIKE ?",
            "${MediaStore.Audio.Media.DISPLAY_NAME} LIKE ?",
            "${MediaStore.Audio.Media.COMPOSER} LIKE ?",
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R)  searchableClauseCols += arrayOf(
            MediaStore.Audio.Media.ALBUM_ARTIST,
            MediaStore.Audio.Media.WRITER,
            MediaStore.Audio.Media.GENRE
        )
        return searchableClauseCols
    }

    override suspend fun countSearch(queryWord: String): Int = withContext(Dispatchers.IO) {
        val searchableClauses = getSearchableClauses()
        queryCount(
            MEDIA_URI,
            searchableClauses.joinToString(" OR "),
            Array(searchableClauses.size) { "%$queryWord%" }
        )
    }

    override suspend fun search(queryWord: String, page: Int, pageSize: Int): List<MediaItem> = withContext(Dispatchers.IO) {
        val searchableClauses = getSearchableClauses()
        contentResolver.query(
            MEDIA_URI,
            ENOUGH_MUSIC_COLUMNS,
            searchableClauses.joinToString(" OR "),
            Array(searchableClauses.size) { "%$queryWord%" },
            "LIMIT $pageSize OFFSET ${(page - 1) * pageSize}"
        )?.use { buildMusicItems(it) } ?: listOf()
    }
}