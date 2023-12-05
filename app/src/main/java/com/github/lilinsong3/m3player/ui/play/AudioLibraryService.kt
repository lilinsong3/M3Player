package com.github.lilinsong3.m3player.ui.play

import android.content.Intent
import android.util.Log
import androidx.annotation.OptIn
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.util.EventLogger
import androidx.media3.session.LibraryResult
import androidx.media3.session.MediaLibraryService
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSession.MediaItemsWithStartPosition
import com.github.lilinsong3.m3player.data.model.PlayStateModel
import com.github.lilinsong3.m3player.data.repository.DefaultMusicRepository
import com.github.lilinsong3.m3player.data.repository.MusicRepository
import com.github.lilinsong3.m3player.data.repository.PlayListRepository
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.guava.future
import java.io.IOException
import javax.inject.Inject

@AndroidEntryPoint
class AudioLibraryService : MediaLibraryService(), CoroutineScope by MainScope() {

    @Inject
    lateinit var playListRepo: PlayListRepository
    @Inject
    lateinit var musicRepo: MusicRepository
    private var audioLibrarySession: MediaLibrarySession? = null
    companion object {
        private const val TAG = "AudioLibraryService"
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaLibrarySession? =
        audioLibrarySession

    override fun onCreate() {
        super.onCreate()
        val exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer.addAnalyticsListener(EventLogger())
        exoPlayer.addListener(object : Player.Listener {
            override fun onEvents(player: Player, events: Player.Events) {
                super.onEvents(player, events)
                if (events.contains(Player.EVENT_IS_PLAYING_CHANGED)) {
                    // TODO: update PlayHistory
                    savePlayState(player)
                }
            }
        })
        audioLibrarySession = MediaLibrarySession.Builder(
            this, exoPlayer, AudioLibrarySessionCallback()
        ).build()
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        audioLibrarySession?.player!!.run {
            if (playWhenReady) {
                pause()
            }
        }
        stopSelf()
    }

    override fun onDestroy() {
        // cancel MainScope
        val player = audioLibrarySession?.player!!
        savePlayState(player)
        // cancel CoroutineScope
        cancel()
        player.release()
        audioLibrarySession?.release()
        // listener of service should be cleared if setListener() for service is called
        // clearListener()
        audioLibrarySession = null
        super.onDestroy()
    }

    private fun savePlayState(player: Player) {
        player.currentMediaItem?.run {
            launch {
                playListRepo.savePlayState(
                    PlayStateModel(
                        mediaId.toLong(),
                        player.repeatMode,
                        player.shuffleModeEnabled,
                        player.currentPosition
                    )
                )
            }
        }
    }

    private inner class AudioLibrarySessionCallback : MediaLibrarySession.Callback {

        override fun onGetLibraryRoot(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<MediaItem>> = Futures.immediateFuture(
            LibraryResult.ofItem(
                DefaultMusicRepository.musicDirMap[DefaultMusicRepository.ROOT] ?: MediaItem.EMPTY,
                params
            )
        )

        override fun onGetItem(
            session: MediaLibrarySession, browser: MediaSession.ControllerInfo, mediaId: String
        ): ListenableFuture<LibraryResult<MediaItem>> = future {
            try {
                // super.onGetItem(session, browser, mediaId)
                val item = musicRepo.getMediaItemByMediaId(mediaId)
                if (item == null) LibraryResult.ofError(LibraryResult.RESULT_ERROR_BAD_VALUE)
                else LibraryResult.ofItem(item, null)
            } catch (e: IOException) {
                LibraryResult.ofError(LibraryResult.RESULT_ERROR_IO)
            }
        }

        override fun onGetChildren(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            page: Int,
            pageSize: Int,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> = future {
            LibraryResult.ofItemList(
                musicRepo.getPagingMediaChildrenByParentId(
                    parentId, page, pageSize
                ), params
            )
        }

        override fun onSubscribe(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            parentId: String,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> {
            launch {
                session.notifyChildrenChanged(
                    browser,
                    parentId,
                    musicRepo.countChildrenByParentId(parentId),
                    params
                )
            }
            return Futures.immediateFuture(LibraryResult.ofVoid(params))
        }

        override fun onSearch(
            session: MediaLibrarySession,
            browser: MediaSession.ControllerInfo,
            query: String,
            params: LibraryParams?
        ): ListenableFuture<LibraryResult<Void>> {
            launch {
                session.notifySearchResultChanged(
                    browser, query, musicRepo.countSearch(query), params
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
        ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> = future {
            LibraryResult.ofItemList(musicRepo.search(query, page, pageSize), params)
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> = future {
            // super.onAddMediaItems(mediaSession, controller, mediaItems)
            try {
                playListRepo.add(mediaItems.map { item ->
                    item.mediaId.substringAfter("/", "-1").toLong()
                }).map { id ->
                    mediaItems.first { item -> item.mediaId.endsWith(id.toString()) }
                }.toMutableList()
            } catch (e: Exception) {
                /* NumberFormatException, IOException, NoSuchElementException */
                mutableListOf()
            }

        }

        @OptIn(UnstableApi::class)
        override fun onSetMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
            startIndex: Int,
            startPositionMs: Long
        ): ListenableFuture<MediaItemsWithStartPosition> = future {
            // TODO: handle IOE
            playListRepo.set(mediaItems.map { item ->
                item.mediaId.substringAfter("/", "-1").toLong()
            })
            MediaItemsWithStartPosition(mediaItems, startIndex, startPositionMs)
        }

        @OptIn(UnstableApi::class)
        override fun onPlaybackResumption(
            mediaSession: MediaSession, controller: MediaSession.ControllerInfo
        ): ListenableFuture<MediaItemsWithStartPosition> = future {
            Log.i(TAG, "in onPlaybackResumption() of session callback, start to resume playback")
            // TODO: test this completely to make sure that this works
            // super.onPlaybackResumption(mediaSession, controller)
            playListRepo.getPlayStateStream().first().run {
                Log.i(TAG, "in onPlaybackResumption() of session callback, start resuming playback")
                mediaSession.player.also {
                    it.repeatMode = repeatMode
                    it.shuffleModeEnabled = shuffleMode
                }
                val items = playListRepo.getAllItemsStream().first()
                val startItem = items.find { item -> item.mediaId == songId.toString() }
                MediaItemsWithStartPosition(
                    items,
                    if (startItem == null) C.INDEX_UNSET else items.indexOf(startItem),
                    currentPosition
                )
            }
        }
    }
}