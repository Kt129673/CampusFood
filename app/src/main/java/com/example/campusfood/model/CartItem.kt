package com.example.campusfood.model

/**
 * Local cart item model — cart is managed in-memory on the device.
 * No backend cart API exists, so this is purely local state.
 */
data class CartItem(
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val price: Double,
    val imageUrl: String? = null,
    val category: String? = null
)
