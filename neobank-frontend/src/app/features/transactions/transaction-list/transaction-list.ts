import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { TransactionService } from '../../../core/services/transaction';
import { Transaction } from '../../../core/models/transaction';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format-pipe';
import { ToastService } from '../../../core/services/toast';

@Component({
  selector: 'app-transaction-list',
  standalone: true,
  imports: [CommonModule, RouterLink, FormsModule, MatProgressSpinnerModule, CurrencyFormatPipe],
  templateUrl: './transaction-list.html',
  styleUrl: './transaction-list.css',
})
export class TransactionList implements OnInit {
  private transactionService = inject(TransactionService);
  private toastService = inject(ToastService);

  transactions = signal<Transaction[]>([]);
  isLoading = signal(true);
  isDownloading = signal(false);
  
  // Filters
  searchTerm = signal<string>('');
  startDate = signal<string>('');
  endDate = signal<string>('');
  selectedType = signal<'ALL' | 'CREDIT' | 'DEBIT'>('ALL');

  filteredTransactions = computed(() => {
    let txs = this.transactions();
    
    // Type filter
    if (this.selectedType() !== 'ALL') {
      txs = txs.filter(tx => tx.type === this.selectedType());
    }

    // Search filter
    const term = this.searchTerm().toLowerCase();
    if (term) {
      txs = txs.filter(tx => 
        tx.description.toLowerCase().includes(term) ||
        tx.accountType.toLowerCase().includes(term) ||
        tx.accountNumber.includes(term)
      );
    }

    return txs;
  });

  totalCredits = computed(() =>
    this.filteredTransactions()
      .filter((tx) => tx.type === 'CREDIT')
      .reduce((sum, tx) => sum + tx.amount, 0)
  );

  totalDebits = computed(() =>
    this.filteredTransactions()
      .filter((tx) => tx.type === 'DEBIT')
      .reduce((sum, tx) => sum + tx.amount, 0)
  );

  netCashFlow = computed(() => this.totalCredits() - this.totalDebits());

  ngOnInit(): void {
    this.loadTransactions();
  }

  loadTransactions(): void {
    this.isLoading.set(true);
    
    let start: string | undefined = undefined;
    let end: string | undefined = undefined;
    
    if (this.startDate()) {
      start = new Date(this.startDate()).toISOString();
    }
    if (this.endDate()) {
      const d = new Date(this.endDate());
      d.setHours(23, 59, 59, 999);
      end = d.toISOString();
    }

    this.transactionService.getAllTransactions(0, 100, start, end).subscribe({
      next: (page) => {
        this.transactions.set(page.content);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false),
    });
  }

  clearFilters(): void {
    this.startDate.set('');
    this.endDate.set('');
    this.searchTerm.set('');
    this.selectedType.set('ALL');
    this.loadTransactions();
  }

  downloadStatement(): void {
    this.isDownloading.set(true);
    this.transactionService.downloadStatement().subscribe({
      next: (blob) => {
        const url = window.URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `NeoBank_Statement_${new Date().toLocaleString('default', { month: 'short' })}_${new Date().getFullYear()}.pdf`;
        link.click();
        window.URL.revokeObjectURL(url);
        this.isDownloading.set(false);
        this.toastService.success('Statement downloaded successfully!');
      },
      error: () => {
        this.isDownloading.set(false);
        this.toastService.error('Failed to download statement.');
      }
    });
  }

  exportToCSV(): void {
    if (this.filteredTransactions().length === 0) {
      this.toastService.error('No transactions to export.');
      return;
    }

    const headers = ['Date', 'Account', 'Description', 'Type', 'Amount', 'Balance After'];
    const rows = this.filteredTransactions().map(tx => [
      new Date(tx.transactionDate).toLocaleString(),
      `${tx.accountType} - ${tx.accountNumber}`,
      `"${tx.description}"`, // wrap in quotes to handle commas
      tx.type,
      tx.amount.toString(),
      tx.balanceAfter.toString()
    ]);

    let csvContent = "data:text/csv;charset=utf-8," 
      + headers.join(",") + "\n"
      + rows.map(e => e.join(",")).join("\n");

    const encodedUri = encodeURI(csvContent);
    const link = document.createElement("a");
    link.setAttribute("href", encodedUri);
    link.setAttribute("download", `NeoBank_Transactions_${new Date().toISOString().split('T')[0]}.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    this.toastService.success('CSV Exported successfully!');
  }

  getCategoryIcon(description: string): string {
    const desc = description.toLowerCase();
    if (desc.includes('coffee') || desc.includes('starbucks')) return 'fa-solid fa-mug-hot text-amber-500';
    if (desc.includes('food') || desc.includes('restaurant') || desc.includes('uber eats')) return 'fa-solid fa-utensils text-orange-500';
    if (desc.includes('salary') || desc.includes('payroll')) return 'fa-solid fa-briefcase text-emerald-500';
    if (desc.includes('amazon') || desc.includes('shopping')) return 'fa-solid fa-bag-shopping text-blue-500';
    if (desc.includes('netflix') || desc.includes('spotify') || desc.includes('subscription')) return 'fa-solid fa-play text-red-500';
    if (desc.includes('uber') || desc.includes('lyft') || desc.includes('transport')) return 'fa-solid fa-car text-indigo-500';
    if (desc.includes('transfer') || desc.includes('zelle') || desc.includes('venmo')) return 'fa-solid fa-money-bill-transfer text-teal-500';
    return 'fa-solid fa-receipt text-gray-400';
  }

  getCategoryName(description: string): string {
    const desc = description.toLowerCase();
    if (desc.includes('coffee') || desc.includes('starbucks')) return 'Coffee';
    if (desc.includes('food') || desc.includes('restaurant') || desc.includes('uber eats')) return 'Dining';
    if (desc.includes('salary') || desc.includes('payroll')) return 'Income';
    if (desc.includes('amazon') || desc.includes('shopping')) return 'Shopping';
    if (desc.includes('netflix') || desc.includes('spotify') || desc.includes('subscription')) return 'Entertainment';
    if (desc.includes('uber') || desc.includes('lyft') || desc.includes('transport')) return 'Transport';
    if (desc.includes('transfer') || desc.includes('zelle') || desc.includes('venmo')) return 'Transfer';
    return 'General';
  }
}
