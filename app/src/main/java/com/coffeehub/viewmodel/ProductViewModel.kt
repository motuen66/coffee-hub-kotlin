package com.coffeehub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeehub.data.repository.ProductRepository
import com.coffeehub.domain.model.Product
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class ProductUiState {
    object Idle : ProductUiState()
    object Loading : ProductUiState()
    data class Success(val products: List<Product>) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
}

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Idle)
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Product>>(emptyList())
    val searchResults: StateFlow<List<Product>> = _searchResults.asStateFlow()
    
    private var allProducts: List<Product> = emptyList()
    private var currentCategory: String = ""
    private var currentSearchQuery: String = ""
    private var currentSortBy: SortBy = SortBy.NONE
    private var currentMinPrice: Float = 0f
    private var currentMaxPrice: Float = 150000f
    private var currentAvailableOnly: Boolean = false
    
    enum class SortBy {
        NONE,
        PRICE_LOW_TO_HIGH,
        PRICE_HIGH_TO_LOW,
        NAME_A_TO_Z,
        NAME_Z_TO_A
    }

    fun loadProducts() {
        _uiState.value = ProductUiState.Loading
        viewModelScope.launch {
            try {
                productRepository.getProducts()
                    .collect { products ->
                        allProducts = products
                        applyFilters()
                    }
            } catch (e: Exception) {
                _uiState.value = ProductUiState.Error(e.message ?: "Failed to load products")
            }
        }
    }
    
    fun searchProducts(query: String) {
        currentSearchQuery = query
        applyFilters()
    }
    
    fun filterByCategory(category: String) {
        currentCategory = category
        applyFilters()
    }
    
    fun applyAdvancedFilters(
        sortBy: SortBy = SortBy.NONE,
        minPrice: Float = 0f,
        maxPrice: Float = 150000f,
        availableOnly: Boolean = false
    ) {
        currentSortBy = sortBy
        currentMinPrice = minPrice
        currentMaxPrice = maxPrice
        currentAvailableOnly = availableOnly
        applyFilters()
    }
    
    private fun applyFilters() {
        var filtered = allProducts
        
        // Apply category filter
        if (currentCategory.isNotBlank()) {
            filtered = filtered.filter { 
                it.category.equals(currentCategory, ignoreCase = true) 
            }
        }
        
        // Apply search filter
        if (currentSearchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.name.contains(currentSearchQuery, ignoreCase = true) ||
                it.description.contains(currentSearchQuery, ignoreCase = true) ||
                it.category.contains(currentSearchQuery, ignoreCase = true)
            }
        }
        
        // Apply price range filter
        filtered = filtered.filter { product ->
            product.price >= currentMinPrice && product.price <= currentMaxPrice
        }
        
        // Apply availability filter
        if (currentAvailableOnly) {
            filtered = filtered.filter { it.isAvailable }
        }
        
        // Apply sorting
        filtered = when (currentSortBy) {
            SortBy.PRICE_LOW_TO_HIGH -> filtered.sortedBy { it.price }
            SortBy.PRICE_HIGH_TO_LOW -> filtered.sortedByDescending { it.price }
            SortBy.NAME_A_TO_Z -> filtered.sortedBy { it.name }
            SortBy.NAME_Z_TO_A -> filtered.sortedByDescending { it.name }
            SortBy.NONE -> filtered
        }
        
        _uiState.value = ProductUiState.Success(filtered)
    }

    fun addProduct(product: Product, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            productRepository.addProduct(product)
                .onSuccess { onResult(true, "Product added successfully") }
                .onFailure { onResult(false, it.message ?: "Failed to add product") }
        }
    }

    fun updateProduct(productId: String, product: Product, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            productRepository.updateProduct(productId, product)
                .onSuccess { onResult(true, "Product updated successfully") }
                .onFailure { onResult(false, it.message ?: "Failed to update product") }
        }
    }

    fun deleteProduct(productId: String, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            productRepository.deleteProduct(productId)
                .onSuccess { onResult(true, "Product deleted successfully") }
                .onFailure { onResult(false, it.message ?: "Failed to delete product") }
        }
    }
}
