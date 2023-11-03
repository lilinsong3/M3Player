package com.github.lilinsong3.m3player.ui.play

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession

class AudioLibraryService: MediaLibraryService() {
    var audioLibrarySession: MediaLibrarySession? = null
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? =
        audioLibrarySession

    override fun onCreate() {
        super.onCreate()
        audioLibrarySession = MediaLibrarySession.Builder(
            this,
            ExoPlayer.Builder(this).build(),
            // TODO: add a impl
            object : MediaLibrarySession.Callback{}
        ).build()
    }

    override fun onDestroy() {
        audioLibrarySession?.run {
            player.release()
            release()
            audioLibrarySession = null
        }
        super.onDestroy()
    }
}