package com.infy.NeoBank.repository;

import com.infy.NeoBank.entity.LoanAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface LoanAccountRepository extends JpaRepository<LoanAccount, Long> {
    List<LoanAccount> findByUserId(Long userId);
    Optional<LoanAccount> findOneByUserId(Long userId);
}
