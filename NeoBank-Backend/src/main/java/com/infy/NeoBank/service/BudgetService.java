package com.infy.NeoBank.service;

import com.infy.NeoBank.dto.request.BudgetRequest;
import com.infy.NeoBank.dto.response.BudgetSummaryResponse;
import com.infy.NeoBank.entity.Budget;
import com.infy.NeoBank.entity.User;
import com.infy.NeoBank.enums.BudgetCategory;
import com.infy.NeoBank.enums.TransactionType;
import com.infy.NeoBank.exception.AccessDeniedException;
import com.infy.NeoBank.exception.DuplicateResourceException;
import com.infy.NeoBank.exception.ResourceNotFoundException;
import com.infy.NeoBank.repository.AccountRepository;
import com.infy.NeoBank.repository.BudgetRepository;
import com.infy.NeoBank.repository.TransactionRepository;
import com.infy.NeoBank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;

    private static final Map<BudgetCategory, List<String>> CATEGORY_KEYWORDS = Map.of(
            BudgetCategory.GROCERIES, List.of("grocery", "groceries", "supermarket", "food"),
            BudgetCategory.UTILITIES, List.of("utility", "utilities", "electricity", "water", "gas"),
            BudgetCategory.RENT, List.of("rent", "lease", "housing"),
            BudgetCategory.ENTERTAINMENT, List.of("entertainment", "movie", "netflix", "spotify"),
            BudgetCategory.TRANSFER, List.of("transfer", "neft", "imps", "upi")
    );

    @Transactional
    public BudgetSummaryResponse create(Long userId, BudgetRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        LocalDate budgetMonth = LocalDate.parse(request.getBudgetMonth() + "-01");

        budgetRepository.findByUserIdAndCategoryAndBudgetLimit(userId, request.getCategory(), request.getBudgetLimit())
                .ifPresent(b -> { throw new DuplicateResourceException(
                        "Budget already exists for category " + request.getCategory() + " with limit " + request.getBudgetLimit()); });

        Budget budget = new Budget();
        budget.setUser(user);
        budget.setCategory(request.getCategory());
        budget.setBudgetMonth(budgetMonth);
        budget.setBudgetLimit(request.getBudgetLimit());

        return mapToSummary(budgetRepository.save(budget), BigDecimal.ZERO);
    }

    @Transactional(readOnly = true)
    public List<BudgetSummaryResponse> getSummary(Long userId, String month, Long requestingUserId) {
        if (!userId.equals(requestingUserId)) {
            throw new AccessDeniedException("Access denied to budget data of user: " + userId);
        }

        LocalDate budgetMonth = LocalDate.parse(month + "-01");
        List<Budget> budgets = budgetRepository.findByUserIdAndBudgetMonth(userId, budgetMonth);

        LocalDateTime from = budgetMonth.atStartOfDay();
        LocalDateTime to = budgetMonth.plusMonths(1).atStartOfDay();

        return budgets.stream().map(budget -> {
            BigDecimal spent = calculateSpent(userId, budget.getCategory(), from, to);
            return mapToSummary(budget, spent);
        }).toList();
    }

    @Transactional(readOnly = true)
    public List<BudgetSummaryResponse> getAll(Long userId) {
        return budgetRepository.findByUserId(userId).stream()
                .map(b -> mapToSummary(b, BigDecimal.ZERO))
                .toList();
    }

    @Transactional
    public void delete(Long budgetId, Long userId) {
        Budget budget = budgetRepository.findById(budgetId)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found: " + budgetId));
        if (!budget.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Access denied to budget: " + budgetId);
        }
        budgetRepository.delete(budget);
    }

    private BigDecimal calculateSpent(Long userId, BudgetCategory category,
                                      LocalDateTime from, LocalDateTime to) {
        List<String> keywords = CATEGORY_KEYWORDS.getOrDefault(category, List.of());

        return accountRepository.findByUserId(userId).stream()
                .flatMap(account -> transactionRepository
                        .findByAccountIdAndTypeAndTransactionDateBetween(
                                account.getId(), TransactionType.DEBIT, from, to)
                        .stream())
                .filter(txn -> {
                    if (keywords.isEmpty()) return true;
                    String desc = txn.getDescription() == null ? "" : txn.getDescription().toLowerCase();
                    return keywords.stream().anyMatch(desc::contains);
                })
                .map(txn -> txn.getAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private BudgetSummaryResponse mapToSummary(Budget budget, BigDecimal spent) {
        BigDecimal limit = budget.getBudgetLimit();
        BigDecimal remaining = limit.subtract(spent).max(BigDecimal.ZERO);
        double utilization = limit.compareTo(BigDecimal.ZERO) == 0 ? 0.0 :
                spent.divide(limit, 4, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .doubleValue();

        BudgetSummaryResponse r = new BudgetSummaryResponse();
        r.setId(budget.getId());
        r.setCategory(budget.getCategory());
        r.setBudgetMonth(budget.getBudgetMonth().getYear() + "-"
                + String.format("%02d", budget.getBudgetMonth().getMonthValue()));
        r.setBudgetLimit(limit);
        r.setSpentAmount(spent);
        r.setRemaining(remaining);
        r.setUtilizationPercentage(utilization);
        return r;
    }
}

