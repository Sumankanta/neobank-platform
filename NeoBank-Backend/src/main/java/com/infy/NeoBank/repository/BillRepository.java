package com.infy.NeoBank.repository;

import com.infy.NeoBank.entity.Bill;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface BillRepository extends JpaRepository<Bill, Long> {

    // GET /api/bills — all bills for caller
    List<Bill> findByUserId(Long userId);

    // GET /api/bills/{id} — ownership validated in single query
    Optional<Bill> findByIdAndUserId(Long id, Long userId);

    // POST /api/bills — duplicate biller+month check (BR-02)
    // from = first day of month, to = last day of month
    boolean existsByUserIdAndBillerNameAndDueDateBetween(
            Long userId,
            String billerName,
            LocalDate from,
            LocalDate to
    );
}