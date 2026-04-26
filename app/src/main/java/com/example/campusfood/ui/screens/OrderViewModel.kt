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

/**
 * ViewModel for customer order management.
 *
 * Responsibilities:
 * - Fetching user orders sorted by recency
 * - Placing new orders with optimistic UI feedback
 * - Cancelling orders with proper error propagation
 * - Pull-to-refresh support via isRefreshing state
 * - Snackbar-level event feedback via one-shot event flow
 */
class OrderViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<OrderUiState>(OrderUiState.Loading)
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    private val _isPlacingOrder = MutableStateFlow(false)
    val isPlacingOrder: StateFlow<Boolean> = _isPlacingOrder.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    /** One-shot event for snackbar messages (cancel success/failure, etc.) */
    private val _snackbarEvent = MutableStateFlow<String?>(null)
    val snackbarEvent: StateFlow<String?> = _snackbarEvent.asStateFlow()

    private var userId: Long = 0L

    /**
     * Set the current user's ID for fetching orders.
     * Called from MainScreen when auth state changes.
     */
    fun setUserId(id: Long) {
        if (userId != id) {
            userId = id
            getOrders()
        }
    }

    fun getOrders() {
        if (userId == 0L) {
            // Don't show loading state if user ID not set yet
            _uiState.value = OrderUiState.Success(emptyList())
            return
        }
        viewModelScope.launch {
            _uiState.value = OrderUiState.Loading
            try {
                val response = RetrofitInstance.api.getOrdersByUser(userId)
                if (response.success) {
                    val orders = response.data ?: emptyList()
                    // Sort by most recent first
                    _uiState.value = OrderUiState.Success(
                        orders.sortedByDescending { it.id }
                    )
                } else {
                    _uiState.value = OrderUiState.Error(response.message)
                }
            } catch (e: Exception) {
                _uiState.value = OrderUiState.Error(
                    e.message ?: "Failed to load orders. Check your connection."
                )
            }
        }
    }

    /**
     * Silent refresh without showing loading skeleton.
     * Used for pull-to-refresh.
     */
    fun refreshOrders() {
        if (userId == 0L) return
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val response = RetrofitInstance.api.getOrdersByUser(userId)
                if (response.success) {
                    val orders = response.data ?: emptyList()
                    _uiState.value = OrderUiState.Success(
                        orders.sortedByDescending { it.id }
                    )
                }
            } catch (_: Exception) {
                // Silent fail on refresh – keep existing data
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun placeOrder(orderRequest: OrderRequest, onComplete: (Boolean, Long?) -> Unit) {
        viewModelScope.launch {
            _isPlacingOrder.value = true
            try {
                val response = RetrofitInstance.api.placeOrder(orderRequest)
                if (response.success) {
                    getOrders()
                    onComplete(true, response.data?.id)
                } else {
                    _snackbarEvent.value = "Order failed: ${response.message}"
                    onComplete(false, null)
                }
            } catch (e: Exception) {
                _snackbarEvent.value = "Order failed: ${e.message ?: "Network error"}"
                onComplete(false, null)
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
                    _snackbarEvent.value = "Order #$orderId cancelled"
                    getOrders()
                } else {
                    _snackbarEvent.value = "Cannot cancel: ${response.message}"
                    getOrders() // Refresh to get latest state
                }
            } catch (e: Exception) {
                _snackbarEvent.value = "Cancel failed: ${e.message ?: "Network error"}"
                getOrders()
            }
        }
    }

    fun clearSnackbarEvent() {
        _snackbarEvent.value = null
    }
}
