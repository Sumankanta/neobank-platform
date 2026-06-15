package com.infy.NeoBank.dto.request;

import com.infy.NeoBank.enums.BudgetCategory;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetRequest {

    @NotNull
    private BudgetCategory category;

    @NotBlank
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "Month must be in YYYY-MM format")
    private String budgetMonth;

    @NotNull
    @DecimalMin(value = "0.01", message = "Limit amount must be greater than zero")
    private BigDecimal budgetLimit;
}
