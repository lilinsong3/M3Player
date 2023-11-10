package com.github.lilinsong3.m3player.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PlayList(
    @PrimaryKey val songId: Int,
    @ColumnInfo(defaultValue = "(datetime('now'))") val datetime: String
)
