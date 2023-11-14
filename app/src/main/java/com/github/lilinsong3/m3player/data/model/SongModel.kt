package com.github.lilinsong3.m3player.data.model

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

data class SongModel(
    val id: Int,
    val path: String? = null,
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
    val discNumber: Int? = null,
    val totalDiscCount: Int? = null,
    val genre: String? = null,
    val compilation: String? = null,
    val duration: String = "?",
    val datetime: String
) {
    companion object {
        fun toMediaItem(model: SongModel?): MediaItem? = model?.run {
            MediaItem.Builder()
                .setMediaId(id.toString())
                .setUri(path)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(title)
                        .setArtist(artist)
                        .setAlbumTitle(albumTitle)
                        .setAlbumArtist(albumArtist)
                        .setDisplayTitle(displayTitle)
                        .setSubtitle(subtitle)
                        .setDescription(description)
                        .setArtworkUri(Uri.parse(artworkLocation))
                        .setRecordingYear(recordingYear)
                        .setRecordingMonth(recordingMonth)
                        .setRecordingDay(recordingDay)
                        .setReleaseYear(releaseYear)
                        .setReleaseMonth(releaseMonth)
                        .setReleaseDay(releaseDay)
                        .setWriter(writer)
                        .setComposer(composer)
                        .setConductor(conductor)
                        .setDiscNumber(discNumber)
                        .setTotalDiscCount(totalDiscCount)
                        .setGenre(genre)
                        .setCompilation(compilation)
                        .build()
                )
                .build()
        }
    }
}
