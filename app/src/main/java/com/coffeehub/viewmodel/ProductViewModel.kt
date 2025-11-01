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
    object Loading : ProductUiState()
    data class Success(val products: List<Product>) : ProductUiState()
    data class Error(val message: String) : ProductUiState()
}

@HiltViewModel
class ProductViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<ProductUiState>(ProductUiState.Loading)
    val uiState: StateFlow<ProductUiState> = _uiState.asStateFlow()

    private val _searchResults = MutableStateFlow<List<Product>>(emptyList())
    val searchResults: StateFlow<List<Product>> = _searchResults.asStateFlow()

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            productRepository.getProducts()
                .collect { products ->
                    _uiState.value = ProductUiState.Success(products)
                }
        }
    }

    fun searchProducts(query: String) {
        if (query.isBlank()) {
            loadProducts()
            return
        }
        viewModelScope.launch {
            productRepository.searchProducts(query)
                .collect { products ->
                    _searchResults.value = products
                }
        }
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
