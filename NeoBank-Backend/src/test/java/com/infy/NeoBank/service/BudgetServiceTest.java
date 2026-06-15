package com.infy.NeoBank.service;

import com.infy.NeoBank.dto.request.BudgetRequest;
import com.infy.NeoBank.dto.response.BudgetSummaryResponse;
import com.infy.NeoBank.entity.Account;
import com.infy.NeoBank.entity.Budget;
import com.infy.NeoBank.entity.User;
import com.infy.NeoBank.enums.BudgetCategory;
import com.infy.NeoBank.exception.AccessDeniedException;
import com.infy.NeoBank.exception.DuplicateResourceException;
import com.infy.NeoBank.exception.ResourceNotFoundException;
import com.infy.NeoBank.repository.AccountRepository;
import com.infy.NeoBank.repository.BudgetRepository;
import com.infy.NeoBank.repository.TransactionRepository;
import com.infy.NeoBank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BudgetServiceTest {

    @Mock
    private BudgetRepository budgetRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private AccountRepository accountRepository;
    @Mock
    private TransactionRepository transactionRepository;

    @InjectMocks
    private BudgetService budgetService;

    private User testUser;
    private Budget testBudget;
    private BudgetRequest budgetRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);

        testBudget = new Budget();
        testBudget.setId(1L);
        testBudget.setUser(testUser);
        testBudget.setCategory(BudgetCategory.GROCERIES);
        testBudget.setBudgetMonth(LocalDate.of(2026, 6, 1));
        testBudget.setBudgetLimit(BigDecimal.valueOf(500));

        budgetRequest = new BudgetRequest();
        budgetRequest.setCategory(BudgetCategory.GROCERIES);
        budgetRequest.setBudgetMonth("2026-06");
        budgetRequest.setBudgetLimit(BigDecimal.valueOf(500));
    }

    @Test
    void create_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findByUserIdAndCategoryAndBudgetLimit(anyLong(), any(), any())).thenReturn(Optional.empty());
        when(budgetRepository.save(any(Budget.class))).thenReturn(testBudget);

        BudgetSummaryResponse response = budgetService.create(1L, budgetRequest);

        assertNotNull(response);
        assertEquals(testBudget.getCategory(), response.getCategory());
        verify(budgetRepository).save(any(Budget.class));
    }

    @Test
    void create_UserNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> budgetService.create(1L, budgetRequest));
    }

    @Test
    void create_DuplicateBudget_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(budgetRepository.findByUserIdAndCategoryAndBudgetLimit(anyLong(), any(), any())).thenReturn(Optional.of(testBudget));

        assertThrows(DuplicateResourceException.class, () -> budgetService.create(1L, budgetRequest));
    }

    @Test
    void getSummary_Success() {
        when(budgetRepository.findByUserIdAndBudgetMonth(anyLong(), any())).thenReturn(List.of(testBudget));
        when(accountRepository.findByUserId(1L)).thenReturn(List.of());

        List<BudgetSummaryResponse> responses = budgetService.getSummary(1L, "2026-06", 1L);

        assertEquals(1, responses.size());
        assertEquals(testBudget.getCategory(), responses.get(0).getCategory());
    }

    @Test
    void getSummary_AccessDenied_ThrowsException() {
        assertThrows(AccessDeniedException.class, () -> budgetService.getSummary(1L, "2026-06", 2L));
    }

    @Test
    void getAll_Success() {
        when(budgetRepository.findByUserId(1L)).thenReturn(List.of(testBudget));

        List<BudgetSummaryResponse> responses = budgetService.getAll(1L);

        assertEquals(1, responses.size());
    }

    @Test
    void delete_Success() {
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(testBudget));

        budgetService.delete(1L, 1L);

        verify(budgetRepository).delete(testBudget);
    }

    @Test
    void delete_NotFound_ThrowsException() {
        when(budgetRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> budgetService.delete(1L, 1L));
    }

    @Test
    void delete_AccessDenied_ThrowsException() {
        testBudget.getUser().setId(2L);
        when(budgetRepository.findById(1L)).thenReturn(Optional.of(testBudget));

        assertThrows(AccessDeniedException.class, () -> budgetService.delete(1L, 1L));
    }

    @Test
    void calculateSpent_VerifyCorrectCalculation() {
        Account account = new Account();
        account.setId(1L);
        when(budgetRepository.findByUserIdAndBudgetMonth(anyLong(), any())).thenReturn(List.of(testBudget));
        when(accountRepository.findByUserId(1L)).thenReturn(List.of(account));
        when(transactionRepository.findByAccountIdAndTypeAndTransactionDateBetween(anyLong(), any(), any(), any())).thenReturn(List.of());

        List<BudgetSummaryResponse> responses = budgetService.getSummary(1L, "2026-06", 1L);

        assertEquals(BigDecimal.ZERO, responses.get(0).getSpentAmount());
        verify(transactionRepository).findByAccountIdAndTypeAndTransactionDateBetween(anyLong(), any(), any(), any());
    }
}
