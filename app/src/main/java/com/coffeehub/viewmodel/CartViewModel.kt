package com.coffeehub.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.coffeehub.data.repository.CartRepository
import com.coffeehub.domain.model.CartItem
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CartViewModel @Inject constructor(
    private val cartRepository: CartRepository
) : ViewModel() {

    // Observe cart items from repository
    val cartItems: LiveData<List<CartItem>> = cartRepository.cartItems.asLiveData()

    private val _subtotal = MutableLiveData<Double>(0.0)
    val subtotal: LiveData<Double> = _subtotal

    private val _deliveryFee = MutableLiveData<Double>(10000.0) // Fixed 10.000đ
    val deliveryFee: LiveData<Double> = _deliveryFee

    private val _tax = MutableLiveData<Double>(0.0) // 10% of subtotal
    val tax: LiveData<Double> = _tax

    private val _total = MutableLiveData<Double>(0.0)
    val total: LiveData<Double> = _total

    init {
        // Observe cart items and recalculate prices when they change
        cartItems.observeForever { items ->
            calculatePrices(items)
        }
    }

    fun addItem(item: CartItem) {
        viewModelScope.launch {
            cartRepository.addToCart(item)
        }
    }

    fun removeItem(productId: String) {
        viewModelScope.launch {
            cartRepository.removeFromCart(productId)
        }
    }

    fun updateQuantity(productId: String, newQuantity: Int) {
        viewModelScope.launch {
            cartRepository.updateQuantity(productId, newQuantity)
        }
    }

    fun clearCart() {
        viewModelScope.launch {
            cartRepository.clearCart()
        }
    }

    private fun calculatePrices(items: List<CartItem>) {
        
        // Calculate subtotal
        val subtotalAmount = items.sumOf { it.totalPrice }
        
        // Calculate tax (10% of subtotal)
        val taxAmount = subtotalAmount * 0.10
        
        // Calculate total
        val totalAmount = subtotalAmount + (_deliveryFee.value ?: 0.0) + taxAmount
        
        _subtotal.value = subtotalAmount
        _tax.value = taxAmount
        _total.value = totalAmount
    }
}
