package com.github.lilinsong3.m3player.ui.home.lyric


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.github.lilinsong3.m3player.databinding.FragmentLyricBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LyricFragment : Fragment() {
    private var _binding: FragmentLyricBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLyricBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}