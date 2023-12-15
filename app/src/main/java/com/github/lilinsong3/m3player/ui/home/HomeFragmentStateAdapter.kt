package com.github.lilinsong3.m3player.ui.home

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.github.lilinsong3.m3player.ui.home.lists.ListsFragment
import com.github.lilinsong3.m3player.ui.home.lyric.LyricFragment
import com.github.lilinsong3.m3player.ui.home.song.PlayFragment

class HomeFragmentStateAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
    override fun getItemCount(): Int = 3

    override fun createFragment(position: Int): Fragment = when (position) {
        0 -> ListsFragment()
        // 1 -> PlayFragment()
        2 -> LyricFragment()
        else -> PlayFragment()
    }
}