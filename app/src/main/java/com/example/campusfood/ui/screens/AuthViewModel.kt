package com.example.campusfood.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusfood.data.UserSessionManager
import com.example.campusfood.model.LoginRequest
import com.example.campusfood.model.RegisterRequest
import com.example.campusfood.model.User
import com.example.campusfood.network.RetrofitInstance
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed interface AuthUiState {
    data object Idle : AuthUiState
    data object Loading : AuthUiState
    data class Success(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    val sessionManager = UserSessionManager(application)

    private val _authState = MutableStateFlow<AuthUiState>(AuthUiState.Idle)
    val authState: StateFlow<AuthUiState> = _authState.asStateFlow()

    val isLoggedIn = sessionManager.isLoggedIn
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), false)

    val currentUser = sessionManager.currentUser
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    /**
     * Admin login with mobile + password.
     * Verifies that the returned user has ADMIN role.
     */
    fun loginAdmin(mobile: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            try {
                val response = RetrofitInstance.api.login(
                    LoginRequest(mobile = mobile, password = password)
                )
                if (response.success && response.data != null) {
                    val user = response.data
                    if (user.role == "ADMIN") {
                        sessionManager.saveSession(user)
                        _authState.value = AuthUiState.Success(user)
                    } else {
                        _authState.value = AuthUiState.Error("Access denied. Admin credentials required.")
                    }
                } else {
                    _authState.value = AuthUiState.Error(response.message)
                }
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.message ?: "Login failed. Check your connection.")
            }
        }
    }

    /**
     * Regular user login (for demo accounts).
     */
    fun loginUser(mobile: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            try {
                val response = RetrofitInstance.api.login(
                    LoginRequest(mobile = mobile, password = password)
                )
                if (response.success && response.data != null) {
                    sessionManager.saveSession(response.data)
                    _authState.value = AuthUiState.Success(response.data)
                } else {
                    _authState.value = AuthUiState.Error(response.message)
                }
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.message ?: "Login failed. Check your connection.")
            }
        }
    }

    /**
     * Google Sign-In: auto-register or login the user.
     * Uses Google account email to find or create user on backend.
     */
    fun loginWithGoogle(name: String, email: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            try {
                // Generate a stable mobile number from email hash (backend requires mobile)
                val hash = email.hashCode().toLong()
                val absHash = if (hash < 0) -hash else hash
                val generatedMobile = "9" + absHash.toString().takeLast(9).padStart(9, '0')

                // Try to register first (will fail if mobile already exists)
                try {
                    val registerResponse = RetrofitInstance.api.register(
                        RegisterRequest(
                            name = name,
                            mobile = generatedMobile,
                            email = email,
                            password = email, // Use email as password for Google users
                            role = "CUSTOMER"
                        )
                    )
                    if (registerResponse.success && registerResponse.data != null) {
                        sessionManager.saveSession(registerResponse.data)
                        _authState.value = AuthUiState.Success(registerResponse.data)
                        return@launch
                    }
                } catch (_: Exception) {
                    // User likely already exists, proceed to login
                }

                // Login with existing credentials
                val loginResponse = RetrofitInstance.api.login(
                    LoginRequest(mobile = generatedMobile, password = email)
                )
                if (loginResponse.success && loginResponse.data != null) {
                    sessionManager.saveSession(loginResponse.data)
                    _authState.value = AuthUiState.Success(loginResponse.data)
                } else {
                    _authState.value = AuthUiState.Error(loginResponse.message)
                }
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.message ?: "Google sign-in failed")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            sessionManager.clearSession()
            _authState.value = AuthUiState.Idle
        }
    }

    fun resetError() {
        _authState.value = AuthUiState.Idle
    }
}
