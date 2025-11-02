package com.coffeehub.ui.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.coffeehub.R
import com.coffeehub.databinding.FragmentHomeBinding
import com.coffeehub.domain.model.Product
import com.coffeehub.ui.customer.adapter.ProductAdapter
import com.coffeehub.viewmodel.ProductUiState
import com.coffeehub.viewmodel.ProductViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Customer Home Fragment - Main landing page with search, categories, and popular products
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    
    private val viewModel: ProductViewModel by viewModels()
    private lateinit var productAdapter: ProductAdapter
    
    private var selectedCategory: String = "" // Default: All categories (empty string)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupBanner()
        setupRecyclerView()
        setupSearchBar()
        setupCategoryChips()
        setupClickListeners()
        observeProducts()
        
        // Load all products from Firebase (no filter initially)
        viewModel.loadProducts()
    }
    
    private fun setupBanner() {
        // Load beautiful Cappoccino image with Glide from drawable/image folder
        // Note: Android converts "Cappoccino.jpg" to lowercase "cappoccino"
        val imageResId = resources.getIdentifier("cappoccino", "drawable", requireContext().packageName)
        
        Glide.with(this)
            .load(imageResId)
            .apply(RequestOptions().transform(RoundedCorners(60))) // 60px rounded corners
            .into(binding.ivBannerCoffee)
        
        // Apply fade-in animation to banner image
        val fadeInAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.fade_in)
        binding.ivBannerCoffee.startAnimation(fadeInAnim)
        
        // Apply slide-in animation to banner text with staggered delays
        val slideInAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.slide_in_left)
        binding.tvBannerTitle.startAnimation(slideInAnim)
        
        binding.tvBannerSubtitle.postDelayed({
            binding.tvBannerSubtitle.startAnimation(slideInAnim)
        }, 100)
        
        binding.tvBannerPromo.postDelayed({
            binding.tvBannerPromo.startAnimation(slideInAnim)
        }, 150)
        
        binding.btnOrderNow.postDelayed({
            binding.btnOrderNow.startAnimation(slideInAnim)
        }, 200)
    }

    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            onProductClick = { product ->
                // Navigate to ProductDetailFragment with product data
                val bundle = Bundle().apply {
                    putString("productId", product.id)
                    putString("productName", product.name)
                    putString("productDescription", product.description)
                    putDouble("productPrice", product.price)
                    putString("productImageUrl", product.imageUrl)
                    putString("productCategory", product.category)
                    putDouble("productRating", product.rating)
                }
                findNavController().navigate(R.id.action_productList_to_productDetail, bundle)
            },
            onAddClick = { product ->
                // TODO: Add to cart
                Toast.makeText(requireContext(), "Added ${product.name} to cart", Toast.LENGTH_SHORT).show()
            }
        )
        
        binding.rvPopularCoffees.apply {
            layoutManager = GridLayoutManager(requireContext(), 2)
            adapter = productAdapter
            setHasFixedSize(true)
        }
    }

    private fun setupSearchBar() {
        binding.etSearch.doAfterTextChanged { text ->
            val query = text?.toString()?.trim() ?: ""
            viewModel.searchProducts(query)
        }
    }

    private fun setupCategoryChips() {
        // Set "All" as default selected
        binding.chipAll.isChecked = true
        
        binding.chipGroupCategories.setOnCheckedChangeListener { group, checkedId ->
            selectedCategory = when (checkedId) {
                binding.chipEspresso.id -> "Espresso"
                binding.chipCappuccino.id -> "Cappuccino"
                binding.chipLatte.id -> "Latte"
                binding.chipAmericano.id -> "Americano"
                binding.chipAll.id -> "" // Empty string = show all
                else -> ""
            }
            viewModel.filterByCategory(selectedCategory)
        }
        
        // Trigger initial filter for "All" category
        viewModel.filterByCategory(selectedCategory)
    }

    private fun setupClickListeners() {
        binding.btnFilter.setOnClickListener {
            showFilterBottomSheet()
        }
        
        binding.btnOrderNow.setOnClickListener {
            // Scroll to products section
            binding.rvPopularCoffees.smoothScrollToPosition(0)
        }
        
        binding.tvSeeAllCategories.setOnClickListener {
            // TODO: Navigate to all categories
            Toast.makeText(requireContext(), "See all categories", Toast.LENGTH_SHORT).show()
        }
        
        binding.tvSeeAllProducts.setOnClickListener {
            // TODO: Navigate to all products
            Toast.makeText(requireContext(), "See all products", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun showFilterBottomSheet() {
        val filterBottomSheet = FilterBottomSheetFragment.newInstance { filterOptions ->
            // Map FilterBottomSheet.SortBy to ProductViewModel.SortBy
            val sortBy = when (filterOptions.sortBy) {
                FilterBottomSheetFragment.SortBy.PRICE_LOW_TO_HIGH -> 
                    ProductViewModel.SortBy.PRICE_LOW_TO_HIGH
                FilterBottomSheetFragment.SortBy.PRICE_HIGH_TO_LOW -> 
                    ProductViewModel.SortBy.PRICE_HIGH_TO_LOW
                FilterBottomSheetFragment.SortBy.NAME_A_TO_Z -> 
                    ProductViewModel.SortBy.NAME_A_TO_Z
                FilterBottomSheetFragment.SortBy.NAME_Z_TO_A -> 
                    ProductViewModel.SortBy.NAME_Z_TO_A
                FilterBottomSheetFragment.SortBy.NONE -> 
                    ProductViewModel.SortBy.NONE
            }
            
            viewModel.applyAdvancedFilters(
                sortBy = sortBy,
                minPrice = filterOptions.minPrice,
                maxPrice = filterOptions.maxPrice,
                availableOnly = filterOptions.availableOnly
            )
        }
        
        filterBottomSheet.show(parentFragmentManager, "FilterBottomSheet")
    }

    private fun observeProducts() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is ProductUiState.Idle -> {
                        binding.progress.visibility = View.GONE
                        binding.shimmerLayout.visibility = View.GONE
                    }
                    is ProductUiState.Loading -> {
                        // Show shimmer skeleton instead of spinner
                        binding.progress.visibility = View.GONE
                        binding.shimmerLayout.visibility = View.VISIBLE
                        binding.emptyStateLayout.visibility = View.GONE
                        binding.rvPopularCoffees.visibility = View.GONE
                    }
                    is ProductUiState.Success -> {
                        binding.progress.visibility = View.GONE
                        binding.shimmerLayout.visibility = View.GONE
                        
                        if (state.products.isEmpty()) {
                            // Show beautiful empty state
                            binding.emptyStateLayout.visibility = View.VISIBLE
                            binding.rvPopularCoffees.visibility = View.GONE
                        } else {
                            // Show products
                            binding.emptyStateLayout.visibility = View.GONE
                            binding.rvPopularCoffees.visibility = View.VISIBLE
                            productAdapter.submitList(state.products)
                        }
                    }
                    is ProductUiState.Error -> {
                        binding.progress.visibility = View.GONE
                        binding.shimmerLayout.visibility = View.GONE
                        binding.emptyStateLayout.visibility = View.VISIBLE
                        
                        Toast.makeText(
                            requireContext(),
                            "Error: ${state.message}",
                            Toast.LENGTH_LONG
                        ).show()
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
