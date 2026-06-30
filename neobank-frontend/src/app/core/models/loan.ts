export interface LoanProduct {
  id: number;
  productName: string;
  description: string;
  annualInterestRate: number;   // field name from backend LoanProductResponse
  interestRate?: number;        // optional alias kept for backward compat
  referenceAmount: number;
  minAmount: number;
  maxAmount: number;
  minTenureMonths: number;
  maxTenureMonths: number;
  isActive?: boolean;
}

export interface LoanApplication {
  id: number;
  userId: number;
  loanProduct: LoanProduct;
  productName?: string;
  requestedAmount: number;
  tenureMonths: number;
  status: 'PENDING' | 'APPROVED' | 'REJECTED';
  adminRemarks?: string;
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
