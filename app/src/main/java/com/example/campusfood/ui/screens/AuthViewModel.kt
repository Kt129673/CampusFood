package com.example.campusfood.ui.screens

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.campusfood.data.UserSessionManager
import com.example.campusfood.model.GoogleLoginRequest
import com.example.campusfood.model.LoginRequest
import com.example.campusfood.model.OtpRequest
import com.example.campusfood.model.OtpVerificationRequest
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
    data object OtpSent : AuthUiState
    data class Success(val user: User) : AuthUiState
    data class Error(val message: String) : AuthUiState
}

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = UserSessionManager(application)

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
     * Uses the new backend /api/auth/google endpoint.
     */
    fun loginWithGoogle(name: String, email: String, mobile: String? = null) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            try {
                val response = RetrofitInstance.api.googleLogin(
                    GoogleLoginRequest(email = email, name = name, mobile = mobile)
                )
                if (response.success && response.data != null) {
                    sessionManager.saveSession(response.data)
                    _authState.value = AuthUiState.Success(response.data)
                } else {
                    _authState.value = AuthUiState.Error(response.message)
                }
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.message ?: "Google sign-in failed")
            }
        }
    }

    /**
     * Update user's mobile number after Google login
     */
    fun updateMobileNumber(mobile: String) {
        viewModelScope.launch {
            try {
                val user = currentUser.value
                if (user != null) {
                    // Update the user with new mobile number
                    val updatedUser = user.copy(mobile = mobile)
                    sessionManager.saveSession(updatedUser)
                    _authState.value = AuthUiState.Success(updatedUser)
                }
            } catch (e: Exception) {
                // Silent fail - mobile number is optional
            }
        }
    }

    /**
     * Send OTP to mobile
     */
    fun sendOtp(mobile: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            try {
                val response = RetrofitInstance.api.sendOtp(OtpRequest(mobile))
                if (response.success) {
                    _authState.value = AuthUiState.OtpSent
                } else {
                    _authState.value = AuthUiState.Error(response.message)
                }
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.message ?: "Failed to send OTP")
            }
        }
    }

    /**
     * Verify OTP and login
     */
    fun verifyOtp(mobile: String, otp: String) {
        viewModelScope.launch {
            _authState.value = AuthUiState.Loading
            try {
                val response = RetrofitInstance.api.verifyOtp(OtpVerificationRequest(mobile, otp))
                if (response.success && response.data != null) {
                    sessionManager.saveSession(response.data)
                    _authState.value = AuthUiState.Success(response.data)
                } else {
                    _authState.value = AuthUiState.Error(response.message)
                }
            } catch (e: Exception) {
                _authState.value = AuthUiState.Error(e.message ?: "OTP verification failed")
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

    fun setError(message: String) {
        _authState.value = AuthUiState.Error(message)
    }
}
