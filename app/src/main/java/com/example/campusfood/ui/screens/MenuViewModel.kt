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

    init {
        getProducts()
    }

    fun getProducts() {
        viewModelScope.launch {
            _uiState.value = MenuUiState.Loading
            try {
                val products = RetrofitInstance.api.getProducts()
                _uiState.value = MenuUiState.Success(products)
            } catch (e: Exception) {
                _uiState.value = MenuUiState.Error(e.message ?: "Unknown error occurred")
            }
        }
    }
}
