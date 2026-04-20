package com.example.campusfood.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class CartItem(
    val id: Long? = null,
    val productId: Long,
    val productName: String,
    val quantity: Int,
    val price: Double
)
