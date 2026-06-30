package com.infy.NeoBank.dto.response.loan;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class LoanAccountResponse {
    private Long id;
    private Long userId;
    private LoanProductResponse loanProduct;
    private BigDecimal principalAmount;
    private BigDecimal remainingBalance;
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal monthlyEmi;
    private String status;
    private Integer tenureMonths;
    private BigDecimal interestRate;
}
