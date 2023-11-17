package com.github.lilinsong3.m3player.data.model

data class AudioItemModel(
    val id: Long = 0,
    val name: String = "Unknown",
    val artist: String = "Unknown",
    val albumName: String = "Unknown",
    val albumId: Long,
    val duration: String = "?"
)
