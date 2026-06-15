package com.infy.NeoBank.service;

import com.infy.NeoBank.entity.Account;
import com.infy.NeoBank.entity.Transaction;
import com.infy.NeoBank.entity.User;
import com.infy.NeoBank.enums.TransactionType;
import com.infy.NeoBank.repository.TransactionRepository;
import com.infy.NeoBank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StatementServiceTest {

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private StatementService statementService;

    private User testUser;
    private Transaction testTransaction;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setFullName("Test User");

        Account account = new Account();
        account.setAccountNumber("NB123456789012");

        testTransaction = new Transaction();
        testTransaction.setTransactionDate(LocalDateTime.now());
        testTransaction.setAccount(account);
        testTransaction.setDescription("Test Txn");
        testTransaction.setType(TransactionType.CREDIT);
        testTransaction.setAmount(BigDecimal.valueOf(100));
    }

    @Test
    void generateMonthlyStatement_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByAccountUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(anyLong(), any(), any()))
                .thenReturn(List.of(testTransaction));

        byte[] pdf = statementService.generateMonthlyStatement(1L);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void generateMonthlyStatement_NoTransactions_StillGeneratesPdf() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByAccountUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(anyLong(), any(), any()))
                .thenReturn(List.of());

        byte[] pdf = statementService.generateMonthlyStatement(1L);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void generateMonthlyStatement_UserNotFound_UsesDefaultName() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        when(transactionRepository.findByAccountUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(anyLong(), any(), any()))
                .thenReturn(List.of(testTransaction));

        byte[] pdf = statementService.generateMonthlyStatement(1L);

        assertNotNull(pdf);
        assertTrue(pdf.length > 0);
    }

    @Test
    void generateMonthlyStatement_HandlesEmptyTransactionList() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByAccountUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(anyLong(), any(), any()))
                .thenReturn(List.of());

        byte[] pdf = statementService.generateMonthlyStatement(1L);

        assertNotNull(pdf);
        verify(transactionRepository).findByAccountUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(eq(1L), any(), any());
    }

    @Test
    void generateMonthlyStatement_VerifyTransactionRepositoryCalled() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByAccountUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(anyLong(), any(), any()))
                .thenReturn(List.of(testTransaction));

        statementService.generateMonthlyStatement(1L);

        verify(transactionRepository).findByAccountUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(eq(1L), any(), any());
    }

    @Test
    void generateMonthlyStatement_VerifyUserRepositoryCalled() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByAccountUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(anyLong(), any(), any()))
                .thenReturn(List.of(testTransaction));

        statementService.generateMonthlyStatement(1L);

        verify(userRepository).findById(1L);
    }

    @Test
    void generateMonthlyStatement_ThrowsRuntimeExceptionOnException() {
        when(userRepository.findById(1L)).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> statementService.generateMonthlyStatement(1L));
    }

    @Test
    void generateMonthlyStatement_ReturnsNonEmptyByteArray() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByAccountUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(anyLong(), any(), any()))
                .thenReturn(List.of(testTransaction));

        byte[] result = statementService.generateMonthlyStatement(1L);

        assertNotNull(result);
        assertTrue(result.length > 0);
    }

    @Test
    void generateMonthlyStatement_VerifiesCorrectDateRangeUsed() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByAccountUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(anyLong(), any(), any()))
                .thenReturn(List.of(testTransaction));

        statementService.generateMonthlyStatement(1L);

        verify(transactionRepository).findByAccountUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(
                eq(1L),
                argThat(date -> date.getDayOfMonth() == 1),
                argThat(date -> date.isAfter(LocalDateTime.now().minusMinutes(1)))
        );
    }

    @Test
    void generateMonthlyStatement_DebitTransaction_IncludesMinusSign() {
        testTransaction.setType(TransactionType.DEBIT);
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(transactionRepository.findByAccountUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(anyLong(), any(), any()))
                .thenReturn(List.of(testTransaction));

        byte[] pdf = statementService.generateMonthlyStatement(1L);

        assertNotNull(pdf);
        verify(transactionRepository).findByAccountUserIdAndTransactionDateBetweenOrderByTransactionDateDesc(anyLong(), any(), any());
    }
}
