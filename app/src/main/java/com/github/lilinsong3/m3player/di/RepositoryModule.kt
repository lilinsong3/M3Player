package com.github.lilinsong3.m3player.di

import com.github.lilinsong3.m3player.data.repository.DefaultPlayListRepository
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
    abstract fun bindPlayListRepository(defaultPlayListRepository: DefaultPlayListRepository): PlayListRepository
}