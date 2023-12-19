package com.github.lilinsong3.m3player.domain

import com.github.lilinsong3.m3player.data.model.PlayStateModel
import com.github.lilinsong3.m3player.data.repository.PlayListRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetPlayingInfoUseCase @Inject constructor(private val playListRepo: PlayListRepository) {
    operator fun invoke(): Flow<PlayStateModel> = playListRepo.getPlayStateStream()
}