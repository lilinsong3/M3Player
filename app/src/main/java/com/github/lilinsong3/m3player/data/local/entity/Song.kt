package com.github.lilinsong3.m3player.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.PrimaryKey

data class Song(
    @PrimaryKey(autoGenerate = true) val id: Long,
    val title: String = "Unknown",
    val artist: String = "Unknown",
    val albumTitle: String = title,
    val albumArtist: String = artist,
    val displayTitle: String = title,
    val subtitle: String?,
    val description: String?,
    val artworkLocation: String?,
    val lyricLocation: String?,
    val recordingYear: Int?,
    val recordingMonth: Int?,
    val recordingDay: Int?,
    val releaseYear: Int?,
    val releaseMonth: Int?,
    val releaseDay: Int?,
    val writer: String = "Unknown",
    val composer: String = "Unknown",
    val conductor: String = "Unknown",
    val discNumber: Int?,
    val totalDiscCount: Int?,
    val genre: String?,
    val compilation: String?,
    val duration: String = "?",
    @ColumnInfo(defaultValue = "(datetime('now'))") val datetime: String
)
