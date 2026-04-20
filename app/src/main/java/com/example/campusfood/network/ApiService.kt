package com.example.campusfood.network

import com.example.campusfood.model.CartItem
import com.example.campusfood.model.Product
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("products")
    suspend fun getProducts(): List<Product>

    @GET("cart")
    suspend fun getCartItems(): List<CartItem>

    @POST("cart")
    suspend fun addToCart(@Body cartItem: CartItem): CartItem

    @DELETE("cart/{id}")
    suspend fun removeFromCart(@Path("id") id: Long)
}
