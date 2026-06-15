package com.infy.NeoBank.repository;

import com.infy.NeoBank.entity.Budget;
import com.infy.NeoBank.enums.BudgetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BudgetRepository extends JpaRepository<Budget, Long> {

    // GET /api/budgets/{userId}/{month} — fetch budgets for one month
    List<Budget> findByUserIdAndBudgetMonth(
            Long userId,
            LocalDate budgetMonth
    );

    // GET /api/budgets — all budgets across all months
    List<Budget> findByUserId(Long userId);

    // POST /api/budgets — duplicate check (updated to match new unique constraint)
    Optional<Budget> findByUserIdAndCategoryAndBudgetLimit(
            Long userId,
            BudgetCategory category,
            BigDecimal budgetLimit
    );
}