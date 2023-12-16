package com.github.lilinsong3.m3player

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupWithNavController
import com.github.lilinsong3.m3player.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity(), NavController.OnDestinationChangedListener {
    private lateinit var binding: ActivityMainBinding
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
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        val useTopAppbarArg = arguments?.getBoolean("useTopAppbar", true) ?: true
        binding.maTopAppbar.visibility = if (useTopAppbarArg)  View.VISIBLE else View.GONE
    }
}