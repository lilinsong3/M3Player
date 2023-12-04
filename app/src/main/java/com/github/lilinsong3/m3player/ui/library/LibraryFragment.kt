package com.github.lilinsong3.m3player.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.lilinsong3.m3player.common.defaultLaunch
import com.github.lilinsong3.m3player.databinding.FragmentLibraryBinding

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!

    private lateinit var libraryListAdapter: LibraryListAdapter

    private val viewModel: LibraryViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        libraryListAdapter = LibraryListAdapter()
        defaultLaunch {
            viewModel.libraryUiState.collect {
                when (it) {
                    is LibraryState.Success -> libraryListAdapter.submitList(it.items)
                    is LibraryState.Error -> {}
                    LibraryState.Loading -> {}
                }
            }
        }

    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}