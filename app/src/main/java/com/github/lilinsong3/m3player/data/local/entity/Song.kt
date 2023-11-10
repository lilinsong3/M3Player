package com.github.lilinsong3.m3player.data.local.entity

import androidx.room.*

// TODO: 分词最好支持中英，研究一下languageId
@Fts4(tokenizer = FtsOptions.TOKENIZER_ICU, tokenizerArgs = ["zh_CN"])
@Entity
data class Song(
    @PrimaryKey(autoGenerate = true)
    @ColumnInfo(name = "rowid")
    val id: Int,
    val title: String = "Unknown",
    val artist: String = "Unknown",
    val albumTitle: String = title,
    val albumArtist: String = artist,
    val displayTitle: String = title,
    val subtitle: String? = null,
    val description: String? = null,
    val artworkLocation: String? = null,
    val lyricLocation: String? = null,
    val recordingYear: Int? = null,
    val recordingMonth: Int? = null,
    val recordingDay: Int? = null,
    val releaseYear: Int? = null,
    val releaseMonth: Int? = null,
    val releaseDay: Int? = null,
    val writer: String = "Unknown",
    val composer: String = "Unknown",
    val conductor: String = "Unknown",
    val discNumber: Int? = null,
    val totalDiscCount: Int? = null,
    val genre: String? = null,
    val compilation: String? = null,
    val duration: String = "?",
    @ColumnInfo(defaultValue = "(datetime('now'))")
    val datetime: String
)
