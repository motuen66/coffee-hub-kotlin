package com.coffeehub.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.coffeehub.R
import com.coffeehub.databinding.FragmentLoginBinding
import com.coffeehub.viewmodel.AuthUiState
import com.coffeehub.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeUiState()
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()

            if (validateInput(email, password)) {
                viewModel.login(email, password)
            }
        }

        binding.btnRegister.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_registerFragment)
        }
    }

    private fun validateInput(email: String, password: String): Boolean {
        if (email.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter email", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter password", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is AuthUiState.Idle -> {
                        binding.progress.visibility = View.GONE
                    }
                    is AuthUiState.Loading -> {
                        binding.progress.visibility = View.VISIBLE
                        binding.btnLogin.isEnabled = false
                    }
                    is AuthUiState.Success -> {
                        binding.progress.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                        
                        // Debug: Show user info
                        Toast.makeText(requireContext(), "User: ${state.user.email}, isAdmin: ${state.user.isAdmin}", Toast.LENGTH_LONG).show()
                        
                        // Navigate based on user role
                        if (state.user.isAdmin) {
                            Toast.makeText(requireContext(), "Navigating to Admin Dashboard", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_loginFragment_to_adminDashboard)
                        } else {
                            Toast.makeText(requireContext(), "Navigating to Product List", Toast.LENGTH_SHORT).show()
                            findNavController().navigate(R.id.action_loginFragment_to_productList)
                        }
                        viewModel.resetState()
                    }
                    is AuthUiState.Error -> {
                        binding.progress.visibility = View.GONE
                        binding.btnLogin.isEnabled = true
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
