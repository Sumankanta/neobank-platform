package com.infy.NeoBank.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class RewardResponse {

    private Long id;
    private Long userId;
    private int pointsBalance;
    private LocalDateTime lastUpdated;
}
