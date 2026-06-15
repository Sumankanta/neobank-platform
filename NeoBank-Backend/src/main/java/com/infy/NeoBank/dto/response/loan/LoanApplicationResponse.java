package com.infy.NeoBank.dto.response.loan;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LoanApplicationResponse {
    private Long id;
    private Long userId;
    private Long loanProductId;
    private String productName;
    private BigDecimal requestedAmount;
    private String status;
    private String adminRemarks;
    private LocalDateTime appliedAt;
    private LocalDateTime decisionAt;
}
