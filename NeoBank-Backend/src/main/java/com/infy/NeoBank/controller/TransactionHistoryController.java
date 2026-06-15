package com.infy.NeoBank.controller;

import com.infy.NeoBank.dto.response.TransactionResponse;
import com.infy.NeoBank.repository.UserRepository;
import com.infy.NeoBank.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/transactions")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Transaction History", description = "User-wide transaction history and analytics")
public class TransactionHistoryController {

    private final TransactionService transactionService;
    private final UserRepository userRepository;

    @GetMapping("/history")
    @Operation(summary = "Get all transactions for authenticated user across all accounts",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Paginated transactions returned")
            })
    public ResponseEntity<Page<TransactionResponse>> getHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate,
            @PageableDefault(size = 20, sort = "transactionDate", direction = Sort.Direction.DESC) Pageable pageable) {
        Long userId = resolveUserId(userDetails);
        log.info("Fetching all transactions for userId={}, startDate={}, endDate={}", userId, startDate, endDate);
        return ResponseEntity.ok(transactionService.getAllForUser(userId, startDate, endDate, pageable));
    }

    @GetMapping("/summary")
    @Operation(summary = "Get spending summary by category for current month",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Category-wise summary returned")
            })
    public ResponseEntity<Map<String, BigDecimal>> getSummary(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        log.info("Fetching spending summary for userId={}", userId);
        return ResponseEntity.ok(transactionService.getSpendingSummary(userId));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + userDetails.getUsername()))
                .getId();
    }
}
