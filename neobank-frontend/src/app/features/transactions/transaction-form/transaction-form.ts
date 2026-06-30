import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { TransactionService } from '../../../core/services/transaction';
import { AccountService } from '../../../core/services/account';
import { Account } from '../../../core/models/account';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format-pipe';
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
    CurrencyFormatPipe,
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
  
  presets = [500, 1000, 2000, 5000, 10000];

  selectedAccount = computed(() => {
    const id = Number(this.transactionForm.value.accountId);
    if (!id) return null;
    return this.accounts().find(a => a.id === id) || null;
  });

  newBalancePreview = computed(() => {
    const acc = this.selectedAccount();
    if (!acc) return null;
    const amount = Number(this.transactionForm.value.amount) || 0;
    const type = this.transactionForm.value.type;
    return type === 'DEBIT' ? acc.balance - amount : acc.balance + amount;
  });

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

  selectAccount(id: number): void {
    this.transactionForm.patchValue({ accountId: id });
  }

  applyPreset(val: number): void {
    this.transactionForm.patchValue({ amount: val });
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
        this.toastService.success('Transaction executed successfully! ✓');
        this.router.navigate(['/accounts', accountId]);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        const errorMsg = err.error?.message || 'Transaction failed. Please check your balance.';
        this.toastService.error(errorMsg);
      }
    });
  }

  formatCardNumber(num: string): string {
    if (!num) return '•••• •••• •••• ••••';
    // Format numeric code like standard bank cards: XXXX XXXX XXXX
    return num.replace(/(.{4})/g, '$1 ').trim();
  }
}
