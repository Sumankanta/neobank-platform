export interface LoanProduct {
  id: number;
  productName: string;
  description: string;
  interestRate: number;
  referenceAmount: number;
  minAmount: number;
  maxAmount: number;
  minTenureMonths: number;
  maxTenureMonths: number;
  isActive: boolean;
}

export interface LoanApplication {
  id: number;
  userId: number;
  loanProduct: LoanProduct;
  amountRequested: number;
  tenureMonths: number;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  rejectionReason?: string;
  appliedAt: string;
}

export interface LoanAccount {
  id: number;
  userId: number;
  loanProduct: LoanProduct;
  principalAmount: number;
  interestRate: number;
  tenureMonths: number;
  remainingBalance: number;
  monthlyEmi: number;
  status: 'ACTIVE' | 'CLOSED';
  startDate: string;
  endDate: string;
}

export interface LoanRepayment {
  id: number;
  loanAccountId: number;
  installmentNumber: number;
  dueDate: string;
  installmentAmount: number;
  principalComponent: number;
  interestComponent: number;
  paymentDate?: string;
  status: 'SCHEDULED' | 'PAID' | 'OVERDUE';
}
