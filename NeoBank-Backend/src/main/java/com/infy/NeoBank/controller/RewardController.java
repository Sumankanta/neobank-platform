package com.infy.NeoBank.controller;

import com.infy.NeoBank.dto.response.RewardResponse;
import com.infy.NeoBank.repository.UserRepository;
import com.infy.NeoBank.service.RewardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/rewards")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Rewards", description = "Reward points balance and accrual")
public class RewardController {

    private final RewardService rewardService;
    private final UserRepository userRepository;

    @GetMapping("/{userId}")
    @Operation(summary = "Get reward points balance for a user",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Reward balance returned"),
                    @ApiResponse(responseCode = "403", description = "Cross-user access denied"),
                    @ApiResponse(responseCode = "404", description = "User not found")
            })
    public ResponseEntity<RewardResponse> getBalance(
            @PathVariable Long userId,
            @AuthenticationPrincipal UserDetails userDetails) {
        Long requestingUserId = resolveUserId(userDetails);
        log.info("Fetching rewards. requestedUserId={}, requesterId={}", userId, requestingUserId);
        RewardResponse response = rewardService.getBalance(userId, requestingUserId);
        log.info("Rewards fetched successfully. requestedUserId={}, points={}", userId, response.getPointsBalance());
        return ResponseEntity.ok(response);
    }

    private Long resolveUserId(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("Authenticated user not found: " + userDetails.getUsername())).getId();
    }
}
