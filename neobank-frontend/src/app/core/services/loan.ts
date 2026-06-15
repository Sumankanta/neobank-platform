import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiEndpoints } from '../utils/api-endpoints';
import { LoanProduct, LoanApplication, LoanAccount, LoanRepayment } from '../models/loan';

@Injectable({
  providedIn: 'root'
})
export class LoanService {
  private http = inject(HttpClient);

  // Product Management (Admin + User)
  getLoanProducts(): Observable<LoanProduct[]> {
    return this.http.get<LoanProduct[]>(ApiEndpoints.loans.products());
  }

  createLoanProduct(product: Partial<LoanProduct>): Observable<LoanProduct> {
    return this.http.post<LoanProduct>(ApiEndpoints.loans.products(), product);
  }

  // Applications (User)
  applyForLoan(application: { productId: number; amount: number; tenureMonths: number }): Observable<LoanApplication> {
    return this.http.post<LoanApplication>(ApiEndpoints.loans.apply(), application);
  }

  getMyApplications(): Observable<LoanApplication[]> {
    return this.http.get<LoanApplication[]>(ApiEndpoints.loans.myApplications());
  }

  // Applications (Admin)
  getAdminApplications(): Observable<LoanApplication[]> {
    return this.http.get<LoanApplication[]>(ApiEndpoints.loans.adminApplications());
  }

  processApplication(applicationId: number, decision: { status: 'APPROVED' | 'REJECTED'; reason?: string }): Observable<LoanApplication> {
    return this.http.put<LoanApplication>(ApiEndpoints.loans.decision(applicationId), decision);
  }

  // Active Loans & Repayments
  getMyLoanAccounts(): Observable<LoanAccount[]> {
    return this.http.get<LoanAccount[]>(ApiEndpoints.loans.myAccounts());
  }

  getRepaymentSchedule(loanAccountId: number): Observable<LoanRepayment[]> {
    return this.http.get<LoanRepayment[]>(ApiEndpoints.loans.repayments(loanAccountId));
  }
}
