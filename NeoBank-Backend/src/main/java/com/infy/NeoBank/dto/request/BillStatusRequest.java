package com.infy.NeoBank.dto.request;

import com.infy.NeoBank.enums.BillStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class BillStatusRequest {

    @NotNull
    private BillStatus status;
}
