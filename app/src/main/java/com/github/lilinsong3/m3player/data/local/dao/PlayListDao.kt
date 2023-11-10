package com.github.lilinsong3.m3player.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.github.lilinsong3.m3player.data.local.entity.Song
import com.github.lilinsong3.m3player.data.model.SongModel

@Dao
interface PlayListDao {
    // TODO: check join, test null result
    @Query("SELECT s.rowid as id, s.* FROM Song s INNER JOIN PlayList pl ON s.rowid = pl.songId WHERE s.rowid = :songId")
    suspend fun querySongById(songId: Int): SongModel

    @Insert
    suspend fun testInsert(song: Song)

    @Query("SELECT rowid as id, * FROM Song")
    suspend fun testQueryAll(): List<SongModel>
}