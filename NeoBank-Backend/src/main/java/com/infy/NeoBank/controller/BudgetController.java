package com.infy.NeoBank.controller;


import com.infy.NeoBank.dto.request.BudgetRequest;
import com.infy.NeoBank.dto.response.BudgetSummaryResponse;
import com.infy.NeoBank.repository.UserRepository;
import com.infy.NeoBank.service.BudgetService;
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
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Budgets", description = "Monthly budget creation and utilization tracking")
public class BudgetController {

    private final BudgetService budgetService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a monthly budget for a category",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Budget created"),
                    @ApiResponse(responseCode = "400", description = "Validation failed"),
                    @ApiResponse(responseCode = "409", description = "Budget already exists for category/month")
            })
    public ResponseEntity<BudgetSummaryResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BudgetRequest request) {
        Long userId = resolveUserId(userDetails);
        log.info("Creating budget. userId={}, category={}, month={}", userId, request.getCategory(), request.getBudgetMonth());

        BudgetSummaryResponse response = budgetService.create(userId, request);

        log.info("Budget created successfully. userId={}, category={}", userId, response.getCategory());

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(response);
//        return ResponseEntity.status(HttpStatus.CREATED).body(budgetService.create(userId, request));
    }

    @GetMapping("/{userId}/{month}")
    @Operation(summary = "Get budget summary with utilization for a month",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Budget summary returned"),
                    @ApiResponse(responseCode = "400", description = "Invalid month format"),
                    @ApiResponse(responseCode = "403", description = "Cross-user access denied")
            })
    public ResponseEntity<List<BudgetSummaryResponse>> getSummary(
            @PathVariable Long userId,
            @PathVariable String month,
            @AuthenticationPrincipal UserDetails userDetails) {
        if (!month.matches("^\\d{4}-(0[1-9]|1[0-2])$")) {
            throw new com.infy.NeoBank.exception.InvalidRequestException("Invalid month format. Use YYYY-MM");
        }
        Long requestingUserId = resolveUserId(userDetails);
        log.info("Fetching budget summary. requestedUserId={}, month={}, requesterId={}", userId, month, requestingUserId);

        List<BudgetSummaryResponse> response = budgetService.getSummary(userId, month, requestingUserId);

        log.info("Budget summary returned. count={}, requestedUserId={}", response.size(), userId);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(summary = "List all budgets for authenticated user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Budget list returned")
            })
    public ResponseEntity<List<BudgetSummaryResponse>> getAll(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        log.info("Fetching all budgets for userId={}", userId);
        List<BudgetSummaryResponse> response = budgetService.getAll(userId);
        log.info("Retrieved {} budgets for userId={}", response.size(), userId);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a budget entry",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Budget deleted"),
                    @ApiResponse(responseCode = "403", description = "Not budget owner"),
                    @ApiResponse(responseCode = "404", description = "Budget not found")
            })
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        log.info("Delete budget request. budgetId={}, userId={}", id, userId);
        budgetService.delete(id, userId);
        log.info("Budget deleted successfully. budgetId={}, userId={}", id, userId);
        return ResponseEntity.noContent().build();
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found: "
                                        + userDetails.getUsername()))
                .getId();
    }
}
