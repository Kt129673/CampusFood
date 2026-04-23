package com.example.campusfood.network

import com.example.campusfood.model.ApiResponse
import com.example.campusfood.model.InventoryUpdateRequest
import com.example.campusfood.model.LoginRequest
import com.example.campusfood.model.OrderRequest
import com.example.campusfood.model.OrderResponse
import com.example.campusfood.model.PaginatedResponse
import com.example.campusfood.model.Product
import com.example.campusfood.model.ProductRequest
import com.example.campusfood.model.RegisterRequest
import com.example.campusfood.model.StatusUpdateRequest
import com.example.campusfood.model.User
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.Multipart
import retrofit2.http.Part
import okhttp3.MultipartBody
import com.example.campusfood.model.ImageUploadResponse

interface ApiService {

    // ========================
    // Authentication
    // ========================
    @POST("auth/register")
    suspend fun register(@Body request: RegisterRequest): ApiResponse<User>

    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): ApiResponse<User>

    @POST("auth/google")
    suspend fun googleLogin(@Body request: GoogleLoginRequest): ApiResponse<User>

    @GET("auth/user/{id}")
    suspend fun getUserById(@Path("id") id: Long): ApiResponse<User>

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
    // Orders (Customer)
    // ========================
    @POST("orders")
    suspend fun placeOrder(@Body order: OrderRequest): ApiResponse<OrderResponse>

    @GET("orders/user/{userId}")
    suspend fun getOrdersByUser(@Path("userId") userId: Long): ApiResponse<List<OrderResponse>>

    @GET("orders/{orderId}")
    suspend fun getOrderById(@Path("orderId") orderId: Long): ApiResponse<OrderResponse>

    @PUT("orders/{orderId}/cancel")
    suspend fun cancelOrder(@Path("orderId") orderId: Long): ApiResponse<OrderResponse>

    // ========================
    // Admin - Orders
    // ========================
    @GET("admin/orders")
    suspend fun adminGetAllOrders(
        @Query("page") page: Int = 0,
        @Query("size") size: Int = 50
    ): ApiResponse<PaginatedResponse<OrderResponse>>

    @PUT("admin/orders/{orderId}/status")
    suspend fun adminUpdateOrderStatus(
        @Path("orderId") orderId: Long,
        @Body request: StatusUpdateRequest
    ): ApiResponse<OrderResponse>

    // ========================
    // Admin - Products
    // ========================
    @POST("admin/products")
    suspend fun adminCreateProduct(@Body request: ProductRequest): ApiResponse<Product>

    @PUT("admin/products/{id}")
    suspend fun adminUpdateProduct(
        @Path("id") id: Long,
        @Body request: ProductRequest
    ): ApiResponse<Product>

    @DELETE("admin/products/{id}")
    suspend fun adminDeleteProduct(@Path("id") id: Long): ApiResponse<Any?>

    @Multipart
    @POST("images/upload/product")
    suspend fun uploadProductImage(
        @Part file: MultipartBody.Part
    ): ApiResponse<ImageUploadResponse>

    // ========================
    // Admin - Inventory
    // ========================
    @PUT("admin/inventory/{productId}")
    suspend fun adminUpdateInventory(
        @Path("productId") productId: Long,
        @Body request: InventoryUpdateRequest
    ): ApiResponse<Product>
}
