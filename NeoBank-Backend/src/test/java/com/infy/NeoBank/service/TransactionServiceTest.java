package com.infy.NeoBank.service;

import com.infy.NeoBank.dto.request.TransactionRequest;
import com.infy.NeoBank.dto.response.TransactionResponse;
import com.infy.NeoBank.entity.Account;
import com.infy.NeoBank.entity.Transaction;
import com.infy.NeoBank.entity.User;
import com.infy.NeoBank.enums.AccountType;
import com.infy.NeoBank.enums.TransactionType;
import com.infy.NeoBank.exception.AccessDeniedException;
import com.infy.NeoBank.exception.InsufficientBalanceException;
import com.infy.NeoBank.exception.ResourceNotFoundException;
import com.infy.NeoBank.repository.AccountRepository;
import com.infy.NeoBank.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TransactionServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private RewardService rewardService;

    @InjectMocks
    private TransactionService transactionService;

    private User user;
    private Account account;
    private TransactionRequest creditRequest;
    private TransactionRequest debitRequest;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("test@example.com");

        account = new Account();
        account.setId(1L);
        account.setUser(user);
        account.setBalance(new BigDecimal("1000.00"));
        account.setAccountNumber("1234567890");
        account.setAccountType(AccountType.SAVINGS);

        creditRequest = new TransactionRequest();
        creditRequest.setType(TransactionType.CREDIT);
        creditRequest.setAmount(new BigDecimal("500.00"));
        creditRequest.setDescription("Salary");

        debitRequest = new TransactionRequest();
        debitRequest.setType(TransactionType.DEBIT);
        debitRequest.setAmount(new BigDecimal("200.00"));
        debitRequest.setDescription("Grocery Mart");
    }

    @Test
    void create_SuccessfulCredit_ReturnsResponse() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
            Transaction t = i.getArgument(0);
            t.setId(1L);
            return t;
        });

        TransactionResponse response = transactionService.create(1L, 1L, creditRequest);

        assertNotNull(response);
        assertEquals(new BigDecimal("1500.00"), account.getBalance());
        assertEquals(TransactionType.CREDIT, response.getType());
        verify(accountRepository).save(account);
        verify(rewardService, never()).accrue(anyLong(), anyInt());
    }

    @Test
    void create_SuccessfulDebit_ReturnsResponseAndAccruesRewards() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> {
            Transaction t = i.getArgument(0);
            t.setId(2L);
            return t;
        });

        TransactionResponse response = transactionService.create(1L, 1L, debitRequest);

        assertNotNull(response);
        assertEquals(new BigDecimal("800.00"), account.getBalance());
        assertEquals(TransactionType.DEBIT, response.getType());
        verify(accountRepository).save(account);
        verify(rewardService).accrue(1L, 20);
    }

    @Test
    void create_AccountNotFound_ThrowsException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> transactionService.create(1L, 1L, creditRequest));
    }

    @Test
    void create_AccessDenied_ThrowsException() {
        User otherUser = new User();
        otherUser.setId(2L);
        account.setUser(otherUser);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(AccessDeniedException.class, () -> transactionService.create(1L, 1L, creditRequest));
    }

    @Test
    void create_InsufficientBalance_ThrowsException() {
        debitRequest.setAmount(new BigDecimal("2000.00"));
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(InsufficientBalanceException.class, () -> transactionService.create(1L, 1L, debitRequest));
    }

    @Test
    void getHistory_Success_ReturnsPage() {
        Pageable pageable = PageRequest.of(0, 10);
        Transaction txn = new Transaction();
        txn.setId(1L);
        txn.setAccount(account);
        txn.setType(TransactionType.CREDIT);
        txn.setAmount(new BigDecimal("100.00"));
        Page<Transaction> page = new PageImpl<>(List.of(txn));

        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));
        when(transactionRepository.findByAccountIdOrderByTransactionDateDesc(1L, pageable)).thenReturn(page);

        Page<TransactionResponse> result = transactionService.getHistory(1L, 1L, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(TransactionType.CREDIT, result.getContent().get(0).getType());
    }

    @Test
    void getHistory_AccountNotFound_ThrowsException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> transactionService.getHistory(1L, 1L, PageRequest.of(0, 10)));
    }

    @Test
    void getHistory_AccessDenied_ThrowsException() {
        User otherUser = new User();
        otherUser.setId(2L);
        account.setUser(otherUser);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(account));

        assertThrows(AccessDeniedException.class, () -> transactionService.getHistory(1L, 1L, PageRequest.of(0, 10)));
    }

    @Test
    void getAllForUser_WithDateRange_ReturnsPage() {
        LocalDateTime start = LocalDateTime.now().minusDays(7);
        LocalDateTime end = LocalDateTime.now();
        Pageable pageable = PageRequest.of(0, 10);
        Transaction txn = new Transaction();
        txn.setId(1L);
        txn.setAccount(account);
        Page<Transaction> page = new PageImpl<>(List.of(txn));

        when(transactionRepository.findByAccountUserIdAndTransactionDateBetween(1L, start, end, pageable)).thenReturn(page);

        Page<TransactionResponse> result = transactionService.getAllForUser(1L, start, end, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    @Test
    void getSpendingSummary_Success_ReturnsMap() {
        Transaction t1 = new Transaction();
        t1.setType(TransactionType.DEBIT);
        t1.setAmount(new BigDecimal("50.00"));
        t1.setDescription("Grocery Mart");

        Transaction t2 = new Transaction();
        t2.setType(TransactionType.DEBIT);
        t2.setAmount(new BigDecimal("30.00"));
        t2.setDescription("Netflix");

        Transaction t3 = new Transaction();
        t3.setType(TransactionType.CREDIT);
        t3.setAmount(new BigDecimal("100.00"));
        t3.setDescription("Salary");

        when(transactionRepository.findByAccountUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(anyLong(), any(), any()))
                .thenReturn(List.of(t1, t2, t3));

        Map<String, BigDecimal> summary = transactionService.getSpendingSummary(1L);

        assertNotNull(summary);
        assertEquals(new BigDecimal("50.00"), summary.get("GROCERIES"));
        assertEquals(new BigDecimal("30.00"), summary.get("ENTERTAINMENT"));
        assertNull(summary.get("OTHER"));
    }
}
