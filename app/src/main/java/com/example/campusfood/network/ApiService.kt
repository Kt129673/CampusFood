package com.example.campusfood.network

import com.example.campusfood.model.Product
import retrofit2.http.GET

interface ApiService {
    @GET("products")
    suspend fun getProducts(): List<Product>
}
