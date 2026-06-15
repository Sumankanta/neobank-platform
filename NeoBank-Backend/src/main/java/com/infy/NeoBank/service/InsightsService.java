package com.infy.NeoBank.service;

import com.infy.NeoBank.dto.response.insights.InsightsResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class InsightsService {

    public InsightsResponse getInsights(Long userId) {
        InsightsResponse response = new InsightsResponse();
        
        // Mock Trends
        List<InsightsResponse.FinancialTrend> trends = new ArrayList<>();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun"};
        for (String month : months) {
            InsightsResponse.FinancialTrend t = new InsightsResponse.FinancialTrend();
            t.setMonth(month);
            t.setIncome(new BigDecimal("50000").add(new BigDecimal(Math.random() * 10000)));
            t.setExpense(new BigDecimal("30000").add(new BigDecimal(Math.random() * 5000)));
            trends.add(t);
        }
        response.setTrends(trends);

        // Mock Categories
        List<InsightsResponse.SpendingCategory> categories = new ArrayList<>();
        String[] cats = {"Shopping", "Food", "Bills", "Rent", "Travel"};
        for (String cat : cats) {
            InsightsResponse.SpendingCategory c = new InsightsResponse.SpendingCategory();
            c.setCategory(cat);
            c.setAmount(new BigDecimal("5000").add(new BigDecimal(Math.random() * 2000)));
            c.setPercentage(new BigDecimal("20"));
            categories.add(c);
        }
        response.setCategories(categories);

        response.setTotalIncome(new BigDecimal("350000"));
        response.setTotalExpense(new BigDecimal("210000"));
        response.setNetSavings(new BigDecimal("140000"));

        return response;
    }
}
