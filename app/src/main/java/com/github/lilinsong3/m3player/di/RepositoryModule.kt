package com.github.lilinsong3.m3player.di

import com.github.lilinsong3.m3player.data.repository.DefaultMusicRepository
import com.github.lilinsong3.m3player.data.repository.DefaultPlayListRepository
import com.github.lilinsong3.m3player.data.repository.MusicRepository
import com.github.lilinsong3.m3player.data.repository.PlayListRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Singleton
    @Binds
    abstract fun bindPlayListRepository(defaultRepository: DefaultPlayListRepository): PlayListRepository

    @Singleton
    @Binds
    abstract fun bindMusicRepository(defaultRepository: DefaultMusicRepository): MusicRepository
}