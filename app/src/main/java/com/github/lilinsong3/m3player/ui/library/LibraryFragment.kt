package com.github.lilinsong3.m3player.ui.library

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.lilinsong3.m3player.common.defaultLaunch
import com.github.lilinsong3.m3player.databinding.FragmentLibraryBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
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
        binding.libraryLoading.circularLoading.setVisibilityAfterHide(View.GONE)
        // TODO: use PagingAdapter or MediaBrowser instead
        libraryListAdapter = LibraryListAdapter()
        defaultLaunch {
            viewModel.libraryUiState.collect {
                when (it) {
                    is LibraryState.Success -> {
                        binding.libraryErr.root.visibility = View.GONE
                        binding.libraryLoading.circularLoading.hide()
                        binding.libraryRecyclerSongs.visibility = View.VISIBLE
                        libraryListAdapter.submitList(it.items)
                    }
                    is LibraryState.Error -> {
                        binding.libraryRecyclerSongs.visibility = View.GONE
                        binding.libraryLoading.circularLoading.hide()
                        binding.libraryErr.root.visibility = View.VISIBLE
                    }
                    LibraryState.Loading -> {
                        binding.libraryRecyclerSongs.visibility = View.GONE
                        binding.libraryErr.root.visibility = View.GONE
                        binding.libraryLoading.circularLoading.show()
                    }
                }
            }
        }

    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}