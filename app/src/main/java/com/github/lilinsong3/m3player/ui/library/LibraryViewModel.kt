package com.github.lilinsong3.m3player.ui.library

import androidx.lifecycle.ViewModel
import com.github.lilinsong3.m3player.data.model.SongItemModel
import com.github.lilinsong3.m3player.data.repository.SongRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class LibraryViewModel @Inject constructor(private val songRepository: SongRepository) :
    ViewModel() {
    // TODO: use MVI
    private val _mutableSongsFlow: MutableStateFlow<List<SongItemModel>> = MutableStateFlow(listOf())
    val songsFlow: StateFlow<List<SongItemModel>> = _mutableSongsFlow.asStateFlow()
}