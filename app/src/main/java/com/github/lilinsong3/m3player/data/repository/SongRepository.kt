package com.github.lilinsong3.m3player.data.repository

interface SongRepository {

    suspend fun getAllLocalSongs()

}