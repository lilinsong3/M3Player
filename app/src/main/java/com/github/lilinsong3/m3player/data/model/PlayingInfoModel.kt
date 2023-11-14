package com.github.lilinsong3.m3player.data.model

data class PlayingInfoModel(
    val songId: Int,
    val repeatMode: Int,
    val shuffleMode: Boolean,
    val currentPosition: Long
)
