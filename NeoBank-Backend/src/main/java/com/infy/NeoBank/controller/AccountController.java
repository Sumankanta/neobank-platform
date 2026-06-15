package com.infy.NeoBank.controller;

import com.infy.NeoBank.dto.request.AccountRequest;
import com.infy.NeoBank.dto.response.AccountResponse;
import com.infy.NeoBank.repository.UserRepository;
import com.infy.NeoBank.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Accounts", description = "Bank account creation and retrieval")
public class AccountController {

    private final AccountService accountService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a new bank account",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Account created"),
                    @ApiResponse(responseCode = "400", description = "Validation failed"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            })
    public ResponseEntity<AccountResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody AccountRequest request) {
        Long userId = resolveUserId(userDetails);
        AccountResponse response = accountService.create(userId, request);
        log.info(
                "Account created successfully. userId={}, accountId={}",
                userId,
                response.getId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all accounts for authenticated user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Account list returned"),
                    @ApiResponse(responseCode = "401", description = "Unauthorized")
            })
    public ResponseEntity<List<AccountResponse>> getAll(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        log.info("Fetching all accounts for userId={}", userId);
        return ResponseEntity.ok(accountService.getAll(userId));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get account by ID",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Account returned"),
                    @ApiResponse(responseCode = "403", description = "Not account owner"),
                    @ApiResponse(responseCode = "404", description = "Account not found")
            })
    public ResponseEntity<AccountResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        log.info("Fetching account {} for userId={}", id, userId);
        return ResponseEntity.ok(accountService.getById(id, userId));
    }

//    private Long resolveUserId(UserDetails userDetails) {
//        return userRepository.findByEmail(userDetails.getUsername())
//                .orElseThrow()
//                .getId();
//    }

    private Long resolveUserId(UserDetails userDetails) {

        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found: "
                                        + userDetails.getUsername()))
                .getId();
    }
}
