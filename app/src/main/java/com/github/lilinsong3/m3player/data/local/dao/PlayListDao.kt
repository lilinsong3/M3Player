package com.github.lilinsong3.m3player.data.local.dao

import androidx.room.*
import com.github.lilinsong3.m3player.data.local.entity.PlayList
import com.github.lilinsong3.m3player.data.model.SongIdOnly
import com.github.lilinsong3.m3player.data.model.SongModel
import kotlinx.coroutines.flow.Flow

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

    @Query("SELECT count(songId) FROM PlayList")
    suspend fun count(): Int

    @Query("SELECT s.rowid id, s.* FROM Song s INNER JOIN PlayList pl ON s.rowid = pl.songId")
    fun observableQueryAll(): Flow<List<SongModel>>

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

    /**
     * @return 新的id列表
     */
    @Query("DELETE FROM PlayList")
    suspend fun clear()

    @Query("SELECT * FROM PlayList")
    suspend fun queryAllRaw(): List<PlayList>

    @Query("SELECT songId FROM PlayList LIMIT (:page - 1) * :pageSize, :pageSize")
    suspend fun queryPagingSongIds(page: Int, pageSize: Int): List<Long>
}