export interface Bill {
  id: number;
  billerName: string;
  amount: number;
  dueDate: string; // YYYY-MM-DD
  status: 'PENDING' | 'PAID' | 'OVERDUE';
  remindMe: boolean;
  createdAt: string;
}
