package com.infy.NeoBank.dto.response.loan;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class LoanProductResponse {
    private Long id;
    private String productName;
    private BigDecimal referenceAmount;
    private BigDecimal minAmount;
    private BigDecimal maxAmount;
    private BigDecimal annualInterestRate;
    private Integer durationMonths;
    private Integer minTenureMonths;
    private Integer maxTenureMonths;
    private String description;
}
