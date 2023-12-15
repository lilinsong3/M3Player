package com.github.lilinsong3.m3player.ui.home.lists


import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.AbsListView.OnScrollListener
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
        binding.listsLoading.circularLoading.setVisibilityAfterHide(View.GONE)
        listsAdapter = SongListsAdapter()
        binding.listsExpLv.setAdapter(listsAdapter)
        binding.listsExpLv.setOnGroupExpandListener(viewModel::onGroupExpand)
        binding.listsExpLv.setOnScrollListener(object : OnScrollListener {
            override fun onScrollStateChanged(view: AbsListView?, scrollState: Int) {}

            override fun onScroll(
                view: AbsListView?,
                firstVisibleItem: Int,
                visibleItemCount: Int,
                totalItemCount: Int
            ) {
                Log.d("RunDebug", "onScroll: firstVisibleItem=$firstVisibleItem, visibleItemCount=$visibleItemCount, totalItemCount=$totalItemCount")
            }

        })

        defaultLaunch {
            viewModel.uiState.collect {
                when (it.loadState) {
                    is LoadState.Success -> {
                        binding.listsLoading.circularLoading.hide()
                        binding.listsErr.root.visibility = View.GONE
                        binding.listsExpLv.visibility = View.VISIBLE
                        listsAdapter.lists = it.lists
                    }
                    LoadState.Loading -> {
                        binding.listsErr.root.visibility = View.GONE
                        binding.listsExpLv.visibility = View.GONE
                        binding.listsLoading.circularLoading.show()
                    }
                    is LoadState.Error -> {
                        binding.listsExpLv.visibility = View.GONE
                        binding.listsLoading.circularLoading.hide()
                        binding.listsErr.root.visibility = View.VISIBLE
                    }
                }
            }
        }

        viewModel.loadMoreMusicLists()
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}