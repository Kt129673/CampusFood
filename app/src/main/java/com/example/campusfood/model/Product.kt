package com.example.campusfood.model

import java.math.BigDecimal

data class Product(
    val id: Long? = null,
    val name: String,
    val description: String?,
    val price: Double,
    val category: String,
    val imageUrl: String?,
    val available: Boolean = true
)
