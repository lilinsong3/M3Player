package com.github.lilinsong3.m3player.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 歌曲列表
 */
@Entity
data class SongList(
    // 兼顾自动生成以及插入时需要实例化对象的需求，将id 默认为0，Insert时会看做未设置
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val type: SongListType = SongListType.UNSET,
    @ColumnInfo(defaultValue = "(datetime('now'))")
    val datetime: String
)

enum class SongListType {
    LOCAL_FOLDER,
    REMOTE_LIST,
    USER_CREATED,
    UNSET
}
