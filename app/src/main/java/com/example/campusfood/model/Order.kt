package com.example.campusfood.model

import com.squareup.moshi.JsonClass

/**
 * Request model for placing an order.
 */
@JsonClass(generateAdapter = true)
data class OrderRequest(
    val userId: Long,
    val items: List<OrderItemRequest>,
    val deliveryAddress: String?,
    val notes: String? = null
)

@JsonClass(generateAdapter = true)
data class OrderItemRequest(
    val productId: Long,
    val quantity: Int
)

/**
 * Response model matching backend OrderResponse.
 */
@JsonClass(generateAdapter = true)
data class OrderResponse(
    val id: Long,
    val userId: Long,
    val userName: String?,
    val status: String,
    val totalAmount: Double,
    val deliveryAddress: String?,
    val notes: String?,
    val items: List<OrderItemResponse>?,
    val createdAt: String?,
    val updatedAt: String?
)

@JsonClass(generateAdapter = true)
data class OrderItemResponse(
    val id: Long?,
    val productId: Long,
    val productName: String?,
    val quantity: Int,
    val unitPrice: Double?,
    val totalPrice: Double?
)
