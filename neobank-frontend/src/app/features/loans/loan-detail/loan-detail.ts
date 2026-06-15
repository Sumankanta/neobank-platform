import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterModule } from '@angular/router';
import { LoanService } from '../../../core/services/loan';
import { LoanAccount, LoanRepayment } from '../../../core/models/loan';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format-pipe';

@Component({
  selector: 'app-loan-detail',
  standalone: true,
  imports: [CommonModule, RouterModule, CurrencyFormatPipe],
  template: `
    <div class="loan-detail-container animate-fade-in" *ngIf="loan()">
      <div class="back-link mb-4">
        <a routerLink="/loans"><i class="fa-solid fa-arrow-left"></i> Back to Loans</a>
      </div>
      
      <div class="card mb-4">
        <div class="flex-between">
          <div>
            <h1 class="text-2xl font-bold">{{ loan()?.loanProduct?.productName }}</h1>
            <p class="text-muted">Loan ID: #{{ loan()?.id }}</p>
          </div>
          <span class="badge" [class.badge-paid]="loan()?.status === 'ACTIVE'">{{ loan()?.status }}</span>
        </div>
        
        <div class="grid-3 mt-4">
          <div class="stat">
            <span class="label text-xs text-muted">Principal Amount</span>
            <div class="value font-bold text-xl">{{ loan()?.principalAmount | currencyFormat }}</div>
          </div>
          <div class="stat">
            <span class="label text-xs text-muted">Remaining Balance</span>
            <div class="value font-bold text-xl text-primary">{{ loan()?.remainingBalance | currencyFormat }}</div>
          </div>
          <div class="stat">
            <span class="label text-xs text-muted">Monthly EMI</span>
            <div class="value font-bold text-xl">{{ loan()?.monthlyEmi | currencyFormat }}</div>
          </div>
        </div>
      </div>

      <div class="card p-0">
        <div class="p-4 border-b">
          <h3 class="font-bold">Repayment Schedule</h3>
        </div>
        <div class="table-container">
          <table class="table">
            <thead>
              <tr>
                <th>Installment</th>
                <th>Due Date</th>
                <th>Amount</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              <tr *ngFor="let rep of repayments()">
                <td>#{{ rep.installmentNumber }}</td>
                <td>{{ rep.dueDate | date }}</td>
                <td>{{ rep.installmentAmount | currencyFormat }}</td>
                <td>
                  <span class="badge" 
                    [class.badge-paid]="rep.status === 'PAID'"
                    [class.badge-pending]="rep.status === 'SCHEDULED'"
                    [class.badge-overdue]="rep.status === 'OVERDUE'">
                    {{ rep.status }}
                  </span>
                </td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </div>
  `,
  styles: [`
    .loan-detail-container { max-width: 1000px; margin: 0 auto; }
    .back-link a { color: var(--text-muted); text-decoration: none; }
    .back-link a:hover { color: var(--primary); }
  `]
})
export class LoanDetail implements OnInit {
  private route = inject(ActivatedRoute);
  private loanService = inject(LoanService);

  loan = signal<LoanAccount | null>(null);
  repayments = signal<LoanRepayment[]>([]);

  ngOnInit() {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.loanService.getMyLoanAccounts().subscribe(loans => {
      const found = loans.find(l => l.id === id);
      if (found) {
        this.loan.set(found);
        this.loadRepayments(id);
      }
    });
  }

  loadRepayments(loanId: number) {
    this.loanService.getRepaymentSchedule(loanId).subscribe(data => {
      this.repayments.set(data);
    });
  }
}
