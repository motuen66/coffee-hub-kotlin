package com.coffeehub.domain.model

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val category: String = "", // e.g., "Espresso", "Latte", "Cappuccino"
    val stock: Int = 0,
    val isAvailable: Boolean = true,
    val createdAt: Long = System.currentTimeMillis()
)
