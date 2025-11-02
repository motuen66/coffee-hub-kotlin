package com.coffeehub.ui

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.coffeehub.R
import com.coffeehub.databinding.ActivityMainBinding
import com.coffeehub.util.SessionManager
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    
    @Inject
    lateinit var sessionManager: SessionManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        // Auto-login: Check if user is already logged in with Remember Me
        if (savedInstanceState == null) { // Only on first launch
            if (sessionManager.isLoggedIn() && sessionManager.isRememberMeEnabled()) {
                // Navigate to appropriate screen based on user role
                val startDestination = if (sessionManager.isAdmin()) {
                    R.id.adminDashboardFragment
                } else {
                    R.id.productListFragment
                }
                
                navController.navigate(startDestination)
            }
        }

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
