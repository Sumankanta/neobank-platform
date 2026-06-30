package com.infy.NeoBank.dto.response.loan;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class LoanApplicationResponse {
    private Long id;
    private Long userId;
    private String userFullName;
    private String userEmail;
    private Long loanProductId;
    private String productName;
    private LoanProductResponse loanProduct;
    private BigDecimal requestedAmount;
    private Integer tenureMonths;
    private String purpose;
    private String status;
    private String adminRemarks;
    private LocalDateTime appliedAt;
    private LocalDateTime decisionAt;
}
