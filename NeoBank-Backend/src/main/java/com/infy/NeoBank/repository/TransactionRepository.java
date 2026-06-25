package com.infy.NeoBank.repository;

import com.infy.NeoBank.entity.Transaction;
import com.infy.NeoBank.enums.TransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // GET /api/accounts/{id}/transactions — paginated, date desc (AC-11)
    Page<Transaction> findByAccountIdOrderByTransactionDateDesc(
            Long accountId,
            Pageable pageable
    );

    // Sprint 4: All user transactions for history and analytics
    Page<Transaction> findByAccountUserIdOrderByTransactionDateDesc(
            Long userId,
            Pageable pageable
    );

    // Sprint 4: Filter user transactions by date range
    List<Transaction> findByAccountUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
            Long userId,
            LocalDateTime start,
            LocalDateTime end
    );

    Page<Transaction> findByAccountUserIdAndTransactionDateBetween(
            Long userId,
            LocalDateTime start,
            LocalDateTime end,
            Pageable pageable
    );

    // BudgetService.calculateSpent() — DEBIT transactions in date range
    List<Transaction> findByAccountIdAndTypeAndTransactionDateBetween(
            Long accountId,
            TransactionType type,
            LocalDateTime from,
            LocalDateTime to
    );

    @Query("SELECT SUM(t.amount) FROM Transaction t WHERE t.account.user.id = :userId AND t.type = :type")
    BigDecimal sumAmountByUserIdAndType(@Param("userId") Long userId, @Param("type") TransactionType type);

    @Query("SELECT t.category, SUM(t.amount) FROM Transaction t WHERE t.type = 'DEBIT' AND t.account.user.id = :userId GROUP BY t.category")
    List<Object[]> findSpendingByCategory(@Param("userId") Long userId);
}