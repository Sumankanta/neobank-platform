import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule, DecimalPipe, SlicePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { FormsModule } from '@angular/forms';
import { AccountService } from '../../../core/services/account';
import { TransactionService } from '../../../core/services/transaction';
import { InsightsService } from '../../../core/services/insights';
import { Account } from '../../../core/models/account';
import { Transaction } from '../../../core/models/transaction';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format-pipe';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData, ChartType } from 'chart.js';

@Component({
  selector: 'app-home',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    FormsModule,
    DecimalPipe,
    SlicePipe,
    MatProgressSpinnerModule,
    MatIconModule,
    CurrencyFormatPipe,
    BaseChartDirective,
  ],
  templateUrl: './home.html',
  styleUrl: './home.css',
})
export class Home implements OnInit {
  private accountService = inject(AccountService);
  private transactionService = inject(TransactionService);
  private insightsService = inject(InsightsService);

  accounts = signal<Account[]>([]);
  recentTransactions = signal<Transaction[]>([]);
  income = signal<number>(0);
  expense = signal<number>(0);
  isLoading = signal(true);

  // Transaction search
  txSearch = '';

  filteredTransactions = computed(() => {
    const q = this.txSearch.toLowerCase();
    return this.recentTransactions().filter(tx =>
      !q ||
      tx.description?.toLowerCase().includes(q) ||
      tx.accountType?.toLowerCase().includes(q) ||
      tx.accountNumber?.toLowerCase().includes(q)
    );
  });

  totalBalance = computed(() =>
    this.accounts().reduce((sum, acc) => sum + acc.balance, 0)
  );

  savingsRate = computed(() => {
    const inc = this.income();
    if (!inc) return 0;
    const saved = Math.max(0, inc - this.expense());
    return Math.min(100, (saved / inc) * 100);
  });

  expenseRatio = computed(() => {
    const inc = this.income();
    if (!inc) return 0;
    return Math.min(100, (this.expense() / inc) * 100);
  });

  // Doughnut Chart configuration
  public doughnutChartData: ChartData<'doughnut'> = {
    labels: [],
    datasets: [
      {
        data: [],
        backgroundColor: ['#6366f1', '#06b6d4', '#f59e0b', '#ef4444', '#8b5cf6', '#10b981'],
        hoverBackgroundColor: ['#4f46e5', '#0891b2', '#d97706', '#dc2626', '#7c3aed', '#059669'],
        borderWidth: 0,
        hoverBorderWidth: 0,
      }
    ]
  };
  public doughnutChartType: 'doughnut' = 'doughnut';
  public doughnutChartOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    cutout: '72%',
    plugins: {
      legend: {
        position: 'right',
        labels: {
          usePointStyle: true,
          pointStyle: 'circle',
          padding: 14,
          color: '#9ca3af',
          font: { size: 11, family: 'Inter' }
        }
      }
    }
  };

  ngOnInit(): void {
    this.loadDashboardData();
  }

  loadDashboardData(): void {
    this.isLoading.set(true);

    this.accountService.getAccounts().subscribe({
      next: (accounts) => {
        this.accounts.set(accounts);

        this.transactionService.getAllTransactions(0, 8).subscribe({
          next: (page) => {
            this.recentTransactions.set(page.content);
          }
        });

        this.transactionService.getSpendingSummary().subscribe({
          next: (summary) => {
            this.doughnutChartData = {
              labels: Object.keys(summary),
              datasets: [{
                ...this.doughnutChartData.datasets[0],
                data: Object.values(summary)
              }]
            };
          }
        });

        this.insightsService.getInsights().subscribe({
          next: (data) => {
            this.income.set(data.totalIncome);
            this.expense.set(data.totalExpense);
            this.isLoading.set(false);
          },
          error: () => this.isLoading.set(false)
        });
      },
      error: () => this.isLoading.set(false)
    });
  }
}
