package com.github.lilinsong3.m3player.ui.home.lists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import androidx.media3.common.MediaItem
import com.github.lilinsong3.m3player.R
import com.github.lilinsong3.m3player.databinding.ItemSongBinding
import com.github.lilinsong3.m3player.databinding.ItemSongListBinding
import com.google.android.material.color.MaterialColors

class SongListsAdapter : BaseExpandableListAdapter() {
    var lists = listOf<MusicListUiState>()
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun getGroupCount(): Int = lists.size

    override fun getChildrenCount(groupPosition: Int): Int = lists[groupPosition].children.size

    override fun getGroup(groupPosition: Int): MediaItem = lists[groupPosition].list

    override fun getChild(groupPosition: Int, childPosition: Int): MediaItem =
        lists[groupPosition].children[childPosition].item

    // TODO: All MediaItemIds should be numeric
    override fun getGroupId(groupPosition: Int): Long = groupPosition.toLong()

    override fun getChildId(groupPosition: Int, childPosition: Int): Long =
        getChild(groupPosition, childPosition).mediaId.toLong()

    override fun hasStableIds(): Boolean = true

    override fun getGroupView(
        groupPosition: Int, isExpanded: Boolean, convertView: View?, parent: ViewGroup?
    ): View {
        var groupView = convertView
        if (convertView == null) {
            val binding = ItemSongListBinding.inflate(
                LayoutInflater.from(parent?.context),
                parent,
                false
            )
            groupView = binding.root
            groupView.tag = binding
        }
        val metadata = lists[groupPosition].list.mediaMetadata
        val tagBinding = groupView!!.tag as ItemSongListBinding
        tagBinding.root.text = metadata.title ?: metadata.displayTitle
                ?: parent?.context?.getString(R.string.unknown_list) ?: ""
        return groupView
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        var childView = convertView
        if (convertView == null) {
            val binding = ItemSongBinding.inflate(
                LayoutInflater.from(parent?.context),
                parent,
                false
            )
            childView = binding.root
            childView.tag = binding
        }
        val child = lists[groupPosition].children[childPosition]
        val metadata = child.item.mediaMetadata
        val tagBinding = childView!!.tag as ItemSongBinding
        tagBinding.itemImgSong.setImageBitmap(child.thumbnail)
        tagBinding.itemTextSong.text = metadata.title ?: metadata.displayTitle
                ?: parent?.context?.getString(R.string.unknown_list) ?: "unknown"
        tagBinding.itemTextSinger.text =
            metadata.artist ?: parent?.context?.getString(R.string.unknown_song) ?: "unknown"
        tagBinding.itemTextDuration.text = child.duration
        if (child.isPlaying) {
            MaterialColors.getColor(childView, com.google.android.material.R.attr.colorError).also {
                tagBinding.itemTextSong.setTextColor(it)
                tagBinding.itemTextSinger.setTextColor(it)
                tagBinding.itemTextDuration.setTextColor(it)
            }
        }
        return childView
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = false
}