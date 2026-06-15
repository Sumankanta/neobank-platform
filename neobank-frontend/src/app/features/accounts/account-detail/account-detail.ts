import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AccountService } from '../../../core/services/account';
import { TransactionService, Page } from '../../../core/services/transaction';
import { Account } from '../../../core/models/account';
import { Transaction } from '../../../core/models/transaction';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format-pipe';

@Component({
  selector: 'app-account-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatProgressSpinnerModule,
    MatButtonModule,
    MatIconModule,
    CurrencyFormatPipe,
  ],
  templateUrl: './account-detail.html',
  styleUrl: './account-detail.css',
})
export class AccountDetail implements OnInit {
  private route = inject(ActivatedRoute);
  private accountService = inject(AccountService);
  private transactionService = inject(TransactionService);

  account = signal<Account | null>(null);
  transactions = signal<Transaction[]>([]);
  isLoading = signal(true);
  
  currentPage = signal(0);
  totalElements = signal(0);
  pageSize = 10;

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadAccountData(id);
    }
  }

  loadAccountData(id: string): void {
    this.isLoading.set(true);
    this.accountService.getAccountById(id).subscribe({
      next: (account) => {
        this.account.set(account);
        this.loadTransactions(id);
      },
      error: () => this.isLoading.set(false)
    });
  }

  loadTransactions(accountId: string | number): void {
    this.transactionService.getTransactions(accountId, this.currentPage(), this.pageSize).subscribe({
      next: (page) => {
        this.transactions.set(page.content);
        this.totalElements.set(page.totalElements);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  nextPage(): void {
    if ((this.currentPage() + 1) * this.pageSize < this.totalElements()) {
      this.currentPage.update(p => p + 1);
      if (this.account()) {
        this.loadTransactions(this.account()!.id);
      }
    }
  }

  prevPage(): void {
    if (this.currentPage() > 0) {
      this.currentPage.update(p => p - 1);
      if (this.account()) {
        this.loadTransactions(this.account()!.id);
      }
    }
  }
}
