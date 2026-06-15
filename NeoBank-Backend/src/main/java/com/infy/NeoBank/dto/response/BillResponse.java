package com.infy.NeoBank.dto.response;

import com.infy.NeoBank.enums.BillStatus;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BillResponse {

    private Long id;
    private String billerName;
    private BigDecimal amount;
    private LocalDate dueDate;
    private BillStatus status;
    private boolean remindMe;
    private LocalDateTime createdAt;
}
