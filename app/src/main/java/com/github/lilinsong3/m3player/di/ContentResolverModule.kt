package com.github.lilinsong3.m3player.di

import android.content.ContentResolver
import android.content.Context
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object ContentResolverModule {
    @Singleton
    fun provideContentResolver(@ApplicationContext context: Context) : ContentResolver = context.contentResolver
}