package com.github.lilinsong3.m3player.ui.play

import androidx.media3.common.MediaItem
import com.google.common.collect.ImmutableList

object MediaTree {
    class MediaNode(val item: MediaItem) {
        var parent: MediaItem = MediaItem.EMPTY
        private val _children: MutableList<MediaItem> = ArrayList()
        val children: ImmutableList<MediaItem> get() = ImmutableList.copyOf(_children)
    }
}