package com.infy.NeoBank.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpVerificationRequest {
    @NotBlank(message = "Contact identifier is required")
    private String contact;

    @NotBlank(message = "OTP verification code is required")
    private String code;
}
