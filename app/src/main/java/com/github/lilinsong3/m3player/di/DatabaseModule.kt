package com.github.lilinsong3.m3player.di

import android.content.Context
import androidx.room.Room
import com.github.lilinsong3.m3player.data.local.AppDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Singleton
    @Provides
    fun provideAppDatabase(@ApplicationContext context: Context) = Room.databaseBuilder(
        context,
        AppDatabase::class.java,
        "m3_player.db"
    ).build()

    @Provides
    fun providePlayListDao(appDatabase: AppDatabase) = appDatabase.playListDao()

    @Provides
    fun provideSongDao(appDatabase: AppDatabase) = appDatabase.songDao()
}