package com.example.campusfood.model

data class CartItem(
    val id: Long? = null,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val price: Double
)
