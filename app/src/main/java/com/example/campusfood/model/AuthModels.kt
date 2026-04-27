package com.example.campusfood.model

import com.squareup.moshi.JsonClass

/**
 * User model matching backend UserResponse.
 */
@JsonClass(generateAdapter = true)
data class User(
    val id: Long,
    val name: String,
    val mobile: String?,
    val email: String?,
    val role: String, // CUSTOMER, ADMIN, DELIVERY
    val active: Boolean = true,
    val createdAt: String? = null
)

/**
 * Login request matching backend LoginRequest.
 */
@JsonClass(generateAdapter = true)
data class LoginRequest(
    val mobile: String,
    val password: String
)

/**
 * Register request matching backend RegisterRequest.
 */
@JsonClass(generateAdapter = true)
data class RegisterRequest(
    val name: String,
    val mobile: String,
    val email: String?,
    val password: String,
    val role: String = "CUSTOMER"
)

/**
 * Google login request for backend GoogleLoginRequest.
 */
@JsonClass(generateAdapter = true)
data class GoogleLoginRequest(
    val email: String,
    val name: String,
    val mobile: String? = null
)

/**
 * Request to send OTP to mobile.
 */
@JsonClass(generateAdapter = true)
data class OtpRequest(
    val mobile: String
)

/**
 * Request to verify OTP.
 */
@JsonClass(generateAdapter = true)
data class OtpVerificationRequest(
    val mobile: String,
    val otp: String
)
