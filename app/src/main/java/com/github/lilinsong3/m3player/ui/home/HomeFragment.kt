package com.github.lilinsong3.m3player.ui.home


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.github.lilinsong3.m3player.Event
import com.github.lilinsong3.m3player.MainViewModel
import com.github.lilinsong3.m3player.R
import com.github.lilinsong3.m3player.databinding.FragmentHomeBinding
import com.google.android.material.tabs.TabLayoutMediator
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class HomeFragment : Fragment() {
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var homeFragmentStateAdapter: HomeFragmentStateAdapter

    private val mainViewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // NavigationUI.setupWithNavController(binding.homeToolbar, Navigation.findNavController(view))
        binding.homeToolbar.setNavigationOnClickListener { _ -> mainViewModel.send(Event.DrawerOpenEvent(true)) }
        homeFragmentStateAdapter = HomeFragmentStateAdapter(this)
        binding.homePager2.adapter = homeFragmentStateAdapter
        TabLayoutMediator(binding.homeTabLayout, binding.homePager2) { tab, position ->
            val context = requireContext()
            tab.text = when (position) {
                0 -> context.getString(R.string.lists)
                2 -> context.getString(R.string.lyric)
                else -> context.getString(R.string.play)
            }
        }.attach()
        binding.homeTabLayout.selectTab(binding.homeTabLayout.getTabAt(1))
    }

    override fun onDestroyView() {
        _binding = null
        super.onDestroyView()
    }
}