package com.infy.NeoBank.controller;

import com.infy.NeoBank.dto.request.BillRequest;
import com.infy.NeoBank.dto.request.BillStatusRequest;
import com.infy.NeoBank.dto.response.BillResponse;
import com.infy.NeoBank.repository.UserRepository;
import com.infy.NeoBank.service.BillService;
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
@RequestMapping("/api/bills")
@RequiredArgsConstructor
@Slf4j
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Bills", description = "Bill scheduling, status tracking and reminders")
public class BillController {

    private final BillService billService;
    private final UserRepository userRepository;

    @PostMapping
    @Operation(summary = "Create a new bill",
            responses = {
                    @ApiResponse(responseCode = "201", description = "Bill created"),
                    @ApiResponse(responseCode = "400", description = "Past due date or validation failed"),
                    @ApiResponse(responseCode = "409", description = "Duplicate bill for biller/month")
            })
    public ResponseEntity<BillResponse> create(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BillRequest request) {
        Long userId = resolveUserId(userDetails);

        log.info(
                "Create bill request. userId={}, billerName={}",
                userId,
                request.getBillerName()
        );

        BillResponse response = billService.create(userId, request);

        log.info(
                "Bill created successfully. userId={}, billId={}",
                userId,
                response.getId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @Operation(summary = "List all bills for authenticated user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Bill list with remindMe flags returned")
            })
    public ResponseEntity<List<BillResponse>> getAll(
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        log.info("Fetching all bills for userId={}", userId);

        List<BillResponse> bills = billService.getAll(userId);

        log.info(
                "Retrieved {} bills for userId={}",
                bills.size(),
                userId
        );
        return ResponseEntity.ok(bills);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a specific bill",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Bill returned"),
                    @ApiResponse(responseCode = "403", description = "Not bill owner"),
                    @ApiResponse(responseCode = "404", description = "Bill not found")
            })
    public ResponseEntity<BillResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        log.info(
                "Fetching bill. billId={}, userId={}",
                id,
                userId
        );

        BillResponse response = billService.getById(id, userId);

        log.info(
                "Bill fetched successfully. billId={}, userId={}",
                id,
                userId
        );

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update bill payment status",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Status updated"),
                    @ApiResponse(responseCode = "400", description = "Invalid status transition"),
                    @ApiResponse(responseCode = "403", description = "Not bill owner"),
                    @ApiResponse(responseCode = "404", description = "Bill not found")
            })
    public ResponseEntity<BillResponse> updateStatus(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody BillStatusRequest request) {
        Long userId = resolveUserId(userDetails);
        log.info("Updating bill status. billId={}, userId={}, status={}",
                id, userId, request.getStatus()
        );

        BillResponse response = billService.updateStatus(id, userId, request);

        log.info("Bill status updated successfully. billId={}, status={}",
                id, response.getStatus()
        );

        return ResponseEntity.ok(response);
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
