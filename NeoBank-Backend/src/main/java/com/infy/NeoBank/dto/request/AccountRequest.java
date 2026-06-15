package com.infy.NeoBank.dto.request;

import com.infy.NeoBank.enums.AccountType;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AccountRequest {

    @NotNull
    private AccountType accountType;
}
