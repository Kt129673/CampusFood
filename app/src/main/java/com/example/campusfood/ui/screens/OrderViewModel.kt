package com.example.campusfood.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusfood.model.OrderRequest
import com.example.campusfood.model.OrderResponse
import com.example.campusfood.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface OrderUiState {
    data object Loading : OrderUiState
    data class Success(val orders: List<OrderResponse>) : OrderUiState
    data class Error(val message: String) : OrderUiState
}

class OrderViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<OrderUiState>(OrderUiState.Loading)
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    private val _isPlacingOrder = MutableStateFlow(false)
    val isPlacingOrder: StateFlow<Boolean> = _isPlacingOrder.asStateFlow()

    // TODO: Replace with actual logged-in user ID from auth
    private val currentUserId: Long = 2L // Rahul Sharma from seed data

    init {
        getOrders()
    }

    fun getOrders() {
        viewModelScope.launch {
            _uiState.value = OrderUiState.Loading
            try {
                val response = RetrofitInstance.api.getOrdersByUser(currentUserId)
                if (response.success && response.data != null) {
                    _uiState.value = OrderUiState.Success(response.data)
                } else {
                    _uiState.value = OrderUiState.Error(response.message)
                }
            } catch (e: Exception) {
                _uiState.value = OrderUiState.Error(e.message ?: "Failed to load orders")
            }
        }
    }

    fun placeOrder(orderRequest: OrderRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _isPlacingOrder.value = true
            try {
                val response = RetrofitInstance.api.placeOrder(orderRequest)
                if (response.success) {
                    getOrders()
                    onSuccess()
                } else {
                    _uiState.value = OrderUiState.Error(response.message)
                }
            } catch (e: Exception) {
                _uiState.value = OrderUiState.Error(e.message ?: "Failed to place order")
            } finally {
                _isPlacingOrder.value = false
            }
        }
    }

    fun cancelOrder(orderId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.cancelOrder(orderId)
                if (response.success) {
                    getOrders() // Refresh
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
