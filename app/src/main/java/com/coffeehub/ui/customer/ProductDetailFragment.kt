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
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.coffeehub.R
import com.coffeehub.databinding.FragmentProductDetailBinding
import com.coffeehub.domain.model.Product
import com.coffeehub.viewmodel.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.text.NumberFormat
import java.util.Locale

/**
 * Product Detail Fragment - Shows detailed product information
 * with size selector, quantity control, and add to cart functionality
 */
@AndroidEntryPoint
class ProductDetailFragment : Fragment() {

    private var _binding: FragmentProductDetailBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ProductViewModel by viewModels()
    
    private var selectedSize: String = "Medium"
    private var quantity: Int = 1
    private var isFavorite: Boolean = false
    
    // Product data from navigation arguments
    private var productId: String = ""
    private var productName: String = ""
    private var productDescription: String = ""
    private var productPrice: Double = 0.0
    private var productImageUrl: String = ""
    private var productCategory: String = ""
    private var productRating: Double = 0.0

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        // Get product data from navigation arguments
        arguments?.let { args ->
            productId = args.getString("productId", "")
            productName = args.getString("productName", "")
            productDescription = args.getString("productDescription", "")
            productPrice = args.getDouble("productPrice", 0.0)
            productImageUrl = args.getString("productImageUrl", "")
            productCategory = args.getString("productCategory", "")
            productRating = args.getDouble("productRating", 0.0)
        }
        
        setupUI()
        setupClickListeners()
        loadProductDetails()
    }
    
    private fun setupUI() {
        // Set initial values
        binding.tvQuantity.text = quantity.toString()
        
        // Set Medium as default selection
        binding.btnMedium.isChecked = true
        selectedSize = "Medium"
        
        // Display product data
        binding.tvProductName.text = productName
        binding.tvDescription.text = productDescription.ifEmpty {
            "Delicious coffee drink made with love and care."
        }
        binding.tvRating.text = productRating.toString()
        
        updatePrice()
        loadProductImage()
    }
    
    private fun loadProductImage() {
        if (productImageUrl.isNotEmpty()) {
            // Load from imageUrl (for future Firebase images)
            Glide.with(this)
                .load(productImageUrl)
                .centerCrop()
                .placeholder(R.drawable.shimmer_placeholder)
                .error(R.drawable.ic_empty_coffee)
                .into(binding.ivProductImage)
        } else {
            // Fallback: Try to load from drawable using product name
            val imageName = productName.lowercase().replace(" ", "")
            val imageResId = resources.getIdentifier(imageName, "drawable", requireContext().packageName)
            
            if (imageResId != 0) {
                Glide.with(this)
                    .load(imageResId)
                    .centerCrop()
                    .placeholder(R.drawable.shimmer_placeholder)
                    .error(R.drawable.ic_empty_coffee)
                    .into(binding.ivProductImage)
            } else {
                // Show empty state if no image found
                binding.ivProductImage.setImageResource(R.drawable.ic_empty_coffee)
            }
        }
    }
    
    private fun setupClickListeners() {
        // Back button
        binding.btnBack.setOnClickListener {
            findNavController().navigateUp()
        }
        
        // Favorite button
        binding.btnFavorite.setOnClickListener {
            toggleFavorite()
        }
        
        // Size selector
        binding.groupSize.addOnButtonCheckedListener { group, checkedId, isChecked ->
            if (isChecked) {
                selectedSize = when (checkedId) {
                    R.id.btnSmall -> "Small"
                    R.id.btnMedium -> "Medium"
                    R.id.btnLarge -> "Large"
                    else -> "Medium"
                }
                updatePrice()
            }
        }
        
        // Quantity controls
        binding.btnDecrease.setOnClickListener {
            if (quantity > 1) {
                quantity--
                binding.tvQuantity.text = quantity.toString()
                updatePrice()
            }
        }
        
        binding.btnIncrease.setOnClickListener {
            quantity++
            binding.tvQuantity.text = quantity.toString()
            updatePrice()
        }
        
        // Add to cart button
        binding.btnAddToCart.setOnClickListener {
            addToCart()
        }
    }
    
    private fun toggleFavorite() {
        isFavorite = !isFavorite
        
        val iconRes = if (isFavorite) {
            android.R.drawable.btn_star_big_on
        } else {
            android.R.drawable.btn_star_big_off
        }
        
        binding.btnFavorite.setImageResource(iconRes)
        
        // TODO: Save favorite status to Firebase/Room
        Toast.makeText(
            requireContext(),
            if (isFavorite) "Added to favorites" else "Removed from favorites",
            Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun updatePrice() {
        // Calculate price based on size and quantity
        val sizeMultiplier = when (selectedSize) {
            "Small" -> 0.8
            "Medium" -> 1.0
            "Large" -> 1.2
            else -> 1.0
        }
        
        val totalPrice = productPrice * sizeMultiplier * quantity
        
        // Format price in VND with custom format (e.g., "120.000đ")
        val formatter = NumberFormat.getNumberInstance(Locale("vi", "VN"))
        formatter.maximumFractionDigits = 0
        formatter.minimumFractionDigits = 0
        binding.tvPrice.text = "${formatter.format(totalPrice)}đ"
    }
    
    private fun addToCart() {
        // TODO: Implement add to cart functionality
        // viewModel.addToCart(productId, selectedSize, quantity)
        
        Toast.makeText(
            requireContext(),
            "Added to cart: $quantity x $productName ($selectedSize)",
            Toast.LENGTH_SHORT
        ).show()
        
        // Navigate back or to cart
        findNavController().navigateUp()
    }
    
    private fun loadProductDetails() {
        // TODO: Observe product from ViewModel
        // viewLifecycleOwner.lifecycleScope.launch {
        //     viewModel.getProductById(productId).collect { product ->
        //         product?.let {
        //             productName = it.name
        //             productPrice = it.price
        //             productDescription = it.description
        //             productImageUrl = it.imageUrl
        //             productRating = it.rating ?: 4.5
        //             setupUI()
        //         }
        //     }
        // }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
