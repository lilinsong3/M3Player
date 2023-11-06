package com.github.lilinsong3.m3player.ui.play

import androidx.media3.common.MediaItem
import com.google.common.collect.ImmutableList

object MediaTree {

    class MediaNode(val item: MediaItem) {
        var parent: MediaNode? = null
        private val _children: MutableList<MediaNode> = ArrayList()
        val children: ImmutableList<MediaNode> = ImmutableList.copyOf(_children)

        fun addChild(child: MediaNode) {
            child.parent = this
            _children.add(child)
        }

        fun addChild(childItem: MediaItem) {
            addChild(MediaNode(childItem))
        }
    }
}