package com.infy.NeoBank.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "loan_repayments")
public class LoanRepayment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loan_account_id", nullable = false)
    private LoanAccount loanAccount;

    @Column(name = "installment_number", nullable = false)
    private Integer installmentNumber;

    @Column(name = "principal_repaid", nullable = false)
    private BigDecimal principalRepaid;

    @Column(name = "interest_repaid", nullable = false)
    private BigDecimal interestRepaid;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private RepaymentStatus status = RepaymentStatus.PENDING;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    public enum RepaymentStatus {
        PENDING, COMPLETED, FAILED
    }
}
