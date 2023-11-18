package com.github.lilinsong3.m3player.data.model

import android.net.Uri

data class SongItemModel(
    val id: Long = 0,
    val name: String = "Unknown",
    val artist: String = "Unknown",
    val albumName: String = "Unknown",
    val albumArtwork: Uri,
    val duration: String = "?"
)
