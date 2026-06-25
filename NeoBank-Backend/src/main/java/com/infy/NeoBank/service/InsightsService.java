package com.infy.NeoBank.service;

import com.infy.NeoBank.dto.response.insights.InsightsResponse;
import com.infy.NeoBank.entity.Transaction;
import com.infy.NeoBank.enums.TransactionType;
import com.infy.NeoBank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.TextStyle;
import java.util.*;

@Service
@RequiredArgsConstructor
public class InsightsService {

    private final TransactionRepository transactionRepository;

    public InsightsResponse getInsights(Long userId) {
        InsightsResponse response = new InsightsResponse();

        // 1. Calculate overall totals
        BigDecimal totalIncome = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.CREDIT);
        if (totalIncome == null) {
            totalIncome = BigDecimal.ZERO;
        }
        BigDecimal totalExpense = transactionRepository.sumAmountByUserIdAndType(userId, TransactionType.DEBIT);
        if (totalExpense == null) {
            totalExpense = BigDecimal.ZERO;
        }

        response.setTotalIncome(totalIncome);
        response.setTotalExpense(totalExpense);
        response.setNetSavings(totalIncome.subtract(totalExpense));

        // 2. Fetch spending categories dynamically
        List<Object[]> categorySums = transactionRepository.findSpendingByCategory(userId);
        List<InsightsResponse.SpendingCategory> categories = new ArrayList<>();

        if (categorySums != null) {
            for (Object[] row : categorySums) {
                String category = (String) row[0];
                BigDecimal amount = (BigDecimal) row[1];
                if (amount == null) {
                    amount = BigDecimal.ZERO;
                }

                InsightsResponse.SpendingCategory c = new InsightsResponse.SpendingCategory();
                c.setCategory(category);
                c.setAmount(amount);

                BigDecimal percentage = BigDecimal.ZERO;
                if (totalExpense.compareTo(BigDecimal.ZERO) > 0) {
                    percentage = amount.multiply(new BigDecimal("100"))
                            .divide(totalExpense, 2, RoundingMode.HALF_UP);
                }
                c.setPercentage(percentage);
                categories.add(c);
            }
        }
        response.setCategories(categories);

        // 3. Fetch monthly trends dynamically (for the last 6 months)
        LocalDateTime start = LocalDateTime.now().minusMonths(5).withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = LocalDateTime.now();
        List<Transaction> recentTransactions = transactionRepository
                .findByAccountUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(userId, start, end);

        List<InsightsResponse.FinancialTrend> trends = new ArrayList<>();
        List<YearMonth> months = new ArrayList<>();
        YearMonth currentMonth = YearMonth.now();
        for (int i = 5; i >= 0; i--) {
            months.add(currentMonth.minusMonths(i));
        }

        Map<YearMonth, BigDecimal> monthlyIncome = new HashMap<>();
        Map<YearMonth, BigDecimal> monthlyExpense = new HashMap<>();
        for (YearMonth ym : months) {
            monthlyIncome.put(ym, BigDecimal.ZERO);
            monthlyExpense.put(ym, BigDecimal.ZERO);
        }

        if (recentTransactions != null) {
            for (Transaction t : recentTransactions) {
                LocalDateTime date = t.getTransactionDate();
                if (date != null) {
                    YearMonth ym = YearMonth.from(date);
                    if (monthlyIncome.containsKey(ym)) {
                        if (t.getType() == TransactionType.CREDIT) {
                            monthlyIncome.put(ym, monthlyIncome.get(ym).add(t.getAmount()));
                        } else if (t.getType() == TransactionType.DEBIT) {
                            monthlyExpense.put(ym, monthlyExpense.get(ym).add(t.getAmount()));
                        }
                    }
                }
            }
        }

        for (YearMonth ym : months) {
            InsightsResponse.FinancialTrend t = new InsightsResponse.FinancialTrend();
            String monthName = ym.getMonth().getDisplayName(TextStyle.SHORT, Locale.ENGLISH);
            t.setMonth(monthName);
            t.setIncome(monthlyIncome.get(ym));
            t.setExpense(monthlyExpense.get(ym));
            trends.add(t);
        }
        response.setTrends(trends);

        return response;
    }
}
