package com.github.lilinsong3.m3player.ui.home.lists

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseExpandableListAdapter
import android.widget.TextView
import androidx.media3.common.MediaItem
import androidx.media3.session.MediaBrowser
import com.github.lilinsong3.m3player.databinding.ItemSongListBinding

class SongListsAdapter(private val browser: MediaBrowser) : BaseExpandableListAdapter() {
    var parentList = listOf<MediaItem>()

    override fun getGroupCount(): Int = parentList.size

    override fun getChildrenCount(groupPosition: Int): Int {
        TODO("Not yet implemented")
    }

    override fun getGroup(groupPosition: Int): Any {
        TODO("Not yet implemented")
    }

    override fun getChild(groupPosition: Int, childPosition: Int): Any {
        TODO("Not yet implemented")
    }

    override fun getGroupId(groupPosition: Int): Long {
        TODO("Not yet implemented")
    }

    override fun getChildId(groupPosition: Int, childPosition: Int): Long {
        TODO("Not yet implemented")
    }

    override fun hasStableIds(): Boolean {
        TODO("Not yet implemented")
    }

    override fun getGroupView(
        groupPosition: Int,
        isExpanded: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        val groupView = if (convertView == null) {
            ItemSongListBinding.inflate(LayoutInflater.from(parent?.context), parent, false).root
        } else convertView as TextView
        groupView.text = parentList[groupPosition].mediaMetadata.title ?: ""
        return groupView
    }

    override fun getChildView(
        groupPosition: Int,
        childPosition: Int,
        isLastChild: Boolean,
        convertView: View?,
        parent: ViewGroup?
    ): View {
        TODO("Not yet implemented")
    }

    override fun isChildSelectable(groupPosition: Int, childPosition: Int): Boolean = false
}