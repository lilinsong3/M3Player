package com.github.lilinsong3.m3player.data.local.dao

import androidx.room.*
import com.github.lilinsong3.m3player.data.local.entity.PlayList
import com.github.lilinsong3.m3player.data.model.SongIdOnly
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

    @Query("SELECT count(s.rowid) FROM Song s " +
            "INNER JOIN PlayList pl ON s.rowid = pl.songId")
    suspend fun queryNum(): Int

    @Query("SELECT s.rowid id, s.* FROM Song s INNER JOIN PlayList pl ON s.rowid = pl.songId")
    suspend fun queryAll(): List<SongModel>

    @Query("SELECT s.rowid id, s.* FROM Song s " +
            "INNER JOIN PlayList pl ON s.rowid = pl.songId " +
            "WHERE Song MATCH :keyword " +
            "OR s.title LIKE '%' || :keyword || '%' " +
            "OR s.artist LIKE '%' || :keyword || '%' " +
            "LIMIT (:page - 1) * :pageSize, :pageSize")
    suspend fun searchSongs(keyword: String, page: Int, pageSize: Int): List<SongModel>

    /**
     * @return 新的id列表
     */
    @Upsert(entity = PlayList::class)
    suspend fun upsertByIds(ids: List<SongIdOnly>): List<Long>

    @Query("SELECT * FROM PlayList")
    suspend fun queryAllRaw(): List<PlayList>
}