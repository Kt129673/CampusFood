package com.example.campusfood.network

import com.example.campusfood.model.ApiResponse
import com.example.campusfood.model.OrderRequest
import com.example.campusfood.model.OrderResponse
import com.example.campusfood.model.Product
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    // ========================
    // Products
    // ========================
    @GET("products")
    suspend fun getProducts(): ApiResponse<List<Product>>

    @GET("products/{id}")
    suspend fun getProductById(@Path("id") id: Long): ApiResponse<Product>

    @GET("products/category/{category}")
    suspend fun getProductsByCategory(@Path("category") category: String): ApiResponse<List<Product>>

    @GET("products/search")
    suspend fun searchProducts(@Query("query") query: String): ApiResponse<List<Product>>

    // ========================
    // Orders
    // ========================
    @POST("orders")
    suspend fun placeOrder(@Body order: OrderRequest): ApiResponse<OrderResponse>

    @GET("orders/user/{userId}")
    suspend fun getOrdersByUser(@Path("userId") userId: Long): ApiResponse<List<OrderResponse>>

    @GET("orders/{orderId}")
    suspend fun getOrderById(@Path("orderId") orderId: Long): ApiResponse<OrderResponse>

    @PUT("orders/{orderId}/cancel")
    suspend fun cancelOrder(@Path("orderId") orderId: Long): ApiResponse<OrderResponse>
}
