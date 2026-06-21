package com.infy.NeoBank.controller;

import com.infy.NeoBank.dto.request.LoginRequest;
import com.infy.NeoBank.dto.request.RegisterRequest;
import com.infy.NeoBank.dto.request.OtpRequest;
import com.infy.NeoBank.dto.request.OtpVerificationRequest;
import com.infy.NeoBank.dto.response.AuthResponse;
import com.infy.NeoBank.dto.response.UserResponse;

import com.infy.NeoBank.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;


@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "Register and login endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user",
            responses = {
                    @ApiResponse(responseCode = "201", description = "User registered successfully"),
                    @ApiResponse(responseCode = "400", description = "Validation failed"),
                    @ApiResponse(responseCode = "409", description = "Email already registered")
            })
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        log.info("Registration request received for email: {}", request.getEmail());

        UserResponse response = authService.register(request);

        log.info("User registered successfully with email: {}", response.getEmail());

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    @Operation(summary = "Login and receive JWT",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Login successful"),
                    @ApiResponse(responseCode = "401", description = "Invalid credentials"),
                    @ApiResponse(responseCode = "403", description = "Account inactive")
            })
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {

        log.info("Login attempt for email: {}", request.getEmail());

        AuthResponse response = authService.login(request);

        log.info("Login successful for email: {}", response.getEmail());

        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify-document")
    @Operation(summary = "Verify document number uniqueness",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Verification successful")
            })
    public ResponseEntity<Map<String, Boolean>> verifyDocument(@RequestParam String idNumber) {
        log.info("Verifying document number uniqueness: {}", idNumber);
        boolean isVerified = authService.verifyDocument(idNumber);
        return ResponseEntity.ok(Map.of("verified", isVerified));
    }

    @PostMapping("/otp/request")
    @Operation(summary = "Request sign-in OTP via SMS or Email",
            responses = {
                    @ApiResponse(responseCode = "200", description = "OTP sent successfully"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            })
    public ResponseEntity<Map<String, String>> requestOtp(@Valid @RequestBody OtpRequest request) {
        log.info("OTP request received for contact: {}", request.getContact());
        authService.requestOtp(request);
        return ResponseEntity.ok(Map.of("message", "OTP sent successfully"));
    }

    @PostMapping("/otp/verify")
    @Operation(summary = "Verify OTP code and authenticate user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Authentication successful"),
                    @ApiResponse(responseCode = "401", description = "Invalid/expired OTP"),
                    @ApiResponse(responseCode = "403", description = "Account inactive")
            })
    public ResponseEntity<AuthResponse> verifyOtp(@Valid @RequestBody OtpVerificationRequest request) {
        log.info("OTP verification request received for contact: {}", request.getContact());
        AuthResponse response = authService.verifyOtp(request);
        log.info("OTP verified successfully. User logged in: {}", response.getEmail());
        return ResponseEntity.ok(response);
    }
}

