package com.infy.NeoBank.dto.response;

import com.infy.NeoBank.enums.AccountType;
import com.infy.NeoBank.enums.TransactionType;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class TransactionResponse {

    private Long id;
    private Long accountId;
    private String accountNumber;
    private AccountType accountType;
    private TransactionType type;
    private BigDecimal amount;
    private String description;
    private String category;
    private BigDecimal balanceAfter;
    private LocalDateTime transactionDate;
}
