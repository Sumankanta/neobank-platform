package com.infy.NeoBank.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Entity
@Table(name = "loan_products")
public class LoanProduct {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "reference_amount", nullable = false)
    private BigDecimal referenceAmount;

    @Column(name = "min_amount", nullable = false)
    private BigDecimal minAmount = new BigDecimal("1000");

    @Column(name = "max_amount", nullable = false)
    private BigDecimal maxAmount = new BigDecimal("1000000");

    @Column(name = "annual_interest_rate", nullable = false)
    private BigDecimal annualInterestRate;

    @Column(name = "duration_months", nullable = false)
    private Integer durationMonths;

    @Column(name = "min_tenure_months", nullable = false)
    private Integer minTenureMonths = 6;

    @Column(name = "max_tenure_months", nullable = false)
    private Integer maxTenureMonths = 60;

    @Column(name = "description")
    private String description;
}
