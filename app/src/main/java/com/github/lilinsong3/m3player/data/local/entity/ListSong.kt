package com.github.lilinsong3.m3player.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity

/**
 * 歌曲列表和歌曲的映射表
 */
@Entity(primaryKeys = ["listId", "songId"])
data class ListSong(
    val listId: Int,
    val songId: Int,
    @ColumnInfo(defaultValue = "(datetime('now'))") val datetime: String
)
