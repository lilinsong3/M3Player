package com.github.lilinsong3.m3player.data.repository

import com.github.lilinsong3.m3player.data.model.SongItemModel

interface SongRepository {

    suspend fun getAllLocalSongs(page: Int, pageSize: Int = 30) : List<SongItemModel>

}