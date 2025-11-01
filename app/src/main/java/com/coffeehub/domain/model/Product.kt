package com.coffeehub.domain.model

import com.google.firebase.Timestamp

data class Product(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val price: Double = 0.0,
    val imageUrl: String = "",
    val category: String = "", // e.g., "Espresso", "Latte", "Cappuccino"
    val stock: Int = 0,
    val isAvailable: Boolean = true,
    val createdAt: Timestamp = Timestamp.now(),
    val extra: String? = null, // Ingredients/components
    val rating: Double = 0.0 // Product rating (0.0 - 5.0)
)
