package com.example.campusfood.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Order(
    val id: Long? = null,
    val userId: Long,
    val items: List<OrderItem>,
    val deliveryAddress: String,
    val totalAmount: Double? = null,
    val status: String? = null,
    val orderTime: String? = null
)

@JsonClass(generateAdapter = true)
data class OrderItem(
    val productId: Long,
    val quantity: Int
)
