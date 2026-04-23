package com.example.campusfood.model

import com.squareup.moshi.JsonClass

/**
 * Spring Data Page response wrapper.
 * Matches the structure returned by Pageable endpoints.
 */
@JsonClass(generateAdapter = true)
data class PaginatedResponse<T>(
    val content: List<T>,
    val totalElements: Long?,
    val totalPages: Int?,
    val number: Int?,
    val size: Int?,
    val first: Boolean?,
    val last: Boolean?
)

/**
 * Request model for updating order status.
 * Matches backend OrderStatusUpdateRequest.
 */
@JsonClass(generateAdapter = true)
data class StatusUpdateRequest(
    val status: String
)

/**
 * Request model for creating/updating products.
 * Matches backend ProductRequest.
 */
@JsonClass(generateAdapter = true)
data class ProductRequest(
    val name: String,
    val description: String?,
    val price: Double,
    val category: String,
    val imageUrl: String?,
    val available: Boolean = true,
    val initialStock: Int? = 0
)

/**
 * Request model for updating inventory quantity.
 * Matches backend InventoryUpdateRequest.
 */
@JsonClass(generateAdapter = true)
data class InventoryUpdateRequest(
    val quantity: Int
)
