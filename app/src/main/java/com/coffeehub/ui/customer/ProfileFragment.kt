package com.coffeehub.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.coffeehub.R
import com.coffeehub.databinding.FragmentProfileBinding
import com.coffeehub.util.SessionManager
import com.coffeehub.viewmodel.AuthViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    
    @Inject
    lateinit var sessionManager: SessionManager
    
    private val authViewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        displayUserInfo()
        setupClickListeners()
    }

    private fun displayUserInfo() {
        binding.tvUserName.text = sessionManager.getUserName() ?: "Guest"
        binding.tvUserEmail.text = sessionManager.getUserEmail() ?: "Not logged in"
        
        // Show admin badge if user is admin
        if (sessionManager.isAdmin()) {
            binding.tvUserRole.visibility = View.VISIBLE
            binding.tvUserRole.text = "Admin"
        } else {
            binding.tvUserRole.visibility = View.GONE
        }
    }

    private fun setupClickListeners() {
        // Logout button
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }
        
        // Edit profile (TODO: Implement later)
        binding.btnEditProfile.setOnClickListener {
            // TODO: Navigate to edit profile screen
        }
        
        // Order history
        binding.btnOrderHistory.setOnClickListener {
            // TODO: Navigate to order history
        }
        
        // Favorites
        binding.btnFavorites.setOnClickListener {
            // TODO: Navigate to favorites
        }
        
        // Settings
        binding.btnSettings.setOnClickListener {
            // TODO: Navigate to settings
        }
    }

    private fun showLogoutConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { _, _ ->
                performLogout()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun performLogout() {
        // Logout from Firebase
        authViewModel.logout()
        
        // Clear session
        sessionManager.clearSession()
        
        // Navigate to login screen
        findNavController().navigate(R.id.action_profile_to_login)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
