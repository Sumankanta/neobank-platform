package com.infy.NeoBank.dto.response.loan;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class LoanRepaymentResponse {
    private Long id;
    private Long loanAccountId;
    private Integer installmentNumber;
    private LocalDate dueDate;
    private BigDecimal installmentAmount;
    private BigDecimal principalComponent;
    private BigDecimal interestComponent;
    private LocalDateTime paymentDate;
    private String status;
}
