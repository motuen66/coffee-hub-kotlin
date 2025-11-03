package com.coffeehub.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.coffeehub.data.repository.OrderRepository
import com.coffeehub.domain.model.Order
import com.coffeehub.domain.model.OrderStatus
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class OrderUiState {
    object Loading : OrderUiState()
    data class Success(val orders: List<Order>) : OrderUiState()
    data class Error(val message: String) : OrderUiState()
}

@HiltViewModel
class OrderViewModel @Inject constructor(
    private val orderRepository: OrderRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<OrderUiState>(OrderUiState.Loading)
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    private val _pendingOrders = MutableStateFlow<List<Order>>(emptyList())
    val pendingOrders: StateFlow<List<Order>> = _pendingOrders.asStateFlow()

    fun loadOrdersByCustomer(customerId: String) {
        viewModelScope.launch {
            try {
                _uiState.value = OrderUiState.Loading
                orderRepository.getOrdersByCustomer(customerId)
                    .collect { orders ->
                        _uiState.value = OrderUiState.Success(orders)
                    }
            } catch (e: Exception) {
                _uiState.value = OrderUiState.Error(e.message ?: "Failed to load orders")
            }
        }
    }

    fun loadAllOrders() {
        viewModelScope.launch {
            try {
                _uiState.value = OrderUiState.Loading
                orderRepository.getAllOrders()
                    .collect { orders ->
                        _uiState.value = OrderUiState.Success(orders)
                    }
            } catch (e: Exception) {
                _uiState.value = OrderUiState.Error(e.message ?: "Failed to load orders")
            }
        }
    }

    fun loadPendingOrders() {
        viewModelScope.launch {
            orderRepository.getPendingOrders()
                .collect { orders ->
                    _pendingOrders.value = orders
                }
        }
    }

    fun createOrder(order: Order, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            orderRepository.createOrder(order)
                .onSuccess { orderId ->
                    onResult(true, "Order placed successfully! Order ID: $orderId")
                }
                .onFailure {
                    onResult(false, it.message ?: "Failed to place order")
                }
        }
    }

    fun updateOrderStatus(orderId: String, status: OrderStatus, onResult: (Boolean, String) -> Unit) {
        viewModelScope.launch {
            orderRepository.updateOrderStatus(orderId, status)
                .onSuccess { onResult(true, "Order status updated") }
                .onFailure { onResult(false, it.message ?: "Failed to update order") }
        }
    }
}
