package com.infy.NeoBank.dto.response.insights;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class InsightsResponse {
    private List<FinancialTrend> trends;
    private List<SpendingCategory> categories;
    private BigDecimal totalIncome;
    private BigDecimal totalExpense;
    private BigDecimal netSavings;

    @Data
    public static class FinancialTrend {
        private String month;
        private BigDecimal income;
        private BigDecimal expense;
    }

    @Data
    public static class SpendingCategory {
        private String category;
        private BigDecimal amount;
        private BigDecimal percentage;
    }
}
