-- Sprint 3: Loan Management
-- 08_create_loan_applications_table.sql

CREATE TABLE IF NOT EXISTS loan_applications (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    loan_product_id BIGINT NOT NULL,
    requested_amount DECIMAL(15, 2) NOT NULL,
    status ENUM('PENDING', 'APPROVED', 'REJECTED') NOT NULL DEFAULT 'PENDING',
    admin_remarks TEXT,
    applied_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    decision_at TIMESTAMP NULL,
    CONSTRAINT fk_loan_app_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
    CONSTRAINT fk_loan_app_product FOREIGN KEY (loan_product_id) REFERENCES loan_products(id) ON DELETE RESTRICT
) ENGINE=InnoDB;
