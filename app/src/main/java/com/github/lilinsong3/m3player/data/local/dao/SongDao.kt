package com.github.lilinsong3.m3player.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.github.lilinsong3.m3player.data.model.SongModel

@Dao
interface SongDao {
    // 需要默认值不能用@Insert，但这种写法太麻烦了！！！
    @Query("INSERT INTO Song " +
            "(path, title, artist, albumTitle, albumArtist, displayTitle, subtitle, description, artworkLocation, lyricLocation, recordingYear, recordingMonth, recordingDay, releaseYear, releaseMonth, releaseDay, writer, composer, conductor, discNumber, totalDiscCount, genre, compilation, duration, datetime) " +
            "VALUES (:path, :title, :artist, :albumTitle, :albumArtist, :displayTitle, :subtitle, :description, :artworkLocation, :lyricLocation, :recordingYear, :recordingMonth, :recordingDay, :releaseYear, :releaseMonth, :releaseDay, :writer, :composer, :conductor, :discNumber, :totalDiscCount, :genre, :compilation, :duration, :datetime)")
    suspend fun insert(
        path: String?,
        title: String,
        artist: String,
        albumTitle: String,
        albumArtist: String,
        displayTitle: String,
        subtitle: String?,
        description: String?,
        artworkLocation: String?,
        lyricLocation: String?,
        recordingYear: Int?,
        recordingMonth: Int?,
        recordingDay: Int?,
        releaseYear: Int?,
        releaseMonth: Int?,
        releaseDay: Int?,
        writer: String,
        composer: String,
        conductor: String,
        discNumber: Int?,
        totalDiscCount: Int?,
        genre: String?,
        compilation: String?,
        duration: String,
        datetime: String
    )

    @Query("SELECT rowid id, * FROM Song")
    suspend fun queryAll(): List<SongModel>
}