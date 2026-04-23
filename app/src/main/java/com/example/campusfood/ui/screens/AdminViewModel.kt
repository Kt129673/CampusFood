package com.example.campusfood.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusfood.model.OrderResponse
import com.example.campusfood.model.StatusUpdateRequest
import com.example.campusfood.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AdminOrdersState {
    data object Loading : AdminOrdersState
    data class Success(val orders: List<OrderResponse>) : AdminOrdersState
    data class Error(val message: String) : AdminOrdersState
}

class AdminViewModel : ViewModel() {
    private val _ordersState = MutableStateFlow<AdminOrdersState>(AdminOrdersState.Loading)
    val ordersState: StateFlow<AdminOrdersState> = _ordersState.asStateFlow()

    private val _statusUpdateLoading = MutableStateFlow<Long?>(null)
    val statusUpdateLoading: StateFlow<Long?> = _statusUpdateLoading.asStateFlow()

    init {
        loadAllOrders()
    }

    fun loadAllOrders() {
        viewModelScope.launch {
            _ordersState.value = AdminOrdersState.Loading
            try {
                val response = RetrofitInstance.api.adminGetAllOrders(page = 0, size = 100)
                if (response.success && response.data != null) {
                    _ordersState.value = AdminOrdersState.Success(
                        response.data.content.sortedByDescending { it.id }
                    )
                } else {
                    _ordersState.value = AdminOrdersState.Error(response.message)
                }
            } catch (e: Exception) {
                _ordersState.value = AdminOrdersState.Error(e.message ?: "Failed to load orders")
            }
        }
    }

    fun updateOrderStatus(orderId: Long, newStatus: String) {
        viewModelScope.launch {
            _statusUpdateLoading.value = orderId
            try {
                val response = RetrofitInstance.api.adminUpdateOrderStatus(
                    orderId = orderId,
                    request = StatusUpdateRequest(status = newStatus)
                )
                if (response.success) {
                    loadAllOrders()
                } else {
                    // Refresh to show current state even on failure
                    loadAllOrders()
                }
            } catch (_: Exception) {
                // Refresh to get current state so UI isn't stuck
                loadAllOrders()
            } finally {
                _statusUpdateLoading.value = null
            }
        }
    }
}
