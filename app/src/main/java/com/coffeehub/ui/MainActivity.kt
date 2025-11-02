package com.coffeehub.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.coffeehub.R
import com.coffeehub.databinding.ActivityMainBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Setup bottom navigation with NavController
        binding.bottomNavigation.setupWithNavController(navController)

        // Show/hide bottom navigation based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.productListFragment -> {
                    // Show bottom nav for customer screens
                    binding.bottomNavigation.visibility = View.VISIBLE
                }
                R.id.loginFragment,
                R.id.registerFragment,
                R.id.adminDashboardFragment -> {
                    // Hide bottom nav for auth and admin screens
                    binding.bottomNavigation.visibility = View.GONE
                }
                else -> {
                    // Default: show for other customer screens
                    binding.bottomNavigation.visibility = View.VISIBLE
                }
            }
        }
    }
}
