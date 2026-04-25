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

class MenuViewModel : ViewModel() {
    private val _uiState = MutableStateFlow<MenuUiState>(MenuUiState.Loading)
    val uiState: StateFlow<MenuUiState> = _uiState.asStateFlow()

    private val _isBackendOnline = MutableStateFlow(true)
    val isBackendOnline: StateFlow<Boolean> = _isBackendOnline.asStateFlow()

    init {
        getProducts()
        startHealthCheck()
    }

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
                _uiState.value = MenuUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}
