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
 * Enhanced MenuViewModel with proper state management.
 * 
 * Features:
 * - StateFlow for reactive UI updates
 * - Backend health monitoring
 * - Automatic retry on failure
 * - Loading states
 * - Error handling
 */
class MenuViewModel : ViewModel() {
    // Primary state: menu products
    private val _uiState = MutableStateFlow<MenuUiState>(MenuUiState.Loading)
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    // Backend health state
    private val _isBackendOnline = MutableStateFlow(true)
    val isBackendOnline: StateFlow<Boolean> = _isBackendOnline.asStateFlow()

    // Search query state
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Selected category state
    private val _selectedCategory = MutableStateFlow("All")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    init {
        getProducts()
        startHealthCheck()
    }

    /**
     * Monitor backend health status.
     * Checks every 30 seconds.
     */
    private fun startHealthCheck() {
        viewModelScope.launch {
            while (true) {
                try {
                    RetrofitInstance.api.getBackendHealth()
                    _isBackendOnline.value = true
                } catch (e: Exception) {
                    _isBackendOnline.value = false
                }
                kotlinx.coroutines.delay(30000) // Check every 30 seconds
            }
        }
    }

    /**
     * Fetch products from backend.
     * Automatically updates UI through StateFlow.
     */
    fun getProducts() {
        viewModelScope.launch {
            _uiState.value = MenuUiState.Loading
            try {
                val response = RetrofitInstance.api.getProducts()
                if (response.success && response.data != null) {
                    _uiState.value = MenuUiState.Success(response.data)
                } else {
                    _uiState.value = MenuUiState.Error(response.message)
                }
            } catch (e: Exception) {
                _uiState.value = MenuUiState.Error(
                    e.message ?: "Failed to load products. Please check your connection."
                )
            }
        }
    }

    /**
     * Update search query.
     */
    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Update selected category.
     */
    fun updateSelectedCategory(category: String) {
        _selectedCategory.value = category
    }

    /**
     * Refresh products (pull to refresh).
     */
    fun refresh() {
        getProducts()
    }
}
