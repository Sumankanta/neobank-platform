-- Sprint 1: Foundation & Core Banking
-- 03_create_transactions_table.sql

CREATE TABLE IF NOT EXISTS transactions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    account_id BIGINT NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    type ENUM('CREDIT', 'DEBIT') NOT NULL,
    category VARCHAR(50) DEFAULT 'GENERAL',
    description VARCHAR(255),
    balance_after DECIMAL(15, 2),
    transaction_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_transactions_account FOREIGN KEY (account_id) REFERENCES accounts(id) ON DELETE RESTRICT,
    INDEX idx_trans_account (account_id),
    INDEX idx_trans_timestamp (transaction_date)
) ENGINE=InnoDB;
