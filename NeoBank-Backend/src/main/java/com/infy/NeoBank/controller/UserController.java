package com.infy.NeoBank.controller;

import com.infy.NeoBank.dto.request.RegisterRequest;
import com.infy.NeoBank.dto.response.UserResponse;
import com.infy.NeoBank.repository.UserRepository;
import com.infy.NeoBank.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Users", description = "Profile and admin user management")
public class UserController {

    private final UserService userService;
    private final UserRepository userRepository;

    @GetMapping("/me")
    @Operation(summary = "Get authenticated user profile",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Profile returned"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            })
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        log.info("Fetching profile for userId={}", userId);
        UserResponse response = userService.getProfile(userId);
        log.info("Profile retrieved successfully for userId={}", userId);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    @Operation(summary = "Update authenticated user profile",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Profile updated"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            })
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody RegisterRequest request) {
        Long userId = resolveUserId(userDetails);
        log.info("Profile update request received. userId={}, email={}", userId, request.getEmail());
        UserResponse response = userService.updateProfile(userId, request);

        log.info("Profile updated successfully. userId={}", userId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/admin/users")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all users — admin only",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User list returned"),
                    @ApiResponse(responseCode = "403", description = "Admin role required")
            })
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        log.info("Admin requested all users");
        List<UserResponse> users = userService.getAllUsers();
        log.info("Admin retrieved {} users", users.size());
        return ResponseEntity.ok(users);
    }

    @PatchMapping("/admin/users/{userId}/toggle-status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Toggle user active status — admin only",
            responses = {
                    @ApiResponse(responseCode = "200", description = "User status toggled"),
                    @ApiResponse(responseCode = "403", description = "Admin role required"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            })
    public ResponseEntity<UserResponse> toggleUserStatus(@PathVariable Long userId) {
        log.info("Admin requested status toggle for userId={}", userId);
        UserResponse response = userService.toggleUserStatus(userId);
        log.info("User status toggled successfully. userId={}, newStatus={}", userId, response.isActive());
        return ResponseEntity.ok(response);
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: "+ userDetails.getUsername())).getId();
    }
}
