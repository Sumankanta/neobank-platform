-- 11_seed_data.sql

-- 1. Users (Passwords: Password@123)
INSERT INTO users (email, password_hash, full_name, role, is_active, created_at) VALUES
('admin@neobank.in', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00dmxs.TVuHOnu', 'System Admin', 'ADMIN', TRUE, CURRENT_TIMESTAMP),
('customer1@neobank.in', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00dmxs.TVuHOnu', 'John Doe', 'CUSTOMER', TRUE, CURRENT_TIMESTAMP),
('customer2@neobank.in', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00dmxs.TVuHOnu', 'Jane Smith', 'CUSTOMER', TRUE, CURRENT_TIMESTAMP),
('customer3@neobank.in', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00dmxs.TVuHOnu', 'Bob Wilson', 'CUSTOMER', TRUE, CURRENT_TIMESTAMP),
('inactive@neobank.in', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00dmxs.TVuHOnu', 'Old User', 'INACTIVE', FALSE, CURRENT_TIMESTAMP);

-- 2. Bank Accounts
INSERT INTO accounts (user_id, account_number, balance, account_type, created_at) VALUES
(2, 'NB1001', 5000.00, 'SAVINGS', CURRENT_TIMESTAMP),
(2, 'NB1002', 1500.00, 'CHECKING', CURRENT_TIMESTAMP),
(3, 'NB2001', 12000.00, 'SAVINGS', CURRENT_TIMESTAMP),
(3, 'NB2002', 500.00, 'CHECKING', CURRENT_TIMESTAMP),
(4, 'NB3001', 25000.00, 'SAVINGS', CURRENT_TIMESTAMP),
(4, 'NB3002', 8000.00, 'FIXED_DEPOSIT', CURRENT_TIMESTAMP);

-- 3. Transactions (Budget related)
INSERT INTO transactions (account_id, amount, type, category, description, transaction_date) VALUES
(1, 200.00, 'DEBIT', 'GROCERIES', 'Weekly Grocery Shopping', CURRENT_TIMESTAMP),
(1, 1500.00, 'DEBIT', 'RENT', 'Monthly Rent Payment', CURRENT_TIMESTAMP),
(2, 50.00, 'DEBIT', 'ENTERTAINMENT', 'Netflix Subscription', CURRENT_TIMESTAMP),
(3, 5000.00, 'CREDIT', 'GENERAL', 'Salary Credit', CURRENT_TIMESTAMP),
(3, 100.00, 'DEBIT', 'UTILITIES', 'Electricity Bill', CURRENT_TIMESTAMP),
(4, 300.00, 'DEBIT', 'SHOPPING', 'New Shoes', CURRENT_TIMESTAMP),
(5, 1000.00, 'CREDIT', 'GENERAL', 'Transfer from Savings', CURRENT_TIMESTAMP),
(5, 50.00, 'DEBIT', 'HEALTH', 'Pharmacy', CURRENT_TIMESTAMP),
(1, 45.00, 'DEBIT', 'TRAVEL', 'Uber Ride', CURRENT_TIMESTAMP),
(2, 20.00, 'DEBIT', 'OTHER', 'Coffee', CURRENT_TIMESTAMP),
(3, 2000.00, 'DEBIT', 'RENT', 'House Rent', CURRENT_TIMESTAMP),
(4, 150.00, 'DEBIT', 'GROCERIES', 'Market Visit', CURRENT_TIMESTAMP);

-- 4. Budgets
INSERT INTO budgets (user_id, category, budget_limit, spent_amount, budget_month, created_at) VALUES
(2, 'GROCERIES', 500.00, 200.00, '2026-06-01', CURRENT_TIMESTAMP),
(2, 'RENT', 1500.00, 1500.00, '2026-06-01', CURRENT_TIMESTAMP),
(3, 'ENTERTAINMENT', 200.00, 50.00, '2026-06-01', CURRENT_TIMESTAMP),
(3, 'UTILITIES', 300.00, 100.00, '2026-06-01', CURRENT_TIMESTAMP),
(4, 'SHOPPING', 1000.00, 300.00, '2026-06-01', CURRENT_TIMESTAMP),
(4, 'TRAVEL', 500.00, 0.00, '2026-06-01', CURRENT_TIMESTAMP);

-- 5. Bills (One due within 3 days: Current date is June 13, 2026)
INSERT INTO bills (user_id, biller_name, amount, due_date, status, created_at) VALUES
(2, 'Electric Bill', 120.00, '2026-06-15', 'PENDING', CURRENT_TIMESTAMP),
(2, 'Water Bill', 45.00, '2026-06-10', 'PAID', CURRENT_TIMESTAMP),
(3, 'Internet', 80.00, '2026-06-25', 'PENDING', CURRENT_TIMESTAMP),
(3, 'Gym Membership', 50.00, '2026-06-01', 'OVERDUE', CURRENT_TIMESTAMP),
(4, 'Insurance', 500.00, '2026-07-01', 'PENDING', CURRENT_TIMESTAMP),
(4, 'Phone Bill', 60.00, '2026-06-12', 'PAID', CURRENT_TIMESTAMP);

-- 6. Rewards
INSERT INTO rewards (user_id, points_balance) VALUES
(2, 500),
(3, 1250),
(4, 3000);

-- 7. Loan Products
INSERT INTO loan_products (product_name, reference_amount, annual_interest_rate, duration_months, description) VALUES
('Personal Loan Gold', 10000.00, 12.50, 24, 'Low interest personal loan for trusted customers'),
('Home Starter Loan', 50000.00, 8.75, 60, 'Perfect for first-time home buyers'),
('Quick Cash Micro', 1000.00, 15.00, 6, 'Small emergency loan with instant approval');

-- 8. Loan Applications
INSERT INTO loan_applications (user_id, loan_product_id, requested_amount, status) VALUES
(2, 1, 5000.00, 'APPROVED'),
(3, 2, 45000.00, 'PENDING'),
(4, 3, 1000.00, 'REJECTED');

-- 9. Loan Accounts (For customer 1 whose app was approved)
INSERT INTO loan_accounts (user_id, loan_product_id, principal_amount, remaining_balance, start_date, end_date) VALUES
(2, 1, 5000.00, 5000.00, '2026-06-01', '2028-06-01');
