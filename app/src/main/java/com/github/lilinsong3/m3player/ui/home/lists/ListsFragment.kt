package com.github.lilinsong3.m3player.ui.home.lists


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.github.lilinsong3.m3player.common.defaultLaunch
import com.github.lilinsong3.m3player.databinding.FragmentListsBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class ListsFragment : Fragment() {
    private var _binding: FragmentListsBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ListsViewModel by viewModels()

    private lateinit var listsAdapter: SongListsAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentListsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        listsAdapter = SongListsAdapter()
        defaultLaunch {
            viewModel.uiState.collect {
                when (it.loadState) {
                    is LoadState.Success -> {
                        listsAdapter.lists = it.lists
                    }
                    LoadState.Loading -> {}
                    is LoadState.Error -> {}
                }
            }
        }
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}