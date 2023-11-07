package com.github.lilinsong3.m3player.ui.play

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.ListenableFuture

class AudioLibraryService: MediaLibraryService() {
    private var audioLibrarySession: MediaLibrarySession? = null
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? =
        audioLibrarySession

    override fun onCreate() {
        super.onCreate()
        audioLibrarySession = MediaLibrarySession.Builder(
            this,
            ExoPlayer.Builder(this).build(),
            AudioLibrarySessionCallback()
        ).build()
    }

    override fun onDestroy() {
        audioLibrarySession?.run {
            player.release()
            release()
            // listener for service should be cleared if setListener() for service is called
            // clearListener()
            audioLibrarySession = null
        }
        super.onDestroy()
    }

    // TODO: Impl this
    class AudioLibrarySessionCallback: MediaLibrarySession.Callback {
        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            return super.onGetLibraryRoot(session, browser, params)
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            return super.onGetItem(session, browser, mediaId)
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            return super.onGetChildren(session, browser, parentId, page, pageSize, params)
        }

        override fun onSubscribe(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> {
            return super.onSubscribe(session, browser, parentId, params)
        }

        override fun onUnsubscribe(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String
        ): ListenableFuture<LibraryResult<Void>> {
            return super.onUnsubscribe(session, browser, parentId)
        }

        override fun onSearch(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> {
            return super.onSearch(session, browser, query, params)
        }

        override fun onGetSearchResult(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            return super.onGetSearchResult(session, browser, query, page, pageSize, params)
        }

        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            return super.onPlaybackResumption(mediaSession, controller)
        }
    }
}