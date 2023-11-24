package com.github.lilinsong3.m3player.common

import androidx.recyclerview.widget.DiffUtil.ItemCallback

class DefaultDiffItemCallback<M : Differentiable> : ItemCallback<M>() {
    override fun areItemsTheSame(oldItem: M, newItem: M): Boolean =
        oldItem.getKey() == newItem.getKey()

    override fun areContentsTheSame(oldItem: M, newItem: M): Boolean = oldItem == newItem
}

interface Differentiable {
    fun getKey() : String
    override fun equals(other: Any?): Boolean
}
