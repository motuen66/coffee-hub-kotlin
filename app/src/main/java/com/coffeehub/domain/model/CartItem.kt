package com.coffeehub.domain.model

data class CartItem(
    val id: String = "",
    val productId: String = "",
    val productName: String = "",
    val productImage: String = "",
    val size: String = "Medium", // Small, Medium, Large
    val quantity: Int = 1,
    val price: Double = 0.0 // Price per item
) {
    // Computed property - always recalculates based on current quantity
    val totalPrice: Double
        get() = price * quantity
}
