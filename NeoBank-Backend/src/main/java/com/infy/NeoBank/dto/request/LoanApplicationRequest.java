package com.infy.NeoBank.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class LoanApplicationRequest {

    @NotNull
    private Long productId;

    @NotNull
    @Min(1000)
    private BigDecimal amount;

    @NotNull
    @Min(6)
    private Integer tenureMonths;

    private String purpose;
}
