import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { BudgetService } from '../../../core/services/budget';
import { AuthService } from '../../../core/services/auth';
import { BudgetSummary } from '../../../core/models/budget';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format-pipe';
import { BaseChartDirective } from 'ng2-charts';
import { ChartConfiguration, ChartData } from 'chart.js';

@Component({
  selector: 'app-budget-dashboard',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatProgressSpinnerModule,
    CurrencyFormatPipe,
    BaseChartDirective
  ],
  templateUrl: './dashboard.html',
  styleUrl: './dashboard.css',
})
export class Dashboard implements OnInit {
  private budgetService = inject(BudgetService);
  private authService = inject(AuthService);

  budgets = signal<BudgetSummary[]>([]);
  isLoading = signal(true);

  currentMonth = new Date().toISOString().substring(0, 7); // YYYY-MM

  totalBudget = computed(() => 
    this.budgets().reduce((sum, b) => sum + b.limitAmount, 0)
  );

  totalSpent = computed(() => 
    this.budgets().reduce((sum, b) => sum + b.spent, 0)
  );
  
  totalRemaining = computed(() => 
    this.totalBudget() - this.totalSpent()
  );

  overallUtilization = computed(() => 
    this.totalBudget() > 0 ? (this.totalSpent() / this.totalBudget()) * 100 : 0
  );

  // Doughnut Chart Configuration
  public doughnutChartData: ChartData<'doughnut'> = {
    datasets: [],
    labels: []
  };

  public doughnutChartOptions: ChartConfiguration<'doughnut'>['options'] = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: '#1f2937',
        padding: 12,
        cornerRadius: 8,
        callbacks: {
          label: function(context) {
            let label = context.label || '';
            if (label) label += ': ';
            if (context.parsed !== null) {
              label += new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(context.parsed);
            }
            return label;
          }
        }
      }
    },
    cutout: '75%'
  };

  ngOnInit(): void {
    this.loadBudgets();
  }

  loadBudgets(): void {
    const user = this.authService.currentUser();
    if (!user) {
      this.isLoading.set(false);
      return;
    }

    this.isLoading.set(true);
    this.budgetService.getBudgetSummary(user.userId, this.currentMonth).subscribe({
      next: (budgets: BudgetSummary[]) => {
        // Sort budgets by utilization descending
        budgets.sort((a, b) => b.utilizationPercentage - a.utilizationPercentage);
        this.budgets.set(budgets);
        this.prepareChart(budgets);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  prepareChart(budgets: BudgetSummary[]) {
    if (budgets.length === 0) return;
    
    this.doughnutChartData = {
      labels: budgets.map(b => this.formatCategoryName(b.category)),
      datasets: [{
        data: budgets.map(b => b.spent),
        backgroundColor: ['#818cf8', '#34d399', '#fbbf24', '#f87171', '#c084fc', '#60a5fa'],
        borderWidth: 0,
        hoverOffset: 4
      }]
    };
  }

  getProgressColor(percentage: number): string {
    if (percentage >= 95) return 'red';
    if (percentage >= 75) return 'amber';
    return 'green';
  }

  getCategoryIcon(category: string): string {
    const c = category.toUpperCase();
    if (c.includes('GROCER') || c.includes('FOOD')) return 'fa-solid fa-cart-shopping text-emerald-400';
    if (c.includes('UTIL') || c.includes('BILL')) return 'fa-solid fa-lightbulb text-amber-400';
    if (c.includes('RENT') || c.includes('HOUSE')) return 'fa-solid fa-house text-blue-400';
    if (c.includes('ENTERTAINMENT') || c.includes('MOVIE')) return 'fa-solid fa-film text-purple-400';
    if (c.includes('TRANSPORT') || c.includes('TRAVEL')) return 'fa-solid fa-car text-indigo-400';
    if (c.includes('SHOPPING')) return 'fa-solid fa-bag-shopping text-pink-400';
    return 'fa-solid fa-shapes text-gray-400';
  }

  getCategoryBgColor(category: string): string {
    const c = category.toUpperCase();
    if (c.includes('GROCER') || c.includes('FOOD')) return 'bg-emerald-500/10';
    if (c.includes('UTIL') || c.includes('BILL')) return 'bg-amber-500/10';
    if (c.includes('RENT') || c.includes('HOUSE')) return 'bg-blue-500/10';
    if (c.includes('ENTERTAINMENT') || c.includes('MOVIE')) return 'bg-purple-500/10';
    if (c.includes('TRANSPORT') || c.includes('TRAVEL')) return 'bg-indigo-500/10';
    if (c.includes('SHOPPING')) return 'bg-pink-500/10';
    return 'bg-gray-500/10';
  }

  formatCategoryName(category: string): string {
    return category.charAt(0).toUpperCase() + category.slice(1).toLowerCase();
  }
}
