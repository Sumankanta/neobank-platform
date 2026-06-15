export interface Account {
  id: number;
  accountNumber: string;
  accountType: 'SAVINGS' | 'CURRENT';
  balance: number;
  createdAt: string;
}
