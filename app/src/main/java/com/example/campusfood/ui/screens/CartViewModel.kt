package com.example.campusfood.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusfood.model.CartItem
import com.example.campusfood.model.Product
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface CartUiState {
    data object Loading : CartUiState
    data class Success(val items: List<CartItem>) : CartUiState
    data class Error(val message: String) : CartUiState
}

/**
 * Enhanced CartViewModel with proper state management.
 * 
 * Features:
 * - StateFlow for reactive UI updates
 * - Derived StateFlows for cart count and total amount
 * - Thread-safe cart operations
 * - Automatic UI updates when cart changes
 * - In-memory cart (no backend persistence)
 */
class CartViewModel : ViewModel() {
    // Primary state: cart items
    private val _uiState = MutableStateFlow<CartUiState>(CartUiState.Success(emptyList()))
    val uiState: StateFlow<CartUiState> = _uiState.asStateFlow()

    // Derived state: cart item count (automatically updates UI)
    val cartItemCount: StateFlow<Int> = uiState.map { state ->
        when (state) {
            is CartUiState.Success -> {
                synchronized(cartItems) {
                    state.items.sumOf { it.quantity }
                }
            }
            else -> 0
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0
    )

    // Derived state: total amount (automatically updates UI)
    val totalAmount: StateFlow<Double> = uiState.map { state ->
        when (state) {
            is CartUiState.Success -> {
                synchronized(cartItems) {
                    state.items.sumOf { it.price * it.quantity }
                }
            }
            else -> 0.0
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 0.0
    )

    // Thread-safe cart using synchronizedList
    private val cartItems = java.util.Collections.synchronizedList(mutableListOf<CartItem>())

    /**
     * Add a product to cart or increment quantity if already exists.
     * Automatically updates UI through StateFlow.
     */
    fun addToCart(product: Product) {
        synchronized(cartItems) {
            val existing = cartItems.find { it.productId == product.id }
            if (existing != null) {
                // Product already in cart - increment quantity
                val index = cartItems.indexOf(existing)
                cartItems[index] = existing.copy(quantity = existing.quantity + 1)
            } else {
                // New product - add to cart
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
            // Update state - triggers UI recomposition
            emitCartState()
        }
    }

    /**
     * Remove a product from cart completely.
     */
    fun removeFromCart(productId: Long) {
        synchronized(cartItems) {
            cartItems.removeAll { it.productId == productId }
            emitCartState()
        }
    }

    /**
     * Restore a removed item back to cart.
     */
    fun restoreCartItem(item: CartItem) {
        synchronized(cartItems) {
            if (!cartItems.any { it.productId == item.productId }) {
                cartItems.add(item)
                emitCartState()
            }
        }
    }

    /**
     * Increment quantity of a product in cart.
     */
    fun incrementQuantity(productId: Long) {
        synchronized(cartItems) {
            val index = cartItems.indexOfFirst { it.productId == productId }
            if (index >= 0) {
                val item = cartItems[index]
                cartItems[index] = item.copy(quantity = item.quantity + 1)
                emitCartState()
            }
        }
    }

    /**
     * Decrement quantity of a product in cart.
     * Removes item if quantity becomes 0.
     */
    fun decrementQuantity(productId: Long) {
        synchronized(cartItems) {
            val index = cartItems.indexOfFirst { it.productId == productId }
            if (index >= 0) {
                val item = cartItems[index]
                if (item.quantity > 1) {
                    cartItems[index] = item.copy(quantity = item.quantity - 1)
                } else {
                    // Remove item if quantity becomes 0
                    cartItems.removeAt(index)
                }
                emitCartState()
            }
        }
    }

    /**
     * Update quantity directly for a product.
     */
    fun updateQuantity(productId: Long, newQuantity: Int) {
        synchronized(cartItems) {
            if (newQuantity <= 0) {
                removeFromCart(productId)
                return
            }
            val index = cartItems.indexOfFirst { it.productId == productId }
            if (index >= 0) {
                val item = cartItems[index]
                cartItems[index] = item.copy(quantity = newQuantity)
                emitCartState()
            }
        }
    }

    /**
     * Clear all items from cart.
     */
    fun clearCart() {
        synchronized(cartItems) {
            cartItems.clear()
            emitCartState()
        }
    }

    /**
     * Check if a product is in the cart.
     */
    fun isInCart(productId: Long): Boolean {
        return synchronized(cartItems) {
            cartItems.any { it.productId == productId }
        }
    }

    /**
     * Get quantity of a specific product in cart.
     */
    fun getProductQuantity(productId: Long): Int {
        return synchronized(cartItems) {
            cartItems.find { it.productId == productId }?.quantity ?: 0
        }
    }

    /**
     * Emit current cart state to trigger UI updates.
     */
    private fun emitCartState() {
        _uiState.value = CartUiState.Success(cartItems.toList())
    }
}
