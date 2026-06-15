package com.infy.NeoBank.service;

import com.infy.NeoBank.dto.request.AccountRequest;
import com.infy.NeoBank.dto.response.AccountResponse;
import com.infy.NeoBank.entity.Account;
import com.infy.NeoBank.entity.User;
import com.infy.NeoBank.enums.AccountType;
import com.infy.NeoBank.exception.AccessDeniedException;
import com.infy.NeoBank.exception.ResourceNotFoundException;
import com.infy.NeoBank.repository.AccountRepository;
import com.infy.NeoBank.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private AccountService accountService;

    private User testUser;
    private Account testAccount;
    private AccountRequest accountRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("test@example.com");

        testAccount = new Account();
        testAccount.setId(1L);
        testAccount.setUser(testUser);
        testAccount.setAccountNumber("NB123456789012");
        testAccount.setAccountType(AccountType.SAVINGS);
        testAccount.setBalance(BigDecimal.ZERO);

        accountRequest = new AccountRequest();
        accountRequest.setAccountType(AccountType.SAVINGS);
    }

    @Test
    void createAccount_Success() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        AccountResponse response = accountService.create(1L, accountRequest);

        assertNotNull(response);
        assertEquals(testAccount.getAccountNumber(), response.getAccountNumber());
        verify(accountRepository).save(any(Account.class));
    }

    @Test
    void createAccount_UserNotFound_ThrowsException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.create(1L, accountRequest));
        verify(accountRepository, never()).save(any(Account.class));
    }

    @Test
    void getAll_Success_ReturnsList() {
        when(accountRepository.findByUserId(1L)).thenReturn(List.of(testAccount));

        List<AccountResponse> responses = accountService.getAll(1L);

        assertFalse(responses.isEmpty());
        assertEquals(1, responses.size());
        assertEquals(testAccount.getAccountNumber(), responses.get(0).getAccountNumber());
    }

    @Test
    void getAll_EmptyList_ReturnsEmptyList() {
        when(accountRepository.findByUserId(1L)).thenReturn(List.of());

        List<AccountResponse> responses = accountService.getAll(1L);

        assertTrue(responses.isEmpty());
    }

    @Test
    void getById_Success() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        AccountResponse response = accountService.getById(1L, 1L);

        assertNotNull(response);
        assertEquals(testAccount.getAccountNumber(), response.getAccountNumber());
    }

    @Test
    void getById_NotFound_ThrowsException() {
        when(accountRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> accountService.getById(1L, 1L));
    }

    @Test
    void getById_AccessDenied_ThrowsException() {
        testAccount.getUser().setId(2L);
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        assertThrows(AccessDeniedException.class, () -> accountService.getById(1L, 1L));
    }

    @Test
    void createAccount_VerifySaveCalled() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        when(accountRepository.save(any(Account.class))).thenReturn(testAccount);

        accountService.create(1L, accountRequest);

        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void getAll_VerifyRepositoryCalled() {
        when(accountRepository.findByUserId(1L)).thenReturn(List.of(testAccount));

        accountService.getAll(1L);

        verify(accountRepository, times(1)).findByUserId(1L);
    }

    @Test
    void getById_VerifyRepositoryCalled() {
        when(accountRepository.findById(1L)).thenReturn(Optional.of(testAccount));

        accountService.getById(1L, 1L);

        verify(accountRepository, times(1)).findById(1L);
    }
}
