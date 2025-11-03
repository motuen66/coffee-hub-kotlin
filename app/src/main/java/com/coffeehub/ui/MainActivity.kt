package com.coffeehub.ui

import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.coffeehub.R
import com.coffeehub.databinding.ActivityMainBinding
import com.coffeehub.util.SessionManager
import com.coffeehub.viewmodel.CartViewModel
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    
    @Inject
    lateinit var sessionManager: SessionManager
    
    private val cartViewModel: CartViewModel by viewModels()

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
        binding.adminBottomNavigation.setupWithNavController(navController)
        
        // Observe cart items to update badge
        cartViewModel.cartItems.observe(this) { items ->
            val itemCount = items.sumOf { it.quantity }
            updateCartBadge(itemCount)
        }

        // Show/hide bottom navigation based on destination
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Check if user is admin
            val isAdmin = sessionManager.isAdmin()
            
            val isAuthScreen = when (destination.id) {
                R.id.loginFragment,
                R.id.registerFragment -> true
                else -> false
            }
            
            when {
                isAuthScreen -> {
                    // Hide both navs for auth screens
                    binding.bottomNavigation.visibility = View.GONE
                    binding.adminBottomNavigation.visibility = View.GONE
                }
                isAdmin -> {
                    // Show admin nav for all admin destinations
                    binding.bottomNavigation.visibility = View.GONE
                    binding.adminBottomNavigation.visibility = View.VISIBLE
                }
                else -> {
                    // Show customer nav for all customer destinations
                    binding.bottomNavigation.visibility = View.VISIBLE
                    binding.adminBottomNavigation.visibility = View.GONE
                }
            }
        }
    }
    
    private fun updateCartBadge(count: Int) {
        val badge = binding.bottomNavigation.getOrCreateBadge(R.id.cartFragment)
        if (count > 0) {
            badge.isVisible = true
            badge.number = count
            badge.backgroundColor = getColor(R.color.coffee_brown)
            badge.badgeTextColor = getColor(R.color.white)
        } else {
            badge.isVisible = false
        }
    }
}
