package com.infy.NeoBank.controller;

import com.infy.NeoBank.dto.response.loan.LoanAccountResponse;
import com.infy.NeoBank.dto.response.loan.LoanApplicationResponse;
import com.infy.NeoBank.dto.response.loan.LoanProductResponse;
import com.infy.NeoBank.repository.UserRepository;
import com.infy.NeoBank.service.LoanService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

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

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + userDetails.getUsername())).getId();
    }
}
