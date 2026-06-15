import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AccountService } from '../../../core/services/account';
import { TransactionService } from '../../../core/services/transaction';
import { InsightsService } from '../../../core/services/insights';
import { Account } from '../../../core/models/account';
import { Transaction } from '../../../core/models/transaction';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatIconModule } from '@angular/material/icon';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format-pipe';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';

@Component({
  selector: 'app-account-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatProgressSpinnerModule,
    MatIconModule,
    CurrencyFormatPipe,
    BaseChartDirective,
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  private accountService = inject(AccountService);
  private transactionService = inject(TransactionService);
  private insightsService = inject(InsightsService);

  accounts = signal<Account[]>([]);
  recentTransactions = signal<Transaction[]>([]);
  totalIncome = signal<number>(0);
  totalExpense = signal<number>(0);
  isLoading = signal(true);

  totalBalance = computed(() => 
    this.accounts().reduce((sum, acc) => sum + acc.balance, 0)
  );

  availableBalance = computed(() => {
    // Mocking available balance (just an example, usually Total Balance + Credit Limit)
    return this.totalBalance() * 0.8;
  });

  utilizedAmount = computed(() => {
    return this.totalExpense();
  });

  // Chart configuration
  public lineChartData: ChartData<'line'> = {
    labels: [],
    datasets: [
      {
        data: [],
        label: 'Available Balance',
        backgroundColor: 'rgba(124, 58, 237, 0.1)',
        borderColor: '#7c3aed',
        borderWidth: 2,
        pointBackgroundColor: '#7c3aed',
        pointBorderColor: '#fff',
        pointHoverBackgroundColor: '#fff',
        pointHoverBorderColor: '#7c3aed',
        fill: 'origin',
        tension: 0.4
      },
      {
        data: [],
        label: 'Utilized Amount',
        backgroundColor: 'transparent',
        borderColor: '#10b981',
        borderWidth: 2,
        borderDash: [5, 5],
        pointBackgroundColor: '#10b981',
        pointBorderColor: '#fff',
        pointHoverBackgroundColor: '#fff',
        pointHoverBorderColor: '#10b981',
        fill: false,
        tension: 0.4
      }
    ]
  };
  
  public lineChartOptions: ChartConfiguration<'line'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    elements: {
      point: {
        radius: 0,
        hitRadius: 10,
        hoverRadius: 4
      }
    },
    scales: {
      x: {
        grid: {
          display: false
        },
        ticks: { color: '#9ca3af', font: { size: 10 } }
      },
      y: {
        grid: {
          color: 'rgba(255, 255, 255, 0.05)'
        },
        ticks: { 
          color: '#9ca3af', 
          font: { size: 10 },
          callback: function(value) {
            return '$' + Number(value).toLocaleString();
          }
        }
      }
    },
    plugins: {
      legend: {
        display: true,
        position: 'top',
        align: 'end',
        labels: {
          usePointStyle: true,
          boxWidth: 6,
          color: '#9ca3af',
          font: { size: 11 }
        }
      },
      tooltip: {
        backgroundColor: '#1f2937',
        titleColor: '#f3f4f6',
        bodyColor: '#f3f4f6',
        padding: 12,
        cornerRadius: 8,
        displayColors: true,
        boxPadding: 4,
        callbacks: {
          label: function(context) {
            let label = context.dataset.label || '';
            if (label) {
              label += ': ';
            }
            if (context.parsed.y !== null) {
              label += new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(context.parsed.y);
            }
            return label;
          }
        }
      }
    }
  };
  
  public lineChartType: 'line' = 'line';

  ngOnInit(): void {
    this.loadAccountsData();
  }

  loadAccountsData(): void {
    this.isLoading.set(true);

    this.accountService.getAccounts().subscribe({
      next: (accounts) => {
        this.accounts.set(accounts);

        // Fetch recent transactions to use as "Scheduled Payments" mock
        this.transactionService.getAllTransactions(0, 5).subscribe({
          next: (page) => {
            this.recentTransactions.set(page.content);
          }
        });

        // Fetch insights for chart
        this.insightsService.getInsights().subscribe({
          next: (data) => {
            this.totalIncome.set(data.totalIncome);
            this.totalExpense.set(data.totalExpense);
            
            if (data.trends && data.trends.length > 0) {
              this.lineChartData = {
                labels: data.trends.map(t => t.month.substring(0, 3)), // Jan, Feb, etc.
                datasets: [
                  {
                    ...this.lineChartData.datasets[0],
                    data: data.trends.map(t => t.income) // Mocking Available Balance with income trend
                  },
                  {
                    ...this.lineChartData.datasets[1],
                    data: data.trends.map(t => t.expense) // Mocking Utilized with expense trend
                  }
                ]
              };
            }
            
            this.isLoading.set(false);
          },
          error: () => this.isLoading.set(false)
        });
      },
      error: () => this.isLoading.set(false)
    });
  }

  getAccountIcon(type: string): string {
    return type === 'SAVINGS' ? 'fa-vault' : 'fa-money-check-dollar';
  }

  getCardBrand(accountNumber: string): string {
    const num = accountNumber.replace(/\D/g, '');
    if (num.startsWith('4')) return 'Visa';
    if (num.startsWith('5')) return 'Mastercard';
    if (num.startsWith('3')) return 'Amex';
    return 'Card';
  }

  getCardIcon(brand: string): string {
    if (brand === 'Visa') return 'fa-brands fa-cc-visa';
    if (brand === 'Mastercard') return 'fa-brands fa-cc-mastercard';
    if (brand === 'Amex') return 'fa-brands fa-cc-amex';
    return 'fa-solid fa-credit-card';
  }
}
