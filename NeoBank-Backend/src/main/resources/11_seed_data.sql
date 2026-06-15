-- 11_seed_data.sql

-- 1. Users (Passwords: Password@123)
INSERT INTO users (email, password_hash, full_name, role, is_active) VALUES
('admin@neobank.in', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00dmxs.TVuHOnu', 'System Admin', 'ADMIN', TRUE),
('customer1@neobank.in', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00dmxs.TVuHOnu', 'John Doe', 'CUSTOMER', TRUE),
('customer2@neobank.in', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00dmxs.TVuHOnu', 'Jane Smith', 'CUSTOMER', TRUE),
('customer3@neobank.in', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00dmxs.TVuHOnu', 'Bob Wilson', 'CUSTOMER', TRUE),
('inactive@neobank.in', '$2a$10$8.UnVuG9HHgffUDAlk8qfOuVGkqRzgVymGe07xd00dmxs.TVuHOnu', 'Old User', 'INACTIVE', FALSE);

-- 2. Bank Accounts
INSERT INTO accounts (user_id, account_number, balance, account_type) VALUES
(2, 'NB1001', 5000.00, 'SAVINGS'),
(2, 'NB1002', 1500.00, 'CHECKING'),
(3, 'NB2001', 12000.00, 'SAVINGS'),
(3, 'NB2002', 500.00, 'CHECKING'),
(4, 'NB3001', 25000.00, 'SAVINGS'),
(4, 'NB3002', 8000.00, 'FIXED_DEPOSIT');

-- 3. Transactions (Budget related)
INSERT INTO transactions (account_id, amount, type, category, description) VALUES
(1, 200.00, 'DEBIT', 'GROCERIES', 'Weekly Grocery Shopping'),
(1, 1500.00, 'DEBIT', 'RENT', 'Monthly Rent Payment'),
(2, 50.00, 'DEBIT', 'ENTERTAINMENT', 'Netflix Subscription'),
(3, 5000.00, 'CREDIT', 'GENERAL', 'Salary Credit'),
(3, 100.00, 'DEBIT', 'UTILITIES', 'Electricity Bill'),
(4, 300.00, 'DEBIT', 'SHOPPING', 'New Shoes'),
(5, 1000.00, 'CREDIT', 'GENERAL', 'Transfer from Savings'),
(5, 50.00, 'DEBIT', 'HEALTH', 'Pharmacy'),
(1, 45.00, 'DEBIT', 'TRAVEL', 'Uber Ride'),
(2, 20.00, 'DEBIT', 'OTHER', 'Coffee'),
(3, 2000.00, 'DEBIT', 'RENT', 'House Rent'),
(4, 150.00, 'DEBIT', 'GROCERIES', 'Market Visit');

-- 4. Budgets
INSERT INTO budgets (user_id, category, budget_limit, spent_amount, budget_month) VALUES
(2, 'GROCERIES', 500.00, 200.00, '2026-06'),
(2, 'RENT', 1500.00, 1500.00, '2026-06'),
(3, 'ENTERTAINMENT', 200.00, 50.00, '2026-06'),
(3, 'UTILITIES', 300.00, 100.00, '2026-06'),
(4, 'SHOPPING', 1000.00, 300.00, '2026-06'),
(4, 'TRAVEL', 500.00, 0.00, '2026-06');

-- 5. Bills (One due within 3 days: Current date is June 13, 2026)
INSERT INTO bills (user_id, bill_name, amount, due_date, status) VALUES
(2, 'Electric Bill', 120.00, '2026-06-15', 'PENDING'),
(2, 'Water Bill', 45.00, '2026-06-10', 'PAID'),
(3, 'Internet', 80.00, '2026-06-25', 'PENDING'),
(3, 'Gym Membership', 50.00, '2026-06-01', 'OVERDUE'),
(4, 'Insurance', 500.00, '2026-07-01', 'PENDING'),
(4, 'Phone Bill', 60.00, '2026-06-12', 'PAID');

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
