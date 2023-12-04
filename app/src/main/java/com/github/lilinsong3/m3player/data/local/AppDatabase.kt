package com.github.lilinsong3.m3player.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.github.lilinsong3.m3player.data.local.dao.PlayListDao
import com.github.lilinsong3.m3player.data.local.entity.ListSong
import com.github.lilinsong3.m3player.data.local.entity.PlayHistory
import com.github.lilinsong3.m3player.data.local.entity.PlayList
import com.github.lilinsong3.m3player.data.local.entity.SongList

@Database(
    entities = [SongList::class, ListSong::class, PlayList::class, PlayHistory::class],
    version = 1,
    exportSchema = false /* TODO: to delete */
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun playListDao(): PlayListDao
//    abstract fun songDao(): SongDao
}