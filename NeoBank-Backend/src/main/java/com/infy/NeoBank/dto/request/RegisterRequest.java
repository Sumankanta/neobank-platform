package com.infy.NeoBank.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class RegisterRequest {

    // Step 1: Personal Info
    @NotBlank
    private String fullName;

    private String dob;

    private String gender;

    private String nationality;

    // Step 2: Contact Info
    @NotBlank
    @Email
    private String email;

    private String phone;

    // Step 3: Identity Verification
    private String idType;

    private String idNumber;

    // Step 4: Address
    private String street;

    private String city;

    private String state;

    private String zipCode;

    // Step 5: Nominee
    private String nomineeName;

    private String nomineeRelationship;

    private String nomineeDob;

    // Step 6: Account Preferences
    private String accountType;

    private String currency;

    // Step 7: Security
    @NotBlank
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must be at least 8 characters with uppercase, lowercase, digit, and special character"
    )
    private String password;
}
