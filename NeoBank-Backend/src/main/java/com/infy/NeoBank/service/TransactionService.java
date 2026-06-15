package com.infy.NeoBank.service;

import com.infy.NeoBank.dto.request.TransactionRequest;
import com.infy.NeoBank.dto.response.TransactionResponse;
import com.infy.NeoBank.entity.Account;
import com.infy.NeoBank.entity.Transaction;
import com.infy.NeoBank.enums.TransactionType;
import com.infy.NeoBank.exception.AccessDeniedException;
import com.infy.NeoBank.exception.InsufficientBalanceException;
import com.infy.NeoBank.exception.ResourceNotFoundException;
import com.infy.NeoBank.repository.AccountRepository;
import com.infy.NeoBank.repository.TransactionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final RewardService rewardService;

    @Transactional
    public TransactionResponse create(Long accountId, Long userId, TransactionRequest request) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));

        if (!account.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Access denied to account: " + accountId);
        }

        if (request.getType() == TransactionType.DEBIT &&
                account.getBalance().compareTo(request.getAmount()) < 0) {
            throw new InsufficientBalanceException("Insufficient balance for debit");
        }

        if (request.getType() == TransactionType.DEBIT) {
            account.setBalance(account.getBalance().subtract(request.getAmount()));
        } else {
            account.setBalance(account.getBalance().add(request.getAmount()));
        }

        accountRepository.save(account);

        Transaction txn = new Transaction();
        txn.setAccount(account);
        txn.setType(request.getType());
        txn.setAmount(request.getAmount());
        txn.setDescription(request.getDescription());
        txn.setBalanceAfter(account.getBalance());

        Transaction saved = transactionRepository.save(txn);

        // Sprint 2: Accrue 1 point for every 10 spent
        if (request.getType() == TransactionType.DEBIT) {
            int points = request.getAmount().divide(java.math.BigDecimal.valueOf(10), 0, java.math.RoundingMode.FLOOR).intValue();
            if (points > 0) {
                rewardService.accrue(userId, points);
            }
        }

        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getHistory(Long accountId, Long userId, Pageable pageable) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));

        if (!account.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Access denied to account: " + accountId);
        }

        return transactionRepository.findByAccountIdOrderByTransactionDateDesc(accountId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getAllForUser(Long userId, LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        if (startDate != null && endDate != null) {
            // We need a repository method for this or use a specification
            // For simplicity, let's assume we add findByAccountUserIdAndTransactionDateBetween
            return transactionRepository.findByAccountUserIdAndTransactionDateBetween(userId, startDate, endDate, pageable)
                    .map(this::mapToResponse);
        }
        return transactionRepository.findByAccountUserIdOrderByTransactionDateDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Map<String, java.math.BigDecimal> getSpendingSummary(Long userId) {
        LocalDateTime start = LocalDateTime.now().withDayOfMonth(1).withHour(0).withMinute(0).withSecond(0).withNano(0);
        LocalDateTime end = LocalDateTime.now();

        List<Transaction> txns = transactionRepository.findByAccountUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(userId, start, end);

        return txns.stream()
                .filter(t -> t.getType() == TransactionType.DEBIT)
                .collect(Collectors.groupingBy(
                        t -> categorize(t.getDescription()),
                        Collectors.reducing(
                                java.math.BigDecimal.ZERO,
                                Transaction::getAmount,
                                java.math.BigDecimal::add
                        )
                ));
    }

    private String categorize(String description) {
        if (description == null) return "OTHER";
        String desc = description.toLowerCase();
        if (desc.contains("grocery") || desc.contains("mart") || desc.contains("food")) return "GROCERIES";
        if (desc.contains("utility") || desc.contains("bill") || desc.contains("electricity")) return "UTILITIES";
        if (desc.contains("rent") || desc.contains("housing")) return "RENT";
        if (desc.contains("movie") || desc.contains("netflix") || desc.contains("game")) return "ENTERTAINMENT";
        if (desc.contains("transfer") || desc.contains("send")) return "TRANSFER";
        return "OTHER";
    }

    private TransactionResponse mapToResponse(Transaction txn) {
        TransactionResponse r = new TransactionResponse();
        r.setId(txn.getId());
        r.setAccountId(txn.getAccount().getId());
        r.setAccountNumber(txn.getAccount().getAccountNumber());
        r.setAccountType(txn.getAccount().getAccountType());
        r.setType(txn.getType());
        r.setAmount(txn.getAmount());
        r.setDescription(txn.getDescription());
        r.setTransactionDate(txn.getTransactionDate());
        r.setBalanceAfter(txn.getBalanceAfter());
        return r;
    }
}

