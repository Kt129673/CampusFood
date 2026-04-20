package com.campusfood.service;

import com.campusfood.dto.request.LoginRequest;
import com.campusfood.dto.request.RegisterRequest;
import com.campusfood.dto.response.UserResponse;
import com.campusfood.entity.User;
import com.campusfood.exception.InvalidOperationException;
import com.campusfood.exception.ResourceNotFoundException;
import com.campusfood.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByMobile(request.getMobile())) {
            throw new InvalidOperationException("Mobile number already registered: " + request.getMobile());
        }

        User user = User.builder()
                .name(request.getName())
                .mobile(request.getMobile())
                .email(request.getEmail())
                .passwordHash(hashPassword(request.getPassword()))
                .role(request.getRole())
                .active(true)
                .build();

        User saved = userRepository.save(user);
        log.info("User registered: {} ({})", saved.getName(), saved.getRole());
        return mapToResponse(saved);
    }

    public UserResponse login(LoginRequest request) {
        User user = userRepository.findByMobile(request.getMobile())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with mobile: " + request.getMobile()));

        if (!verifyPassword(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidOperationException("Invalid credentials");
        }

        if (!user.getActive()) {
            throw new InvalidOperationException("Account is deactivated");
        }

        log.info("User logged in: {} ({})", user.getName(), user.getRole());
        return mapToResponse(user);
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
        return mapToResponse(user);
    }

    public List<UserResponse> getDeliveryPartners() {
        return userRepository.findByRoleAndActiveTrue(com.campusfood.enums.UserRole.DELIVERY)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    /**
     * Simple password hashing for MVP. Replace with BCrypt in production.
     */
    private String hashPassword(String password) {
        // TODO: Use BCryptPasswordEncoder for production
        return Integer.toHexString(password.hashCode());
    }

    private boolean verifyPassword(String rawPassword, String hashedPassword) {
        return hashPassword(rawPassword).equals(hashedPassword);
    }

    private UserResponse mapToResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .mobile(user.getMobile())
                .email(user.getEmail())
                .role(user.getRole())
                .active(user.getActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
