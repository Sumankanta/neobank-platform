package com.infy.NeoBank.dto.response;

import com.infy.NeoBank.enums.BudgetCategory;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BudgetSummaryResponse {

    private Long id;
    private BudgetCategory category;
    private String budgetMonth;
    private BigDecimal budgetLimit;
    private BigDecimal spentAmount;
    private BigDecimal remaining;
    private double utilizationPercentage;
}
