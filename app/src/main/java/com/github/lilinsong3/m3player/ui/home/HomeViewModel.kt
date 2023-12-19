package com.github.lilinsong3.m3player.ui.home

import androidx.annotation.ColorInt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.session.MediaBrowser
import com.github.lilinsong3.m3player.domain.ExtractColorFromArtworkUseCase
import com.github.lilinsong3.m3player.domain.GetPlayingInfoUseCase
import com.google.common.util.concurrent.ListenableFuture
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.guava.await
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val browseFuture: ListenableFuture<MediaBrowser>,
    private val getPlayingInfoUseCase: GetPlayingInfoUseCase,
    private val extractColorFromArtworkUseCase: ExtractColorFromArtworkUseCase
) : ViewModel() {

    private lateinit var browser: MediaBrowser

    init {
        viewModelScope.launch(Dispatchers.Main) {
            // browseFuture will be cancelled with coroutine together
            browser = browseFuture.await()
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState = getPlayingInfoUseCase().mapLatest {
        HomeUiState.Success(
            extractColorFromArtworkUseCase(
                // TODO: 状态码判断、异常处理，将获取单个item封装进单个UseCase，最后整合到一个获取Item封面的UseCase
                browser.getItem(it.songId.toString()).await().value?.mediaMetadata?.artworkUri
            )
        )
    }
}

sealed interface HomeUiState {
    data class Success(@ColorInt val backgroundColor: Int)
}