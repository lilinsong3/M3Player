package com.github.lilinsong3.m3player.ui.play

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import com.github.lilinsong3.m3player.data.repository.PlayListRepository
import com.github.lilinsong3.m3player.data.repository.SongRepository
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.guava.future
import kotlinx.coroutines.launch

class AudioLibraryService(
    val playListRepo: PlayListRepository,
    val songRepo: SongRepository
) : MediaLibraryService(), CoroutineScope by MainScope() {
    private var audioLibrarySession: MediaLibrarySession? = null

    companion object {
        const val PLAY_LIST_ROOT_ID = "PlayList"
    }

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
        // cancel MainScope
        cancel()
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
     inner class AudioLibrarySessionCallback : MediaLibrarySession.Callback {
        private val playListLibraryRoot = MediaItem.Builder()
            .setMediaId(PLAY_LIST_ROOT_ID)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setMediaType(MediaMetadata.MEDIA_TYPE_PLAYLIST)
                    .setIsBrowsable(false)
                    .setIsPlayable(false)
                    .build()
            )
            .build()

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> {
            return Futures.immediateFuture(LibraryResult.ofItem(playListLibraryRoot, params))
        }

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> {
            super.onGetItem(session, browser, mediaId)
            return future {
                val item = playListRepo.getMediaItem(mediaId)
                if (item == null) LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
                else LibraryResult.ofItem(item, null)
            }
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            super.onGetChildren(session, browser, parentId, page, pageSize, params)
            return if (parentId == PLAY_LIST_ROOT_ID) future {
                LibraryResult.ofItemList(playListRepo.getMediaItems(page, pageSize), params)
            }
            else Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
        }

        override fun onSearch(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> {
            launch {
                session.notifySearchResultChanged(
                    browser,
                    query,
                    playListRepo.countMatchedSongs(query),
                    params
                )
            }
            return Futures.immediateFuture(LibraryResult.ofVoid(params))
        }

        override fun onGetSearchResult(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
            return future {
                LibraryResult.ofItemList(playListRepo.searchSongs(query, page, pageSize), params)
            }
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            return future {
                // TODO: optimize this
                mediaItems.filter { it ->
                    playListRepo.add(mediaItems.map { it.mediaId.toInt() }).contains(it.mediaId.toInt())
                }.toMutableList()
            }
        }

        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            return super.onPlaybackResumption(mediaSession, controller)
        }
    }
}