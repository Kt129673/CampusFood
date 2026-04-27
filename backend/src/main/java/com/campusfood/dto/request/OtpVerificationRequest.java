package com.campusfood.dto.request;

import lombok.Data;

@Data
public class OtpVerificationRequest {
    private String mobile;
    private String otp;
}
