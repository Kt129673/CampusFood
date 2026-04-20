package com.campusfood.dto.response;

import com.campusfood.enums.UserRole;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {

    private Long id;
    private String name;
    private String mobile;
    private String email;
    private UserRole role;
    private Boolean active;
    private LocalDateTime createdAt;
}
