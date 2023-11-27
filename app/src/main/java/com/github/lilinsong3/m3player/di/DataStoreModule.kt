package com.github.lilinsong3.m3player.di

import android.content.Context
import com.github.lilinsong3.m3player.data.repository.playStateDataStore
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataStoreModule {
    @Singleton
    @Provides
    fun providePlayStateDataStore(@ApplicationContext context: Context) = context.playStateDataStore
}