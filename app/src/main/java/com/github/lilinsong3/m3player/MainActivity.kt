package com.github.lilinsong3.m3player

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.github.lilinsong3.m3player.common.defaultLaunch
import com.github.lilinsong3.m3player.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: MainViewModel by viewModels()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        (supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment).run {
            NavigationUI.setupWithNavController(
                binding.maTopAppbar,
                navController,
                AppBarConfiguration(binding.maDrawerNav.menu, binding.maDrawerLayout)
            )
            binding.maDrawerNav.setupWithNavController(navController)
            navController.addOnDestinationChangedListener(this@MainActivity)
        }

        defaultLaunch {
            viewModel.event.collect {
                when (it) {
                    // 工具栏显示控制
                    is Event.AppBarVisibilityEvent -> {
                        binding.maTopAppbar.visibility = if (it.shown) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    }
                    is Event.DrawerOpenEvent -> {
                        if (it.open) {
                            binding.maDrawerLayout.open()
                        } else {
                            binding.maDrawerLayout.close()
                        }
                    }
                }
            }
        }
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        val useTopAppbarArg = arguments?.getBoolean("useTopAppbar", true) ?: true
        binding.maTopAppbar.visibility = if (useTopAppbarArg)  View.VISIBLE else View.GONE

        val useTopPlaybackCtrlArg = arguments?.getBoolean("useTopPlaybackCtrl", true) ?: true
        binding.mainPlaybackControlLayout.visibility = if (useTopPlaybackCtrlArg)  View.VISIBLE else View.GONE
    }
}