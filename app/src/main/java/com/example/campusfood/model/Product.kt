package com.example.campusfood.model

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class Product(
    val id: Long? = null,
    val name: String,
    val description: String?,
    val price: Double,
    val category: String,
    val imageUrl: String?,
    val available: Boolean = true,
    val stock: Int? = null
)
