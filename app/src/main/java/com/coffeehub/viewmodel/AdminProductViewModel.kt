package com.coffeehub.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeehub.data.repository.ProductRepository
import com.coffeehub.domain.model.Product
import com.google.firebase.Timestamp
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class AdminProductUiState {
    object Loading : AdminProductUiState()
    data class Success(val products: List<Product>) : AdminProductUiState()
    data class Error(val message: String) : AdminProductUiState()
}

@HiltViewModel
class AdminProductViewModel @Inject constructor(
    private val productRepository: ProductRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<AdminProductUiState>(AdminProductUiState.Loading)
    val uiState: StateFlow<AdminProductUiState> = _uiState.asStateFlow()

    private val _allProducts = MutableStateFlow<List<Product>>(emptyList())

    init {
        loadProducts()
    }

    fun loadProducts() {
        viewModelScope.launch {
            try {
                _uiState.value = AdminProductUiState.Loading
                productRepository.getProducts()
                    .collect { products ->
                        _allProducts.value = products
                        _uiState.value = AdminProductUiState.Success(products)
                    }
            } catch (e: Exception) {
                _uiState.value = AdminProductUiState.Error(e.message ?: "Failed to load products")
            }
        }
    }

    fun searchProducts(query: String) {
        if (query.isBlank()) {
            _uiState.value = AdminProductUiState.Success(_allProducts.value)
            return
        }

        val filtered = _allProducts.value.filter { product ->
            product.name.contains(query, ignoreCase = true) ||
            product.category?.contains(query, ignoreCase = true) == true
        }
        _uiState.value = AdminProductUiState.Success(filtered)
    }

    fun addProduct(
        name: String,
        price: Double,
        category: String,
        description: String,
        stock: Int,
        imageUri: Uri?,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Create product with temporary ID
                val tempId = System.currentTimeMillis().toString()
                
                // Upload image if provided (optional)
                var imageUrl = ""
                if (imageUri != null) {
                    productRepository.saveProductImage(imageUri, tempId)
                        .onSuccess { url -> 
                            imageUrl = url 
                        }
                        .onFailure { 
                            // Image save failed, but continue without image
                            imageUrl = ""
                        }
                }

                // Create product object
                val product = Product(
                    name = name,
                    description = description,
                    price = price,
                    imageUrl = imageUrl,
                    category = category,
                    stock = stock,
                    isAvailable = true,
                    createdAt = Timestamp.now()
                )

                // Add to Firestore
                productRepository.addProduct(product)
                    .onSuccess { 
                        onResult(true, "Product added successfully")
                    }
                    .onFailure { 
                        onResult(false, "Failed to add product: ${it.message}")
                    }

            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    fun updateProduct(
        productId: String,
        name: String,
        price: Double,
        category: String,
        description: String,
        stock: Int,
        imageUri: Uri?,
        currentImageUrl: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                // Upload new image if provided (optional)
                var imageUrl = currentImageUrl
                if (imageUri != null) {
                    productRepository.saveProductImage(imageUri, productId)
                        .onSuccess { url -> 
                            imageUrl = url 
                        }
                        .onFailure { 
                            // Keep old image URL if save fails
                            imageUrl = currentImageUrl
                        }
                }

                // Create updated product object
                val product = Product(
                    id = productId,
                    name = name,
                    description = description,
                    price = price,
                    imageUrl = imageUrl,
                    category = category,
                    stock = stock,
                    isAvailable = stock > 0,
                    createdAt = Timestamp.now() // This should ideally preserve original
                )

                // Update in Firestore
                productRepository.updateProduct(productId, product)
                    .onSuccess { 
                        onResult(true, "Product updated successfully")
                    }
                    .onFailure { 
                        onResult(false, "Failed to update product: ${it.message}")
                    }

            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }

    fun deleteProduct(product: Product, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            try {
                // Delete product image from storage
                if (product.imageUrl.isNotEmpty()) {
                    productRepository.deleteProductImage(product.imageUrl)
                }

                // Delete product from Firestore
                productRepository.deleteProduct(product.id)
                    .onSuccess { 
                        onResult(true, "Product deleted successfully")
                    }
                    .onFailure { 
                        onResult(false, "Failed to delete product: ${it.message}")
                    }

            } catch (e: Exception) {
                onResult(false, "Error: ${e.message}")
            }
        }
    }
}
