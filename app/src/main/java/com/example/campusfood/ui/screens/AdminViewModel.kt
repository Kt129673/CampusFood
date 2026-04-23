package com.example.campusfood.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusfood.model.InventoryUpdateRequest
import com.example.campusfood.model.OrderResponse
import com.example.campusfood.model.Product
import com.example.campusfood.model.ProductRequest
import com.example.campusfood.model.StatusUpdateRequest
import com.example.campusfood.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface AdminOrdersState {
    data object Loading : AdminOrdersState
    data class Success(val orders: List<OrderResponse>) : AdminOrdersState
    data class Error(val message: String) : AdminOrdersState
}

sealed interface AdminProductsState {
    data object Loading : AdminProductsState
    data class Success(val products: List<Product>) : AdminProductsState
    data class Error(val message: String) : AdminProductsState
}

sealed interface AdminProductActionState {
    data object Idle : AdminProductActionState
    data object Loading : AdminProductActionState
    data class Success(val message: String) : AdminProductActionState
    data class Error(val message: String) : AdminProductActionState
}

class AdminViewModel : ViewModel() {
    private val _ordersState = MutableStateFlow<AdminOrdersState>(AdminOrdersState.Loading)
    val ordersState: StateFlow<AdminOrdersState> = _ordersState.asStateFlow()

    private val _statusUpdateLoading = MutableStateFlow<Long?>(null)
    val statusUpdateLoading: StateFlow<Long?> = _statusUpdateLoading.asStateFlow()

    private val _productsState = MutableStateFlow<AdminProductsState>(AdminProductsState.Loading)
    val productsState: StateFlow<AdminProductsState> = _productsState.asStateFlow()

    private val _productActionState = MutableStateFlow<AdminProductActionState>(AdminProductActionState.Idle)
    val productActionState: StateFlow<AdminProductActionState> = _productActionState.asStateFlow()

    private val _selectedProduct = MutableStateFlow<Product?>(null)
    val selectedProduct: StateFlow<Product?> = _selectedProduct.asStateFlow()

    init {
        loadAllOrders()
        loadAllProducts()
    }

    // ========================
    // Order Management
    // ========================

    fun loadAllOrders() {
        viewModelScope.launch {
            _ordersState.value = AdminOrdersState.Loading
            try {
                val response = RetrofitInstance.api.adminGetAllOrders(page = 0, size = 100)
                if (response.success && response.data != null) {
                    _ordersState.value = AdminOrdersState.Success(
                        response.data.content.sortedByDescending { it.id }
                    )
                } else {
                    _ordersState.value = AdminOrdersState.Error(response.message)
                }
            } catch (e: Exception) {
                _ordersState.value = AdminOrdersState.Error(e.message ?: "Failed to load orders")
            }
        }
    }

    fun updateOrderStatus(orderId: Long, newStatus: String) {
        viewModelScope.launch {
            _statusUpdateLoading.value = orderId
            try {
                val response = RetrofitInstance.api.adminUpdateOrderStatus(
                    orderId = orderId,
                    request = StatusUpdateRequest(status = newStatus)
                )
                if (response.success) {
                    loadAllOrders()
                } else {
                    // Refresh to show current state even on failure
                    loadAllOrders()
                }
            } catch (_: Exception) {
                // Refresh to get current state so UI isn't stuck
                loadAllOrders()
            } finally {
                _statusUpdateLoading.value = null
            }
        }
    }

    // ========================
    // Product Management
    // ========================

    fun loadAllProducts() {
        viewModelScope.launch {
            _productsState.value = AdminProductsState.Loading
            try {
                val response = RetrofitInstance.api.getProducts()
                if (response.success && response.data != null) {
                    _productsState.value = AdminProductsState.Success(response.data)
                } else {
                    _productsState.value = AdminProductsState.Error(response.message)
                }
            } catch (e: Exception) {
                _productsState.value = AdminProductsState.Error(e.message ?: "Failed to load products")
            }
        }
    }

    fun loadProductById(productId: Long) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.getProductById(productId)
                if (response.success && response.data != null) {
                    _selectedProduct.value = response.data
                }
            } catch (_: Exception) {
                // Silent fail — screen will show empty form
            }
        }
    }

    fun createProduct(request: ProductRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _productActionState.value = AdminProductActionState.Loading
            try {
                val response = RetrofitInstance.api.adminCreateProduct(request)
                if (response.success) {
                    _productActionState.value = AdminProductActionState.Success("Product created successfully")
                    loadAllProducts()
                    onSuccess()
                } else {
                    _productActionState.value = AdminProductActionState.Error(response.message)
                }
            } catch (e: Exception) {
                _productActionState.value = AdminProductActionState.Error(e.message ?: "Failed to create product")
            }
        }
    }

    fun updateProduct(productId: Long, request: ProductRequest, onSuccess: () -> Unit) {
        viewModelScope.launch {
            _productActionState.value = AdminProductActionState.Loading
            try {
                val response = RetrofitInstance.api.adminUpdateProduct(productId, request)
                if (response.success) {
                    _productActionState.value = AdminProductActionState.Success("Product updated successfully")
                    loadAllProducts()
                    onSuccess()
                } else {
                    _productActionState.value = AdminProductActionState.Error(response.message)
                }
            } catch (e: Exception) {
                _productActionState.value = AdminProductActionState.Error(e.message ?: "Failed to update product")
            }
        }
    }

    fun deleteProduct(productId: Long) {
        viewModelScope.launch {
            _productActionState.value = AdminProductActionState.Loading
            try {
                val response = RetrofitInstance.api.adminDeleteProduct(productId)
                if (response.success) {
                    _productActionState.value = AdminProductActionState.Success("Product deleted")
                    loadAllProducts()
                } else {
                    _productActionState.value = AdminProductActionState.Error(response.message)
                }
            } catch (e: Exception) {
                _productActionState.value = AdminProductActionState.Error(e.message ?: "Failed to delete product")
            }
        }
    }

    fun updateInventory(productId: Long, quantity: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitInstance.api.adminUpdateInventory(
                    productId, InventoryUpdateRequest(quantity)
                )
                if (response.success) {
                    loadAllProducts()
                }
            } catch (_: Exception) {
                // Silent — product list refresh will show current state
            }
        }
    }

    fun clearProductAction() {
        _productActionState.value = AdminProductActionState.Idle
    }

    fun clearSelectedProduct() {
        _selectedProduct.value = null
    }
}
