package com.infy.NeoBank.controller;

import com.infy.NeoBank.dto.request.TransactionRequest;
import com.infy.NeoBank.dto.response.TransactionResponse;
import com.infy.NeoBank.repository.UserRepository;
import com.infy.NeoBank.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/accounts/{accountId}/transactions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Transactions", description = "Debit and credit transaction processing")
public class TransactionController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a debit or credit transaction",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Transaction created"),
                    @ApiResponse(responseCode = "400", description = "Invalid amount"),
                    @ApiResponse(responseCode = "403", description = "Not account owner"),
                    @ApiResponse(responseCode = "422", description = "Insufficient balance")
            })
    public ResponseEntity<TransactionResponse> create(
            @PathVariable Long accountId,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody TransactionRequest request) {
        Long userId = resolveUserId(userDetails);
        log.info("Transaction request received. accountId={}, userId={}, type={}, amount={}", accountId, userId, request.getType(), request.getAmount());
        TransactionResponse response = transactionService.create(accountId, userId, request);
        log.info("Transaction completed successfully. accountId={}, transactionId={}, type={}, amount={}", accountId, response.getId(), response.getType(), response.getAmount());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "Get paginated transaction history",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Transaction history returned"),
                    @ApiResponse(responseCode = "403", description = "Not account owner"),
                    @ApiResponse(responseCode = "404", description = "Account not found")
            })
    public ResponseEntity<Page<TransactionResponse>> getHistory(
            @PathVariable Long accountId,
            @AuthenticationPrincipal UserDetails userDetails,
            @PageableDefault(size = 20, sort = "transactionDate",
                    direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = resolveUserId(userDetails);
        log.info("Fetching transaction history. accountId={}, userId={}, page={}, size={}", accountId, userId, pageable.getPageNumber(), pageable.getPageSize());
        Page<TransactionResponse> response = transactionService.getHistory(accountId, userId, pageable);
        log.info("Transaction history returned. accountId={}, totalElements={}", accountId, response.getTotalElements());
        return ResponseEntity.ok(response);
    }



    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + userDetails.getUsername()))
                .getId();
    }
}
