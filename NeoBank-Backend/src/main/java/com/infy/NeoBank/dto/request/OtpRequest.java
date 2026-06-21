package com.infy.NeoBank.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class OtpRequest {
    @NotBlank(message = "Contact identifier is required")
    private String contact;

    @NotBlank(message = "Contact type is required (phone or email)")
    private String contactType;
}
