-- Sprint 1: Foundation & Core Banking
-- 02_create_accounts_table.sql

CREATE TABLE IF NOT EXISTS accounts (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    account_number VARCHAR(20) NOT NULL UNIQUE,
    balance DECIMAL(15, 2) NOT NULL DEFAULT 0.00,
    account_type ENUM('SAVINGS', 'CURRENT', 'CHECKING', 'FIXED_DEPOSIT', 'SALARIED') NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_accounts_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    INDEX idx_acc_number (account_number),
    INDEX idx_acc_user (user_id)
) ENGINE=InnoDB;
