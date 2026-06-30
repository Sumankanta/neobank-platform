package com.infy.NeoBank.controller;

import com.infy.NeoBank.dto.request.LoanApplicationRequest;
import com.infy.NeoBank.dto.response.loan.LoanAccountResponse;
import com.infy.NeoBank.dto.response.loan.LoanApplicationResponse;
import com.infy.NeoBank.dto.response.loan.LoanProductResponse;
import com.infy.NeoBank.dto.response.loan.LoanRepaymentResponse;
import com.infy.NeoBank.repository.UserRepository;
import com.infy.NeoBank.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/loans")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Loans", description = "Loan product browsing and application tracking")
public class LoanController {

    private final LoanService loanService;
    private final UserRepository userRepository;

    @GetMapping("/products")
    @Operation(summary = "Get all available loan products")
    public ResponseEntity<List<LoanProductResponse>> getAllProducts() {
        return ResponseEntity.ok(loanService.getAllProducts());
    }

    @GetMapping("/my-applications")
    @Operation(summary = "Get loan applications for current user")
    public ResponseEntity<List<LoanApplicationResponse>> getMyApplications(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(loanService.getMyApplications(userId));
    }

    @GetMapping("/my-accounts")
    @Operation(summary = "Get active loan accounts for current user")
    public ResponseEntity<List<LoanAccountResponse>> getMyAccounts(@AuthenticationPrincipal UserDetails userDetails) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(loanService.getMyAccounts(userId));
    }

    @PostMapping("/apply")
    @Operation(summary = "Apply for a loan")
    public ResponseEntity<LoanApplicationResponse> applyForLoan(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody LoanApplicationRequest request) {
        Long userId = resolveUserId(userDetails);
        return ResponseEntity.ok(loanService.applyForLoan(userId, request));
    }

    @GetMapping("/admin/applications")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get all loan applications (Admin only)")
    public ResponseEntity<List<LoanApplicationResponse>> getAdminApplications() {
        return ResponseEntity.ok(loanService.getAdminApplications());
    }

    @PutMapping("/{id}/decision")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Process a loan application (Admin only)")
    public ResponseEntity<LoanApplicationResponse> processApplication(
            @PathVariable Long id,
            @RequestBody Map<String, String> decision) {
        String status = decision.get("status");
        String reason = decision.get("reason");
        return ResponseEntity.ok(loanService.processApplication(id, status, reason));
    }

    @GetMapping("/{id}/repayments")
    @Operation(summary = "Get repayment schedule for a loan account")
    public ResponseEntity<List<LoanRepaymentResponse>> getRepaymentSchedule(@PathVariable Long id) {
        return ResponseEntity.ok(loanService.getRepaymentSchedule(id));
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + userDetails.getUsername())).getId();
    }
}
