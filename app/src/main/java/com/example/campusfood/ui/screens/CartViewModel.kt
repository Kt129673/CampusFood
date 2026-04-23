package com.example.campusfood.ui.screens

import androidx.lifecycle.ViewModel
import com.example.campusfood.model.CartItem
import com.example.campusfood.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

sealed interface CartUiState {
    data object Loading : CartUiState
    data class Success(val items: List<CartItem>) : CartUiState
    data class Error(val message: String) : CartUiState
}

/**
 * Cart is managed locally in-memory.
 * No backend cart API exists — cart persists only during the app session.
 */
class CartViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<CartUiState>(CartUiState.Success(emptyList()))
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    private val cartItems = mutableListOf<CartItem>()

    fun addToCart(product: Product) {
        val existing = cartItems.find { it.productId == product.id }
        if (existing != null) {
            val index = cartItems.indexOf(existing)
            cartItems[index] = existing.copy(quantity = existing.quantity + 1)
        } else {
            cartItems.add(
                CartItem(
                    productId = product.id ?: 0,
                    productName = product.name,
                    quantity = 1,
                    price = product.price,
                    imageUrl = product.imageUrl,
                    category = product.category
                )
            )
        }
        _uiState.value = CartUiState.Success(cartItems.toList())
    }

    fun removeFromCart(productId: Long) {
        cartItems.removeAll { it.productId == productId }
        _uiState.value = CartUiState.Success(cartItems.toList())
    }

    fun incrementQuantity(productId: Long) {
        val index = cartItems.indexOfFirst { it.productId == productId }
        if (index >= 0) {
            val item = cartItems[index]
            cartItems[index] = item.copy(quantity = item.quantity + 1)
            _uiState.value = CartUiState.Success(cartItems.toList())
        }
    }

    fun decrementQuantity(productId: Long) {
        val index = cartItems.indexOfFirst { it.productId == productId }
        if (index >= 0) {
            val item = cartItems[index]
            if (item.quantity > 1) {
                cartItems[index] = item.copy(quantity = item.quantity - 1)
            } else {
                cartItems.removeAt(index)
            }
            _uiState.value = CartUiState.Success(cartItems.toList())
        }
    }

    fun clearCart() {
        cartItems.clear()
        _uiState.value = CartUiState.Success(emptyList())
    }

    val itemCount: Int get() = cartItems.sumOf { it.quantity }

    val totalAmount: Double get() = cartItems.sumOf { it.price * it.quantity }
}
