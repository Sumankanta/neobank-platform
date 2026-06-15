# NeoBank Database Migration Guide

## Architecture Overview
This database schema is designed for a robust NeoBanking system, supporting core banking, personal finance management, and automated lending.

## Execution Order
The scripts must be executed in the following numeric order to ensure all Foreign Key dependencies are satisfied.

### Sprint 1: Foundation
1. `01_create_users_table.sql`: Base user entity.
2. `02_create_accounts_table.sql`: Links to users.
3. `03_create_transactions_table.sql`: Links to accounts.

### Sprint 2: PFM & Loyalty
4. `04_create_budgets_table.sql`: Monthly spending limits.
5. `05_create_bills_table.sql`: Upcoming payments.
6. `06_create_rewards_table.sql`: Loyalty points.

### Sprint 3: Lending
7. `07_create_loan_products_table.sql`: Standard loan offerings.
8. `08_create_loan_applications_table.sql`: User requests for loans.
9. `09_create_loan_accounts_table.sql`: Active loans.
10. `10_create_loan_repayments_table.sql`: Installment tracking.

### Post-Migration
11. `11_seed_data.sql`: Populates the system with administrative and sample customer data.

---

## Sprint 4: Analytics & Aggregation Queries

Sprint 4 focuses on generating insights without modifying the schema. Below are the core aggregation queries used in the Admin and User consoles.

### 1. Spending Insights (by Category)
*Used for the Doughnut Chart in the Dashboard.*
```sql
SELECT category, SUM(amount) as total_spent
FROM transactions
WHERE type = 'DEBIT' AND account_id IN (SELECT id FROM accounts WHERE user_id = :userId)
GROUP BY category;
```

### 2. Monthly Budget Utilization
*Calculates how much of the budget remains.*
```sql
SELECT category, budget_limit, spent_amount, 
       (spent_amount / budget_limit) * 100 as utilization_percent
FROM budgets
WHERE user_id = :userId AND budget_month = :currentMonth;
```

### 3. Overdue Bill Summary (Admin Console)
*Identifies systemic payment issues.*
```sql
SELECT status, COUNT(*) as count, SUM(amount) as total_value
FROM bills
GROUP BY status;
```

### 4. Loan Portfolio Risk
*Calculates total exposure in approved loans.*
```sql
SELECT lp.product_name, SUM(la.remaining_balance) as total_outstanding
FROM loan_accounts la
JOIN loan_products lp ON la.loan_product_id = lp.id
GROUP BY lp.product_name;
```

## Production Readiness Notes
- **Indexes:** Strategic indexes are included on `email`, `account_number`, `status`, and `due_date` for high-performance lookups.
- **Constraints:** `ON DELETE CASCADE` is used for user-owned data (Budgets, Rewards) while `ON DELETE RESTRICT` protects financial history (Transactions).
- **Engine:** All tables use `InnoDB` for ACID compliance and row-level locking.
