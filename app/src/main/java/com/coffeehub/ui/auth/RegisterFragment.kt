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
import com.coffeehub.databinding.FragmentRegisterBinding
import com.coffeehub.viewmodel.AuthUiState
import com.coffeehub.viewmodel.AuthViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupListeners()
        observeUiState()
    }

    private fun setupListeners() {
        binding.btnRegister.setOnClickListener {
            val name = binding.inputName.text.toString().trim()
            val email = binding.inputEmail.text.toString().trim()
            val password = binding.inputPassword.text.toString().trim()
            val confirmPassword = binding.inputConfirmPassword.text.toString().trim()

            if (validateInput(name, email, password, confirmPassword)) {
                viewModel.register(email, password, name)
            }
        }

        binding.btnBackToLogin.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun validateInput(name: String, email: String, password: String, confirmPassword: String): Boolean {
        if (name.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter your name", Toast.LENGTH_SHORT).show()
            return false
        }
        if (email.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter email", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter password", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password.length < 6) {
            Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
            return false
        }
        if (password != confirmPassword) {
            Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
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
                        binding.btnRegister.isEnabled = false
                    }
                    is AuthUiState.Success -> {
                        binding.progress.visibility = View.GONE
                        binding.btnRegister.isEnabled = true
                        Toast.makeText(requireContext(), "Registration successful!", Toast.LENGTH_SHORT).show()
                        
                        // Navigate to product list for customers
                        findNavController().navigate(R.id.action_registerFragment_to_productList)
                        viewModel.resetState()
                    }
                    is AuthUiState.Error -> {
                        binding.progress.visibility = View.GONE
                        binding.btnRegister.isEnabled = true
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
