package com.github.lilinsong3.m3player.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 歌曲列表
 */
@Entity
data class SongList(@PrimaryKey(autoGenerate = true) val id: Long, val name: String)
