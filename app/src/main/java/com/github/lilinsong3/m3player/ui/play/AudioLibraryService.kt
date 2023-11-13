package com.github.lilinsong3.m3player.ui.play

import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.MediaItemsWithStartPosition
import com.github.lilinsong3.m3player.data.repository.PlayListRepository
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.guava.future

class AudioLibraryService(
    val playListRepo: PlayListRepository
) : MediaLibraryService(), CoroutineScope by MainScope() {
    private var audioLibrarySession: MediaLibrarySession? = null

    companion object {
        const val PLAY_LIST_ROOT_ID = "PlayList"
        private const val TAG = "AudioLibraryService"
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
        ): ListenableFuture<LibraryResult<MediaItem>> = Futures.immediateFuture(
            LibraryResult.ofItem(playListLibraryRoot, params)
        )

        override fun onGetItem(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> = future(Dispatchers.IO) {
            try {
                val item = playListRepo.getMediaItem(mediaId.toInt())
                if (item == null) LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
                else LibraryResult.ofItem(item, null)
            } catch (e: NumberFormatException) {
                LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
            }

        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> =
            if (parentId == PLAY_LIST_ROOT_ID) future(Dispatchers.IO) {
                LibraryResult.ofItemList(playListRepo.getMediaItems(page, pageSize), params)
            }
            else Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))

        override fun onSubscribe(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> {
            if (parentId == PLAY_LIST_ROOT_ID) {
                launch(Dispatchers.IO) {
                    session.notifyChildrenChanged(
                        browser,
                        parentId,
                        playListRepo.countSongs(),
                        params
                    )
                }
                return Futures.immediateFuture(LibraryResult.ofVoid(params))
            }
            return Futures.immediateFuture(LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE))
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
            launch(Dispatchers.IO) {
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
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> = future(Dispatchers.IO) {
            LibraryResult.ofItemList(playListRepo.searchSongs(query, page, pageSize), params)
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> = future(Dispatchers.IO) {
            mediaItems.filter {
                playListRepo.add(mediaItems.map { item ->
                    item.mediaId.toInt()
                }).contains(it.mediaId.toInt())
            }.toMutableList()
        }

        override fun onPlaybackResumption(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo
        ): ListenableFuture<MediaItemsWithStartPosition> = future(Dispatchers.IO) {
            Log.i(TAG, "in onPlaybackResumption() of session callback, start to resume playback")
            // TODO: test this completely to make sure that this works
            playListRepo.getPlayingInfoStream().first().run {
                Log.i(TAG, "in onPlaybackResumption() of session callback, start resuming playback")
                mediaSession.player.also {
                    it.repeatMode = repeatMode
                    it.shuffleModeEnabled = shuffleMode
                }
                val items = playListRepo.getAllItems()
                val startItem = items.find { item -> item.mediaId == songId.toString() }
                MediaItemsWithStartPosition(
                    items,
                    if (startItem == null) C.INDEX_UNSET else items.indexOf(startItem) ,
                    currentPosition
                )
            }
        }
    }
}