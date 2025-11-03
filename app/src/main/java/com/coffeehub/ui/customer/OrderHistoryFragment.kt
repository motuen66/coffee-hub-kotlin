package com.coffeehub.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.coffeehub.R
import com.coffeehub.databinding.FragmentOrderHistoryBinding
import com.coffeehub.domain.model.Order
import com.coffeehub.util.SessionManager
import com.coffeehub.viewmodel.OrderUiState
import com.coffeehub.viewmodel.OrderViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class OrderHistoryFragment : Fragment() {

    private var _binding: FragmentOrderHistoryBinding? = null
    private val binding get() = _binding!!

    private val orderViewModel: OrderViewModel by viewModels()
    
    @Inject
    lateinit var sessionManager: SessionManager

    private lateinit var orderAdapter: OrderAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrderHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        observeViewModel()
        loadOrders()
    }

    private fun setupRecyclerView() {
        orderAdapter = OrderAdapter { order ->
            onOrderClick(order)
        }

        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = orderAdapter
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            orderViewModel.uiState.collect { state ->
                when (state) {
                    is OrderUiState.Loading -> {
                        binding.progressIndicator.visibility = View.VISIBLE
                        binding.rvOrders.visibility = View.GONE
                        binding.layoutEmptyState.visibility = View.GONE
                    }
                    is OrderUiState.Success -> {
                        binding.progressIndicator.visibility = View.GONE
                        
                        if (state.orders.isEmpty()) {
                            binding.rvOrders.visibility = View.GONE
                            binding.layoutEmptyState.visibility = View.VISIBLE
                        } else {
                            binding.rvOrders.visibility = View.VISIBLE
                            binding.layoutEmptyState.visibility = View.GONE
                            orderAdapter.submitList(state.orders)
                        }
                    }
                    is OrderUiState.Error -> {
                        binding.progressIndicator.visibility = View.GONE
                        binding.rvOrders.visibility = View.GONE
                        binding.layoutEmptyState.visibility = View.VISIBLE
                        
                        Toast.makeText(
                            requireContext(),
                            "Error: ${state.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    private fun loadOrders() {
        try {
            val userId = sessionManager.getUserId()
            if (userId.isNullOrEmpty()) {
                binding.progressIndicator.visibility = View.GONE
                binding.rvOrders.visibility = View.GONE
                binding.layoutEmptyState.visibility = View.VISIBLE
                Toast.makeText(requireContext(), "Please login first", Toast.LENGTH_SHORT).show()
                return
            }
            
            orderViewModel.loadOrdersByCustomer(userId)
        } catch (e: Exception) {
            binding.progressIndicator.visibility = View.GONE
            binding.rvOrders.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
            Toast.makeText(requireContext(), "Error loading orders: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun onOrderClick(order: Order) {
        // TODO: Navigate to order detail screen
        // For now, just show a toast
        Toast.makeText(
            requireContext(),
            "Order #${order.id.takeLast(6)} - ${order.status.name}",
            Toast.LENGTH_SHORT
        ).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
