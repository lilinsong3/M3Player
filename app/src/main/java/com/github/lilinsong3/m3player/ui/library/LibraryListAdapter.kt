package com.github.lilinsong3.m3player.ui.library

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.github.lilinsong3.m3player.data.model.SongItemModel
import com.github.lilinsong3.m3player.databinding.ItemSongBinding

class LibraryListAdapter :
    ListAdapter<SongItemModel, LibraryListAdapter.SongItemViewHolder>(object :
        DiffUtil.ItemCallback<SongItemModel>() {
        override fun areItemsTheSame(oldItem: SongItemModel, newItem: SongItemModel): Boolean =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: SongItemModel, newItem: SongItemModel): Boolean =
            oldItem == newItem
    }) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongItemViewHolder {
        TODO("Not yet implemented")
    }

    override fun onBindViewHolder(holder: SongItemViewHolder, position: Int) {
        TODO("Not yet implemented")
    }

    class SongItemViewHolder(private val binding: ItemSongBinding) : ViewHolder(binding.root) {

    }
}