package com.github.lilinsong3.m3player.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.github.lilinsong3.m3player.data.model.SongModel

@Dao
interface PlayListDao {
    @Query("SELECT s.rowid id, s.* FROM Song s INNER JOIN PlayList pl ON s.rowid = pl.songId WHERE s.rowid = :songId")
    suspend fun querySongById(songId: Int): SongModel?

    @Query("SELECT s.rowid id, s.* FROM Song s INNER JOIN PlayList pl ON s.rowid = pl.songId LIMIT (:page - 1) * :pageSize, :pageSize")
    suspend fun querySongs(page: Int, pageSize: Int): List<SongModel>
}