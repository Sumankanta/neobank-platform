-- Sprint 3: Loan Management
-- 07_create_loan_products_table.sql

CREATE TABLE IF NOT EXISTS loan_products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    product_name VARCHAR(100) NOT NULL,
    reference_amount DECIMAL(15, 2) NOT NULL,
    annual_interest_rate DECIMAL(5, 2) NOT NULL,
    duration_months INT NOT NULL,
    description TEXT
) ENGINE=InnoDB;
