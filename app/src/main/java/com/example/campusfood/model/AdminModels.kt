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
