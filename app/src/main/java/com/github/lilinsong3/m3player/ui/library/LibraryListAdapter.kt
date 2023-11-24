package com.github.lilinsong3.m3player.ui.library

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.github.lilinsong3.m3player.common.DefaultDiffItemCallback
import com.github.lilinsong3.m3player.databinding.ItemSongBinding

class LibraryListAdapter :
    ListAdapter<LibraryItemState, LibraryListAdapter.SongItemViewHolder>(DefaultDiffItemCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongItemViewHolder {
        return SongItemViewHolder(
            ItemSongBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: SongItemViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SongItemViewHolder(private val binding: ItemSongBinding) : ViewHolder(binding.root) {
        fun bind(itemState: LibraryItemState) {
            binding.apply {
                itemTextSong.text = itemState.item.name
                itemTextSinger.text = itemState.item.artist
                itemTextDuration.text = itemState.item.duration
            }
            itemView.setOnClickListener{ itemState.onClick() }
        }
    }
}