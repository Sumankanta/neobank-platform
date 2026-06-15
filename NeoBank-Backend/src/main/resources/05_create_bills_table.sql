-- Sprint 2: Budgeting, Bills & Rewards
-- 05_create_bills_table.sql

CREATE TABLE IF NOT EXISTS bills (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    bill_name VARCHAR(100) NOT NULL,
    amount DECIMAL(15, 2) NOT NULL,
    due_date DATE NOT NULL,
    status ENUM('PENDING', 'PAID', 'OVERDUE') NOT NULL DEFAULT 'PENDING',
    CONSTRAINT fk_bills_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_bill_name_user (user_id, bill_name),
    INDEX idx_bill_status (status),
    INDEX idx_bill_due_date (due_date)
) ENGINE=InnoDB;
