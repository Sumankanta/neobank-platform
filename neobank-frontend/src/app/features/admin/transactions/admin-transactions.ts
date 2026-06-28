import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../core/services/admin';

@Component({
  selector: 'app-admin-transactions',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-transactions.html',
  styleUrl: './admin-transactions.css',
})
export class AdminTransactions implements OnInit {
  private adminService = inject(AdminService);

  transactions = signal<any[]>([]);
  isLoading = signal(true);
  searchQuery = signal('');
  filterType = signal<string>('ALL');

  filteredTxns = computed(() => {
    const q = this.searchQuery().toLowerCase();
    const type = this.filterType();
    return this.transactions().filter(t => {
      const matchType = type === 'ALL' || t.type === type;
      const matchQ = !q || t.description?.toLowerCase().includes(q) || t.category?.toLowerCase().includes(q);
      return matchType && matchQ;
    });
  });

  totalCredit = computed(() => this.transactions().filter(t => t.type === 'CREDIT').reduce((s, t) => s + (t.amount || 0), 0));
  totalDebit = computed(() => this.transactions().filter(t => t.type === 'DEBIT').reduce((s, t) => s + (t.amount || 0), 0));

  ngOnInit(): void {
    this.loadTransactions();
  }

  loadTransactions(): void {
    this.isLoading.set(true);
    this.adminService.getAllTransactions().subscribe({
      next: (txns) => { this.transactions.set(txns); this.isLoading.set(false); },
      error: () => this.isLoading.set(false),
    });
  }

  setFilter(type: string): void { this.filterType.set(type); }

  formatCurrency(v: number): string {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(v);
  }
}
