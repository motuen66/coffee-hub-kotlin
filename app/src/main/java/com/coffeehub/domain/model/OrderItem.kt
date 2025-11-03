package com.coffeehub.domain.model

data class OrderItem(
    val productId: String = "",
    val productName: String = "",
    val size: String = "Medium", // Small, Medium, Large
    val quantity: Int = 0,
    val price: Double = 0.0,
    val imageUrl: String = ""
) {
    val subtotal: Double
        get() = quantity * price
}
