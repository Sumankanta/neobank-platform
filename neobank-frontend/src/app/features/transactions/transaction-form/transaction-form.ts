import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TransactionService } from '../../../core/services/transaction';
import { AccountService } from '../../../core/services/account';
import { Account } from '../../../core/models/account';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ToastService } from '../../../core/services/toast';

@Component({
  selector: 'app-transaction-form',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatSelectModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './transaction-form.html',
  styleUrl: './transaction-form.css',
})
export class TransactionForm implements OnInit {
  private fb = inject(FormBuilder);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private transactionService = inject(TransactionService);
  private accountService = inject(AccountService);
  private toastService = inject(ToastService);

  transactionForm: FormGroup = this.fb.group({
    accountId: ['', [Validators.required]],
    type: ['DEBIT', [Validators.required]],
    amount: ['', [Validators.required, Validators.min(0.01)]],
    description: ['', [Validators.required, Validators.minLength(3)]]
  });

  accounts = signal<Account[]>([]);
  isLoading = signal(false);
  isSubmitting = signal(false);

  ngOnInit(): void {
    this.loadAccounts();
    const preselectedId = this.route.snapshot.queryParamMap.get('accountId');
    if (preselectedId) {
      this.transactionForm.patchValue({ accountId: Number(preselectedId) });
    }
  }

  loadAccounts(): void {
    this.isLoading.set(true);
    this.accountService.getAccounts().subscribe({
      next: (accounts) => {
        this.accounts.set(accounts);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  onSubmit(): void {
    if (this.transactionForm.invalid) {
      this.transactionForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    const { accountId, ...transactionData } = this.transactionForm.value;

    this.transactionService.createTransaction(accountId, transactionData).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.toastService.success('Transaction successful!');
        this.router.navigate(['/accounts', accountId]);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        const errorMsg = err.error?.message || 'Transaction failed. Please check your balance.';
        this.toastService.error(errorMsg);
      }
    });
  }
}
