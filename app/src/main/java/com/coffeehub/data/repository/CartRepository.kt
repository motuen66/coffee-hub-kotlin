package com.coffeehub.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.coffeehub.domain.model.CartItem
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CartRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences = 
        context.getSharedPreferences("cart_prefs", Context.MODE_PRIVATE)
    
    private val gson = Gson()
    
    private val _cartItems = MutableStateFlow<List<CartItem>>(emptyList())
    val cartItems: Flow<List<CartItem>> = _cartItems.asStateFlow()
    
    init {
        // Load saved cart items on initialization
        loadCartItems()
    }
    
    private fun loadCartItems() {
        val json = prefs.getString(KEY_CART_ITEMS, null)
        if (json != null) {
            val type = object : TypeToken<List<CartItem>>() {}.type
            val items: List<CartItem> = gson.fromJson(json, type)
            _cartItems.value = items
        }
    }
    
    private fun saveCartItems(items: List<CartItem>) {
        val json = gson.toJson(items)
        prefs.edit().putString(KEY_CART_ITEMS, json).apply()
        _cartItems.value = items
    }
    
    fun addToCart(item: CartItem) {
        val currentItems = _cartItems.value.toMutableList()
        
        // Check if item already exists (same productId and size)
        val existingIndex = currentItems.indexOfFirst { 
            it.productId == item.productId && it.size == item.size 
        }
        
        if (existingIndex != -1) {
            // Update quantity if item exists
            val existingItem = currentItems[existingIndex]
            currentItems[existingIndex] = existingItem.copy(
                quantity = existingItem.quantity + item.quantity
            )
        } else {
            // Add new item with unique ID
            val newItem = item.copy(id = generateCartItemId())
            currentItems.add(newItem)
        }
        
        saveCartItems(currentItems)
    }
    
    fun removeFromCart(productId: String) {
        val currentItems = _cartItems.value.toMutableList()
        currentItems.removeAll { it.productId == productId }
        saveCartItems(currentItems)
    }
    
    fun updateQuantity(productId: String, newQuantity: Int) {
        if (newQuantity < 1) return
        
        val currentItems = _cartItems.value.toMutableList()
        val itemIndex = currentItems.indexOfFirst { it.productId == productId }
        
        if (itemIndex != -1) {
            currentItems[itemIndex] = currentItems[itemIndex].copy(quantity = newQuantity)
            saveCartItems(currentItems)
        }
    }
    
    fun clearCart() {
        saveCartItems(emptyList())
    }
    
    fun getCartItemCount(): Int {
        return _cartItems.value.sumOf { it.quantity }
    }
    
    private fun generateCartItemId(): String {
        return "cart_${System.currentTimeMillis()}"
    }
    
    companion object {
        private const val KEY_CART_ITEMS = "cart_items"
    }
}
