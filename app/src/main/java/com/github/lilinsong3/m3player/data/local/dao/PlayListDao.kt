package com.github.lilinsong3.m3player.data.local.dao

import androidx.room.*
import com.github.lilinsong3.m3player.data.local.entity.PlayList
import com.github.lilinsong3.m3player.data.model.SongIdOnly

@Dao
interface PlayListDao {

    @Query("SELECT count(*) FROM PlayList")
    suspend fun count(): Int

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