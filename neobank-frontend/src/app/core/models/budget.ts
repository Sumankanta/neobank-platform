export interface BudgetSummary {
  id: number;
  category: 'GROCERIES' | 'UTILITIES' | 'RENT' | 'ENTERTAINMENT' | 'TRANSFER' | 'OTHER';
  budgetMonth: string; // YYYY-MM
  limitAmount: number;
  spent: number;
  remaining: number;
  utilizationPercentage: number;
}
