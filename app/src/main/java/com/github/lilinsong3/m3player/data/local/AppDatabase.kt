package com.github.lilinsong3.m3player.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.lilinsong3.m3player.data.local.dao.PlayListDao
import com.github.lilinsong3.m3player.data.local.dao.SongDao
import com.github.lilinsong3.m3player.data.local.entity.*

@Database(
    entities = [Song::class, SongList::class, ListSong::class, PlayList::class, PlayHistory::class],
    version = 1
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playListDao(): PlayListDao
    abstract fun songDao(): SongDao
}