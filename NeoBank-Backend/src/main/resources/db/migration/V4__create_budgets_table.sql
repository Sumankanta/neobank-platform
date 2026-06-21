-- Sprint 2: Budgeting, Bills & Rewards
-- 04_create_budgets_table.sql

CREATE TABLE IF NOT EXISTS budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    category ENUM('GROCERIES', 'RENT', 'UTILITIES', 'ENTERTAINMENT', 'SHOPPING', 'HEALTH', 'TRAVEL', 'OTHER') NOT NULL,
    budget_limit DECIMAL(15, 2) NOT NULL,
    spent_amount DECIMAL(15, 2) DEFAULT 0.00,
    budget_month DATE NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_budgets_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    UNIQUE KEY uk_user_category_limit (user_id, category, budget_limit),
    INDEX idx_budget_user_month (user_id, budget_month)
) ENGINE=InnoDB;
