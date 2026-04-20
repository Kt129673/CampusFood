package com.example.campusfood.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusfood.model.Order
import com.example.campusfood.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface OrderUiState {
    data object Loading : OrderUiState
    data class Success(val orders: List<Order>) : OrderUiState
    data class Error(val message: String) : OrderUiState
}

class OrderViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<OrderUiState>(OrderUiState.Loading)
    val uiState: StateFlow<OrderUiState> = _uiState.asStateFlow()

    init {
        getOrders()
    }

    fun getOrders() {
        viewModelScope.launch {
            _uiState.value = OrderUiState.Loading
            try {
                val response = RetrofitInstance.api.getOrders()
                if (response.success && response.data != null) {
                    _uiState.value = OrderUiState.Success(response.data)
                } else {
                    _uiState.value = OrderUiState.Error(response.message)
                }
            } catch (e: Exception) {
                _uiState.value = OrderUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun placeOrder(order: Order, onSuccess: () -> Unit) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.placeOrder(order)
                getOrders()
                onSuccess()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
