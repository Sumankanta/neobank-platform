-- Sprint 3: Loan Management
-- 10_create_loan_repayments_table.sql

CREATE TABLE IF NOT EXISTS loan_repayments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    loan_account_id BIGINT NOT NULL,
    installment_number INT NOT NULL,
    principal_repaid DECIMAL(15, 2) NOT NULL,
    interest_repaid DECIMAL(15, 2) NOT NULL,
    payment_status ENUM('PENDING', 'COMPLETED', 'FAILED') NOT NULL DEFAULT 'PENDING',
    paid_at TIMESTAMP NULL,
    complete_status BOOLEAN DEFAULT FALSE,
    CONSTRAINT fk_repayment_loan_acc FOREIGN KEY (loan_account_id) REFERENCES loan_accounts(id) ON DELETE CASCADE
) ENGINE=InnoDB;
