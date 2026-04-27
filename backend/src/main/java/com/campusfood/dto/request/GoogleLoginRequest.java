package com.campusfood.dto.request;

import lombok.Data;

@Data
public class GoogleLoginRequest {
    private String email;
    private String name;
}
