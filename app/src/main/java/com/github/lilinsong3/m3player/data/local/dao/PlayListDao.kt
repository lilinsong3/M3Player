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

    @Query("SELECT count(s.rowid) FROM Song s " +
            "INNER JOIN PlayList pl ON s.rowid = pl.songId " +
            "WHERE Song MATCH :keyword " +
            "OR s.title LIKE '%' || :keyword || '%' " +
            "OR s.artist LIKE '%' || :keyword || '%'")
    suspend fun queryMatchedNum(keyword: String): Int

    @Query("SELECT s.rowid id, s.* FROM Song s " +
            "INNER JOIN PlayList pl ON s.rowid = pl.songId " +
            "WHERE Song MATCH :keyword " +
            "OR s.title LIKE '%' || :keyword || '%' " +
            "OR s.artist LIKE '%' || :keyword || '%' " +
            "LIMIT (:page - 1) * :pageSize, :pageSize")
    suspend fun searchSongs(keyword: String, page: Int, pageSize: Int): List<SongModel>

    /**
     * @return 新插入的id列表
     */
    @Query("INSERT INTO PlayList (songId) SELECT rowid FROM Song WHERE rowid IN (:ids)")
    suspend fun insertByIds(ids: List<Int>): List<Int>
}