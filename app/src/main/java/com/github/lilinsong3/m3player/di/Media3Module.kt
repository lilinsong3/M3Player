package com.github.lilinsong3.m3player.di

import android.content.ComponentName
import android.content.Context
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.github.lilinsong3.m3player.ui.home.song.AudioLibraryService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object Media3Module {
    @Singleton
    @Provides
    fun provideMediaBrowserFuture(@ApplicationContext context: Context) = MediaBrowser.Builder(
        context,
        SessionToken(context, ComponentName(context, AudioLibraryService::class.java))
    ).buildAsync()
}