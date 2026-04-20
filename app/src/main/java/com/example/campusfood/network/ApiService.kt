package com.example.campusfood.network

import com.example.campusfood.model.ApiResponse
import com.example.campusfood.model.CartItem
import com.example.campusfood.model.Order
import com.example.campusfood.model.Product
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    @GET("products")
    suspend fun getProducts(): ApiResponse<List<Product>>

    @GET("cart")
    suspend fun getCartItems(): ApiResponse<List<CartItem>>

    @POST("cart")
    suspend fun addToCart(@Body cartItem: CartItem): ApiResponse<CartItem>

    @DELETE("cart/{id}")
    suspend fun removeFromCart(@Path("id") id: Long): ApiResponse<Unit>

    @GET("orders")
    suspend fun getOrders(): ApiResponse<List<Order>>

    @POST("orders")
    suspend fun placeOrder(@Body order: Order): ApiResponse<Order>
}
