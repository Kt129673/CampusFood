package com.campusfood.controller;

import com.campusfood.dto.request.GoogleLoginRequest;
import com.campusfood.dto.request.LoginRequest;
import com.campusfood.dto.request.OtpRequest;
import com.campusfood.dto.request.OtpVerificationRequest;
import com.campusfood.dto.request.RegisterRequest;
import com.campusfood.dto.response.ApiResponse;
import com.campusfood.dto.response.UserResponse;
import com.campusfood.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = userService.register(request);
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success("Registration successful", user));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponse>> login(@Valid @RequestBody LoginRequest request) {
        UserResponse user = userService.login(request);
        return ResponseEntity.ok(ApiResponse.success("Login successful", user));
    }

    @GetMapping("/user/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable Long id) {
        UserResponse user = userService.getUserById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    @PostMapping("/google")
    public ResponseEntity<ApiResponse<UserResponse>> googleLogin(@RequestBody GoogleLoginRequest request) {
        UserResponse user = userService.googleLogin(request.getEmail(), request.getName());
        return ResponseEntity.ok(ApiResponse.success("Google login successful", user));
    }

    @PostMapping("/otp/send")
    public ResponseEntity<ApiResponse<String>> sendOtp(@RequestBody OtpRequest request) {
        // In a real app, this would send an SMS. For now, we'll just return success.
        return ResponseEntity.ok(ApiResponse.success("OTP sent to " + request.getMobile(), "123456"));
    }

    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<UserResponse>> verifyOtp(@RequestBody OtpVerificationRequest request) {
        // For demo purposes, we accept '123456' as the valid OTP.
        if ("123456".equals(request.getOtp())) {
            UserResponse user = userService.loginWithOtp(request.getMobile());
            return ResponseEntity.ok(ApiResponse.success("Login successful", user));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Invalid OTP"));
        }
    }
}
