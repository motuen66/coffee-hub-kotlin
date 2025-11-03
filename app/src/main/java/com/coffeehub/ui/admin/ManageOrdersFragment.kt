package com.coffeehub.ui.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.coffeehub.R
import com.coffeehub.databinding.FragmentManageOrdersBinding
import com.coffeehub.domain.model.Order
import com.coffeehub.domain.model.OrderStatus
import com.coffeehub.viewmodel.OrderUiState
import com.coffeehub.viewmodel.OrderViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManageOrdersFragment : Fragment() {

    private var _binding: FragmentManageOrdersBinding? = null
    private val binding get() = _binding!!

    private val viewModel: OrderViewModel by viewModels()
    private lateinit var adapter: AdminOrderAdapter

    private var allOrders: List<Order> = emptyList()
    private var currentFilter: OrderStatus? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentManageOrdersBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupTabLayout()
        setupRefreshButton()
        observeOrders()

        // Load orders after a short delay to ensure view is ready
        view.post {
            loadOrders()
        }
    }

    private fun setupRecyclerView() {
        adapter = AdminOrderAdapter(
            onNextStatus = { order -> handleNextStatus(order) },
            onViewDetails = { order -> showOrderDetails(order) }
        )

        binding.rvOrders.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ManageOrdersFragment.adapter
        }
    }

    private fun setupTabLayout() {
        binding.tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> currentFilter = null // All
                    1 -> currentFilter = OrderStatus.PENDING
                    2 -> currentFilter = OrderStatus.PREPARING
                    3 -> currentFilter = OrderStatus.READY
                    4 -> currentFilter = OrderStatus.COMPLETED
                }
                applyFilter()
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
    }

    private fun setupRefreshButton() {
        binding.toolbar.setOnMenuItemClickListener { menuItem ->
            if (menuItem.itemId == R.id.action_refresh) {
                loadOrders()
                true
            } else {
                false
            }
        }
    }

    private fun observeOrders() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                viewModel.uiState.collect { state ->
                    when (state) {
                        is OrderUiState.Loading -> {
                            binding.progressIndicator.visibility = View.VISIBLE
                            binding.rvOrders.visibility = View.GONE
                            binding.layoutEmptyState.visibility = View.GONE
                        }
                        is OrderUiState.Success -> {
                            binding.progressIndicator.visibility = View.GONE
                            allOrders = state.orders.sortedByDescending { it.timestamp }
                            applyFilter()
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
            } catch (e: Exception) {
                e.printStackTrace()
                binding.progressIndicator.visibility = View.GONE
                binding.layoutEmptyState.visibility = View.VISIBLE
                Toast.makeText(
                    requireContext(),
                    "Failed to load orders: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun loadOrders() {
        viewModel.loadAllOrders()
    }

    private fun applyFilter() {
        val filteredOrders = if (currentFilter == null) {
            allOrders
        } else {
            allOrders.filter { it.status == currentFilter }
        }

        if (filteredOrders.isEmpty()) {
            binding.rvOrders.visibility = View.GONE
            binding.layoutEmptyState.visibility = View.VISIBLE
        } else {
            binding.rvOrders.visibility = View.VISIBLE
            binding.layoutEmptyState.visibility = View.GONE
            adapter.submitList(filteredOrders)
        }
    }

    private fun handleNextStatus(order: Order) {
        val nextStatus = when (order.status) {
            OrderStatus.PENDING -> OrderStatus.COMPLETED
            else -> return
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Complete Order")
            .setMessage("Mark this order as completed?")
            .setPositiveButton("Yes") { _, _ ->
                updateOrderStatus(order, nextStatus)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun updateOrderStatus(order: Order, newStatus: OrderStatus) {
        viewModel.updateOrderStatus(
            orderId = order.id,
            status = newStatus,
            onResult = { success, message ->
                if (success) {
                    Toast.makeText(
                        requireContext(),
                        "Order status updated successfully",
                        Toast.LENGTH_SHORT
                    ).show()
                    loadOrders() // Refresh list
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed to update status: $message",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun showOrderDetails(order: Order) {
        val items = order.items.joinToString("\n") { item ->
            "• ${item.productName} (${item.size}) × ${item.quantity} - ${String.format("%,.0fđ", item.price * item.quantity)}"
        }

        val message = """
            Customer: ${order.customerName}
            Phone: ${order.customerPhone}
            Payment: ${order.paymentMethod}
            
            Items:
            $items
            
            Subtotal: ${String.format("%,.0fđ", order.subtotal)}
            Delivery Fee: ${String.format("%,.0fđ", order.deliveryFee)}
            Total: ${String.format("%,.0fđ", order.total)}
            
            ${if (!order.notes.isNullOrBlank()) "Notes: ${order.notes}" else ""}
        """.trimIndent()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Order Details #${order.id.take(8).uppercase()}")
            .setMessage(message)
            .setPositiveButton("Close", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
