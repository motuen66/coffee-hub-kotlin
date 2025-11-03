package com.coffeehub.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.coffeehub.R
import com.coffeehub.databinding.FragmentCartBinding
import com.coffeehub.viewmodel.CartViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class CartFragment : Fragment() {

    private var _binding: FragmentCartBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by viewModels()
    private lateinit var cartAdapter: CartAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCartBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupListeners()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        cartAdapter = CartAdapter(
            onQuantityChange = { productId, newQty ->
                cartViewModel.updateQuantity(productId, newQty)
            },
            onRemove = { productId ->
                showRemoveConfirmation(productId)
            }
        )

        binding.rvCartItems.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = cartAdapter
        }
    }

    private fun setupListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Checkout button
        binding.btnCheckout.setOnClickListener {
            // Navigate to CheckoutFragment
            findNavController().navigate(R.id.action_cartFragment_to_checkoutFragment)
        }
    }

    private fun observeViewModel() {
        // Cart items
        cartViewModel.cartItems.observe(viewLifecycleOwner) { items ->
            cartAdapter.submitList(items)
            
            // Show/hide empty state
            if (items.isEmpty()) {
                binding.rvCartItems.visibility = View.GONE
                binding.cardPriceSummary.visibility = View.GONE
                binding.btnCheckout.visibility = View.GONE
                binding.layoutEmptyCart.visibility = View.VISIBLE
            } else {
                binding.rvCartItems.visibility = View.VISIBLE
                binding.cardPriceSummary.visibility = View.VISIBLE
                binding.btnCheckout.visibility = View.VISIBLE
                binding.layoutEmptyCart.visibility = View.GONE
            }
        }

        // Subtotal
        cartViewModel.subtotal.observe(viewLifecycleOwner) { subtotal ->
            binding.tvSubtotal.text = formatPrice(subtotal)
        }

        // Delivery fee
        cartViewModel.deliveryFee.observe(viewLifecycleOwner) { fee ->
            binding.tvDeliveryFee.text = formatPrice(fee)
        }

        // Total
        cartViewModel.total.observe(viewLifecycleOwner) { total ->
            binding.tvTotal.text = formatPrice(total)
        }
    }

    private fun showRemoveConfirmation(productId: String) {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Remove Item")
            .setMessage("Are you sure you want to remove this item from your cart?")
            .setPositiveButton("Remove") { _, _ ->
                cartViewModel.removeItem(productId)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun formatPrice(price: Double): String {
        return String.format("%,.0fđ", price)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
