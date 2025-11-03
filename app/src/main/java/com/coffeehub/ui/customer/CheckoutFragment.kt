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
import com.coffeehub.R
import com.coffeehub.databinding.FragmentCheckoutBinding
import com.coffeehub.domain.model.Order
import com.coffeehub.domain.model.OrderItem
import com.coffeehub.domain.model.OrderStatus
import com.coffeehub.util.SessionManager
import com.coffeehub.viewmodel.CartViewModel
import com.coffeehub.viewmodel.OrderViewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class CheckoutFragment : Fragment() {

    private var _binding: FragmentCheckoutBinding? = null
    private val binding get() = _binding!!

    private val cartViewModel: CartViewModel by viewModels()
    private val orderViewModel: OrderViewModel by viewModels()

    @Inject
    lateinit var sessionManager: SessionManager

    private var subtotal: Double = 0.0
    private var deliveryFee: Double = 0.0
    private var total: Double = 0.0
    private var itemCount: Int = 0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCheckoutBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupListeners()
        observeViewModel()
    }

    private fun setupUI() {
        // Pre-fill customer name from session
        binding.etCustomerName.setText(sessionManager.getUserName())
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnPlaceOrder.setOnClickListener {
            validateAndPlaceOrder()
        }
    }

    private fun observeViewModel() {
        // Observe cart data
        cartViewModel.cartItems.observe(viewLifecycleOwner) { items ->
            itemCount = items.sumOf { it.quantity }
            binding.tvItemCount.text = "$itemCount items"
        }

        cartViewModel.subtotal.observe(viewLifecycleOwner) { value ->
            subtotal = value
            binding.tvSubtotal.text = formatPrice(value)
        }

        cartViewModel.deliveryFee.observe(viewLifecycleOwner) { value ->
            deliveryFee = value
            binding.tvDeliveryFee.text = formatPrice(value)
        }

        cartViewModel.total.observe(viewLifecycleOwner) { value ->
            total = value
            binding.tvTotal.text = formatPrice(value)
        }
    }

    private fun validateAndPlaceOrder() {
        val customerName = binding.etCustomerName.text.toString().trim()
        val phoneNumber = binding.etPhoneNumber.text.toString().trim()
        val notes = binding.etNotes.text.toString().trim()

        // Validation
        when {
            customerName.isEmpty() -> {
                binding.etCustomerName.error = "Please enter your name"
                binding.etCustomerName.requestFocus()
                return
            }
            phoneNumber.isEmpty() -> {
                binding.etPhoneNumber.error = "Please enter your phone number"
                binding.etPhoneNumber.requestFocus()
                return
            }
            phoneNumber.length < 10 -> {
                binding.etPhoneNumber.error = "Please enter a valid phone number"
                binding.etPhoneNumber.requestFocus()
                return
            }
            itemCount == 0 -> {
                Toast.makeText(requireContext(), "Your cart is empty", Toast.LENGTH_SHORT).show()
                return
            }
        }

        // Get payment method
        val paymentMethod = when (binding.rgPaymentMethod.checkedRadioButtonId) {
            R.id.rbCard -> "Card"
            else -> "Cash"
        }

        placeOrder(customerName, phoneNumber, notes, paymentMethod)
    }

    private fun placeOrder(
        customerName: String,
        phoneNumber: String,
        notes: String,
        paymentMethod: String
    ) {
        // Show loading
        binding.progressIndicator.visibility = View.VISIBLE
        binding.btnPlaceOrder.isEnabled = false

        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // Get cart items
                val cartItems = cartViewModel.cartItems.value ?: emptyList()

                // Convert CartItem to OrderItem
                val orderItems = cartItems.map { cartItem ->
                    OrderItem(
                        productId = cartItem.productId,
                        productName = cartItem.productName,
                        imageUrl = cartItem.productImage,
                        quantity = cartItem.quantity,
                        price = cartItem.price
                    )
                }

                // Create order
                val order = Order(
                    customerId = sessionManager.getUserId() ?: "",
                    customerName = customerName,
                    customerPhone = phoneNumber,
                    items = orderItems,
                    subtotal = subtotal,
                    deliveryFee = deliveryFee,
                    tax = 0.0,
                    total = total,
                    paymentMethod = paymentMethod,
                    isPaid = false,
                    status = OrderStatus.PENDING,
                    timestamp = System.currentTimeMillis(),
                    notes = notes
                )

                // Save order to Firestore
                orderViewModel.createOrder(order) { success, message ->
                    // Hide loading
                    binding.progressIndicator.visibility = View.GONE
                    binding.btnPlaceOrder.isEnabled = true

                    if (success) {
                        // Show success dialog
                        showSuccessDialog()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            message,
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }

            } catch (e: Exception) {
                binding.progressIndicator.visibility = View.GONE
                binding.btnPlaceOrder.isEnabled = true
                Toast.makeText(
                    requireContext(),
                    "Failed to place order: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun showSuccessDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Order Placed!")
            .setMessage("Your order has been placed successfully. We'll notify you when it's ready.")
            .setPositiveButton("View Orders") { _, _ ->
                // Clear cart
                cartViewModel.clearCart()
                
                // Navigate to order history
                findNavController().navigate(R.id.action_checkoutFragment_to_orderHistory)
            }
            .setNegativeButton("Back to Home") { _, _ ->
                // Clear cart
                cartViewModel.clearCart()
                
                // Navigate to home
                findNavController().navigate(R.id.productListFragment)
            }
            .setCancelable(false)
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
