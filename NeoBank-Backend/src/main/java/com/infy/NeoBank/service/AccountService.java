package com.infy.NeoBank.service;

import com.infy.NeoBank.dto.request.AccountRequest;
import com.infy.NeoBank.dto.response.AccountResponse;
import com.infy.NeoBank.entity.Account;
import com.infy.NeoBank.entity.User;
import com.infy.NeoBank.exception.AccessDeniedException;
import com.infy.NeoBank.exception.ResourceNotFoundException;
import com.infy.NeoBank.repository.AccountRepository;
import com.infy.NeoBank.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AccountService {

    private final AccountRepository accountRepository;
    private final UserRepository userRepository;

    public AccountResponse create(Long userId, AccountRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));

        Account account = new Account();
        account.setUser(user);
        account.setAccountType(request.getAccountType());
        account.setAccountNumber(generateAccountNumber());

        return mapToResponse(accountRepository.save(account));
    }

    public List<AccountResponse> getAll(Long userId) {
        return accountRepository.findByUserId(userId).stream()
                .map(this::mapToResponse)
                .toList();
    }

    public AccountResponse getById(Long accountId, Long userId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found: " + accountId));
        if (!account.getUser().getId().equals(userId)) {
            throw new AccessDeniedException("Access denied to account: " + accountId);
        }
        return mapToResponse(account);
    }

    private String generateAccountNumber() {
        return "NB" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }

    private AccountResponse mapToResponse(Account account) {
        AccountResponse r = new AccountResponse();
        r.setId(account.getId());
        r.setAccountNumber(account.getAccountNumber());
        r.setAccountType(account.getAccountType());
        r.setBalance(account.getBalance());
        r.setCreatedAt(account.getCreatedAt());
        return r;
    }
}
