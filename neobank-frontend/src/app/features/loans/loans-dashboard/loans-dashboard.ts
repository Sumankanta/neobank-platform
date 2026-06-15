import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { LoanService } from '../../../core/services/loan';
import { LoanProduct, LoanAccount, LoanApplication } from '../../../core/models/loan';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format-pipe';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-loans-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule, CurrencyFormatPipe, MatProgressSpinnerModule],
  templateUrl: './loans-dashboard.html',
  styleUrl: './loans-dashboard.css'
})
export class LoansDashboard implements OnInit {
  private loanService = inject(LoanService);

  products = signal<LoanProduct[]>([]);
  activeLoans = signal<LoanAccount[]>([]);
  applications = signal<LoanApplication[]>([]);
  isLoading = signal(true);

  totalOutstanding = computed(() => 
    this.activeLoans().reduce((sum, loan) => sum + loan.remainingBalance, 0)
  );

  totalEmi = computed(() =>
    this.activeLoans().reduce((sum, loan) => sum + loan.monthlyEmi, 0)
  );

  totalPrincipal = computed(() =>
    this.activeLoans().reduce((sum, loan) => sum + loan.principalAmount, 0)
  );

  nextEmiDate = computed(() => {
    const loans = this.activeLoans();
    if (loans.length === 0) return null;
    // Just mock next due date as 5th of next month
    const d = new Date();
    d.setMonth(d.getMonth() + 1);
    d.setDate(5);
    return d;
  });

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.isLoading.set(true);
    this.loanService.getLoanProducts().subscribe(data => this.products.set(data));
    this.loanService.getMyLoanAccounts().subscribe(data => this.activeLoans.set(data));
    this.loanService.getMyApplications().subscribe(data => {
      this.applications.set(data);
      this.isLoading.set(false);
    });
  }

  getStatusClass(status: string): string {
    switch (status) {
      case 'APPROVED': return 'badge-success';
      case 'PENDING': return 'badge-warning';
      case 'REJECTED': return 'badge-danger';
      case 'ACTIVE': return 'badge-success';
      case 'CLOSED': return 'badge-secondary';
      default: return 'badge-secondary';
    }
  }

  getRepaymentPercentage(loan: LoanAccount): number {
    if (loan.principalAmount === 0) return 0;
    return ((loan.principalAmount - loan.remainingBalance) / loan.principalAmount) * 100;
  }

  getRepaymentStyle(loan: LoanAccount) {
    const pct = this.getRepaymentPercentage(loan);
    return {
      'background': `conic-gradient(#818cf8 ${pct}%, rgba(255,255,255,0.05) 0)`
    };
  }
}
