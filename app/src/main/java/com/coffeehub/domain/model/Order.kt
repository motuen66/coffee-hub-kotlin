package com.coffeehub.domain.model

data class Order(
    val id: String = "",
    val customerId: String = "",
    val customerName: String = "",
    val items: List<OrderItem> = emptyList(),
    val total: Double = 0.0,
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
