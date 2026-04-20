package com.example.campusfood.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusfood.model.CartItem
import com.example.campusfood.model.Product
import com.example.campusfood.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface CartUiState {
    data object Loading : CartUiState
    data class Success(val items: List<CartItem>) : CartUiState
    data class Error(val message: String) : CartUiState
}

class CartViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<CartUiState>(CartUiState.Loading)
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    init {
        getCartItems()
    }

    fun getCartItems() {
        viewModelScope.launch {
            _uiState.value = CartUiState.Loading
            try {
                val items = RetrofitInstance.api.getCartItems()
                _uiState.value = CartUiState.Success(items)
            } catch (e: Exception) {
                _uiState.value = CartUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun addToCart(product: Product) {
        viewModelScope.launch {
            try {
                val cartItem = CartItem(
                    productId = product.id ?: 0,
                    productName = product.name,
                    quantity = 1,
                    price = product.price
                )
                RetrofitInstance.api.addToCart(cartItem)
                getCartItems() // Refresh cart
            } catch (e: Exception) {
                // Handle error
            }
        }
    }

    fun removeFromCart(itemId: Long) {
        viewModelScope.launch {
            try {
                RetrofitInstance.api.removeFromCart(itemId)
                getCartItems()
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
}
