package com.infy.NeoBank.dto.response.loan;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanAccountResponse {
    private Long id;
    private Long userId;
    private Long loanProductId;
    private String productName;
    private BigDecimal principalAmount;
    private BigDecimal remainingBalance;
    private LocalDate startDate;
    private LocalDate endDate;
}
