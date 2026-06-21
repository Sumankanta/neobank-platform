-- Sprint 2: Budgeting, Bills & Rewards
-- 06_create_rewards_table.sql

CREATE TABLE IF NOT EXISTS rewards (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL UNIQUE,
    points_balance INT NOT NULL DEFAULT 0,
    CONSTRAINT fk_rewards_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT chk_rewards_points CHECK (points_balance >= 0)
) ENGINE=InnoDB;
