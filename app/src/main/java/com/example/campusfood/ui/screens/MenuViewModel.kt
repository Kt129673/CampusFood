package com.example.campusfood.ui.screens

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusfood.model.Product
import com.example.campusfood.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface MenuUiState {
    data object Loading : MenuUiState
    data class Success(val products: List<Product>) : MenuUiState
    data class Error(val message: String) : MenuUiState
}

/**
 * ViewModel for the student-facing menu screen.
 *
 * Responsibilities:
 * - Fetching and caching product list from backend
 * - Backend health monitoring for online/offline indicator
 * - Pull-to-refresh without full loading skeleton
 * - Search and category filter state management
 */
class MenuViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<MenuUiState>(MenuUiState.Loading)
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    private val _isBackendOnline = MutableStateFlow(true)
    val isBackendOnline: StateFlow<Boolean> = _isBackendOnline.asStateFlow()

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    init {
        getProducts()
        startHealthCheck()
    }

    /**
     * Monitor backend health status.
     * Checks every 30 seconds. Properly cancels when ViewModel is destroyed.
     */
    private fun startHealthCheck() {
        viewModelScope.launch {
            try {
                while (true) {
                    try {
                        RetrofitInstance.api.getBackendHealth()
                        _isBackendOnline.value = true
                    } catch (_: Exception) {
                        _isBackendOnline.value = false
                    }
                    kotlinx.coroutines.delay(30_000L)
                }
            } catch (_: kotlinx.coroutines.CancellationException) {
                // Coroutine cancelled - cleanup properly
            }
        }
    }

    /**
     * Fetch products from backend with full loading skeleton.
     */
    fun getProducts() {
        viewModelScope.launch {
            _uiState.value = MenuUiState.Loading
            try {
                val response = RetrofitInstance.api.getProducts()
                if (response.success) {
                    val products = response.data ?: emptyList()
                    _uiState.value = MenuUiState.Success(
                        products.filter { it.available }
                    )
                } else {
                    _uiState.value = MenuUiState.Error(response.message)
                }
            } catch (e: Exception) {
                _uiState.value = MenuUiState.Error(
                    e.message ?: "Failed to load menu. Please check your connection."
                )
            }
        }
    }

    /**
     * Silent refresh for pull-to-refresh. Keeps existing data visible.
     */
    fun refreshProducts() {
        viewModelScope.launch {
            _isRefreshing.value = true
            try {
                val response = RetrofitInstance.api.getProducts()
                if (response.success) {
                    val products = response.data ?: emptyList()
                    _uiState.value = MenuUiState.Success(
                        products.filter { it.available }
                    )
                }
            } catch (_: Exception) {
                // Silent fail – keep existing data
            } finally {
                _isRefreshing.value = false
            }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun updateSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    /**
     * Refresh products (pull to refresh).
     */
    fun refresh() {
        refreshProducts()
    }
}
