package com.github.lilinsong3.m3player.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.github.lilinsong3.m3player.data.local.entity.Song
import com.github.lilinsong3.m3player.data.model.PartialSongModel
import com.github.lilinsong3.m3player.data.model.SongModel

@Dao
interface SongDao {
//    @Query("""INSERT INTO Song
//(
//title,
//artist,
//albumTitle,
//albumArtist,
//displayTitle,
//subtitle,
//description,
//artworkLocation,
//lyricLocation,
//recordingYear,
//recordingMonth,
//recordingDay,
//releaseYear,
//releaseMonth,
//releaseDay,
//writer,
//composer,
//conductor,
//discNumber,
//totalDiscCount,
//genre,
//compilation,
//duration
//)
//VALUES
//(
//:song.title
//)""")
//    suspend fun insert()
// TODO: to test this
    @Insert(entity = Song::class)
    suspend fun insert(songData: PartialSongModel)

    @Query("SELECT rowid id, * FROM Song")
    suspend fun queryAll(): List<SongModel>
}