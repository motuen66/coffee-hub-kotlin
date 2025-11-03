package com.coffeehub.domain.model

data class Order(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val customerPhone: String = "",
    val items: List<OrderItem> = emptyList(),
    val subtotal: Double = 0.0,
    val deliveryFee: Double = 0.0,
    val tax: Double = 0.0,
    val total: Double = 0.0,
    val paymentMethod: String = "Cash", // Cash, Card
    val isPaid: Boolean = false,
    val status: OrderStatus = OrderStatus.PENDING,
    val timestamp: Long = System.currentTimeMillis(),
    val notes: String = ""
)

enum class OrderStatus {
    PENDING,
    PREPARING,
    READY,
    COMPLETED,
    CANCELLED
}
