export interface Transaction {
  id: number;
  accountId: number;
  accountNumber: string;
  accountType: 'SAVINGS' | 'CURRENT';
  type: 'DEBIT' | 'CREDIT';
  amount: number;
  description: string;
  category: string;
  transactionDate: string;
  balanceAfter: number;
}
