import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { InsightsService, InsightsData } from '../../../core/services/insights';
import { TransactionService } from '../../../core/services/transaction';
import { Transaction } from '../../../core/models/transaction';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format-pipe';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

@Component({
  selector: 'app-insights-dashboard',
  standalone: true,
  imports: [CommonModule, BaseChartDirective, CurrencyFormatPipe, MatProgressSpinnerModule],
  templateUrl: './insights-dashboard.html',
  styleUrl: './insights-dashboard.css'
})
export class InsightsDashboard implements OnInit {
  private insightsService = inject(InsightsService);
  private transactionService = inject(TransactionService);
  
  insights = signal<InsightsData | null>(null);
  recentTransactions = signal<Transaction[]>([]);
  isLoading = signal(true);

  // Bar Chart Configuration (Cash Flow)
  public barChartData: ChartData<'bar'> = {
    datasets: [],
    labels: []
  };

  public barChartOptions: ChartConfiguration<'bar'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { 
        display: true, 
        position: 'top', 
        align: 'end',
        labels: { 
          usePointStyle: true, 
          boxWidth: 8, 
          color: '#9ca3af',
          font: { size: 12 }
        } 
      },
      tooltip: { 
        backgroundColor: '#1f2937',
        titleColor: '#f3f4f6',
        bodyColor: '#f3f4f6',
        padding: 12,
        cornerRadius: 8,
        displayColors: true
      }
    },
    scales: {
      y: { 
        grid: { color: 'rgba(255, 255, 255, 0.05)' }, 
        ticks: { 
          color: '#9ca3af',
          callback: function(value) { return '$' + Number(value) / 1000 + 'k'; }
        },
        border: { display: false }
      },
      x: { 
        grid: { display: false }, 
        ticks: { color: '#9ca3af' },
        border: { display: false }
      }
    }
  };

  // Doughnut Chart Configuration (Monthly Expense)
  public doughnutChartData: ChartData<'doughnut'> = {
    datasets: [],
    labels: []
  };

  public doughnutChartOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false }, // Custom legend in HTML
      tooltip: {
        backgroundColor: '#1f2937',
        padding: 12,
        cornerRadius: 8
      }
    },
    cutout: '75%'
  };

  // Stats for the bottom of the chart
  avgIncome = 0;
  avgExpense = 0;
  bestMonth = '';
  highestIncome = 0;

  ngOnInit() {
    this.loadData();
  }

  loadData() {
    this.isLoading.set(true);
    
    // Fetch insights
    this.insightsService.getInsights().subscribe({
      next: (data) => {
        this.insights.set(data);
        this.prepareCharts(data);
        
        // Fetch transactions for "Top Merchants" mock
        this.transactionService.getAllTransactions(0, 4).subscribe({
          next: (page) => {
            this.recentTransactions.set(page.content);
            this.isLoading.set(false);
          },
          error: () => this.isLoading.set(false)
        });
      },
      error: () => this.isLoading.set(false)
    });
  }

  prepareCharts(data: InsightsData) {
    if (!data.trends || data.trends.length === 0) return;

    // Trend Chart (Bar)
    this.barChartData = {
      labels: data.trends.map(t => t.month.substring(0, 3).toUpperCase()),
      datasets: [
        {
          data: data.trends.map(t => t.income),
          label: 'Income',
          backgroundColor: '#86efac', // Light green
          hoverBackgroundColor: '#4ade80',
          borderRadius: { topLeft: 4, topRight: 4, bottomLeft: 0, bottomRight: 0 }
        },
        {
          data: data.trends.map(t => t.expense),
          label: 'Expense',
          backgroundColor: '#fde047', // Light yellow/amber
          hoverBackgroundColor: '#facc15',
          borderRadius: { topLeft: 4, topRight: 4, bottomLeft: 0, bottomRight: 0 }
        }
      ]
    };

    // Calculate averages and bests
    const totalInc = data.trends.reduce((sum, t) => sum + t.income, 0);
    const totalExp = data.trends.reduce((sum, t) => sum + t.expense, 0);
    this.avgIncome = totalInc / data.trends.length;
    this.avgExpense = totalExp / data.trends.length;
    
    let maxInc = 0;
    let maxIncMonth = '';
    data.trends.forEach(t => {
      if (t.income > maxInc) {
        maxInc = t.income;
        maxIncMonth = t.month;
      }
    });
    this.highestIncome = maxInc;
    this.bestMonth = maxIncMonth ? `${maxIncMonth.substring(0, 3).toUpperCase()} - $${maxInc}` : 'N/A';

    // Category Chart (Doughnut)
    this.doughnutChartData = {
      labels: data.categories.map(c => c.category),
      datasets: [{
        data: data.categories.map(c => c.amount),
        backgroundColor: ['#facc15', '#a3e635', '#4ade80', '#fbbf24', '#eab308', '#fef08a'],
        borderWidth: 0,
        hoverOffset: 4
      }]
    };
  }

  getCategoryColor(index: number): string {
    const colors = ['#facc15', '#a3e635', '#4ade80', '#fbbf24', '#eab308', '#fef08a'];
    return colors[index % colors.length];
  }

  getMerchantIcon(desc: string): string {
    const d = desc.toLowerCase();
    if (d.includes('spotify')) return 'fa-brands fa-spotify text-success';
    if (d.includes('amazon')) return 'fa-brands fa-amazon text-warning';
    if (d.includes('food') || d.includes('restaurant')) return 'fa-solid fa-utensils text-primary';
    if (d.includes('uber')) return 'fa-brands fa-uber text-primary';
    return 'fa-solid fa-store text-secondary';
  }
}
