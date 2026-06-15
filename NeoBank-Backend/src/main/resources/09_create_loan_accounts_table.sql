-- Sprint 3: Loan Management
-- 09_create_loan_accounts_table.sql

CREATE TABLE IF NOT EXISTS loan_accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    loan_product_id BIGINT NOT NULL,
    principal_amount DECIMAL(15, 2) NOT NULL,
    remaining_balance DECIMAL(15, 2) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    CONSTRAINT fk_loan_acc_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_loan_acc_product FOREIGN KEY (loan_product_id) REFERENCES loan_products(id) ON DELETE RESTRICT
) ENGINE=InnoDB;
