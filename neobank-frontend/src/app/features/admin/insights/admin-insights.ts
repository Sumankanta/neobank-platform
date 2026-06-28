import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AdminService } from '../../../core/services/admin';

@Component({
  selector: 'app-admin-insights',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './admin-insights.html',
  styleUrl: './admin-insights.css',
})
export class AdminInsights implements OnInit {
  private adminService = inject(AdminService);

  users = signal<any[]>([]);
  transactions = signal<any[]>([]);
  loans = signal<any[]>([]);
  isLoading = signal(true);

  // Computed stats
  totalUsers = computed(() => this.users().length);
  activeUsers = computed(() => this.users().filter(u => u.active).length);
  pendingKyc = computed(() => this.users().filter(u => !u.active).length);

  creditTxns = computed(() => this.transactions().filter(t => t.type === 'CREDIT'));
  debitTxns = computed(() => this.transactions().filter(t => t.type === 'DEBIT'));
  totalCredit = computed(() => this.creditTxns().reduce((s, t) => s + (t.amount || 0), 0));
  totalDebit = computed(() => this.debitTxns().reduce((s, t) => s + (t.amount || 0), 0));

  approvedLoans = computed(() => this.loans().filter(l => l.status === 'APPROVED'));
  pendingLoans = computed(() => this.loans().filter(l => l.status === 'PENDING'));
  rejectedLoans = computed(() => this.loans().filter(l => l.status === 'REJECTED'));
  totalLoanAmount = computed(() => this.approvedLoans().reduce((s, l) => s + (l.amount || 0), 0));

  // Category breakdown
  categoryBreakdown = computed(() => {
    const map: Record<string, number> = {};
    this.transactions().forEach(t => {
      const cat = t.category || 'GENERAL';
      map[cat] = (map[cat] || 0) + 1;
    });
    const total = this.transactions().length || 1;
    return Object.entries(map).map(([cat, count]) => ({ cat, count, pct: Math.round((count / total) * 100) }))
      .sort((a, b) => b.count - a.count).slice(0, 6);
  });

  // Role breakdown
  roleBreakdown = computed(() => {
    const admins = this.users().filter(u => u.role === 'ADMIN').length;
    const customers = this.users().filter(u => u.role === 'CUSTOMER').length;
    return [{ label: 'Customers', count: customers, pct: Math.round((customers / (this.totalUsers() || 1)) * 100) },
            { label: 'Admins', count: admins, pct: Math.round((admins / (this.totalUsers() || 1)) * 100) }];
  });

  loanStatusDist = computed(() => {
    const total = this.loans().length || 1;
    return [
      { label: 'Approved', count: this.approvedLoans().length, pct: Math.round((this.approvedLoans().length / total) * 100), cls: 'approved' },
      { label: 'Pending',  count: this.pendingLoans().length,  pct: Math.round((this.pendingLoans().length  / total) * 100), cls: 'pending' },
      { label: 'Rejected', count: this.rejectedLoans().length, pct: Math.round((this.rejectedLoans().length / total) * 100), cls: 'rejected' },
    ];
  });

  ngOnInit(): void {
    this.loadAll();
  }

  loadAll(): void {
    this.isLoading.set(true);
    let done = 0;
    const check = () => { if (++done === 3) this.isLoading.set(false); };

    this.adminService.getAllUsers().subscribe({ next: u => { this.users.set(u); check(); }, error: check });
    this.adminService.getAllTransactions().subscribe({ next: t => { this.transactions.set(t); check(); }, error: check });
    this.adminService.getAllLoanApplications().subscribe({ next: l => { this.loans.set(l); check(); }, error: check });
  }

  formatCurrency(v: number): string {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(v);
  }

  catColor(cat: string): string {
    const colors: Record<string, string> = {
      GENERAL: '#818cf8', FOOD: '#34d399', TRANSPORT: '#22d3ee',
      SHOPPING: '#fbbf24', UTILITIES: '#f87171', ENTERTAINMENT: '#a78bfa',
    };
    return colors[cat] || '#818cf8';
  }
}
