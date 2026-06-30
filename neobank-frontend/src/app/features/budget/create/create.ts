import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { BudgetService } from '../../../core/services/budget';
import { ToastService } from '../../../core/services/toast';

@Component({
  selector: 'app-budget-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './create.html',
  styleUrl: './create.css',
})
export class Create {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private budgetService = inject(BudgetService);
  private toastService = inject(ToastService);

  budgetForm: FormGroup = this.fb.group({
    category: ['GROCERIES', [Validators.required]],
    limitAmount: ['', [Validators.required, Validators.min(0.01)]],
    budgetMonth: [new Date().toISOString().substring(0, 7), [Validators.required]]
  });

  categories = ['GROCERIES', 'UTILITIES', 'RENT', 'ENTERTAINMENT', 'TRANSFER', 'OTHER'];
  isSubmitting = signal(false);

  amountPresets = [2000, 5000, 10000, 20000, 50000];

  categoryLabels: Record<string, string> = {
    GROCERIES: 'Groceries',
    UTILITIES: 'Utilities',
    RENT: 'Rent & Housing',
    ENTERTAINMENT: 'Entertainment',
    TRANSFER: 'Transfers',
    OTHER: 'Other',
  };

  categoryIcons: Record<string, string> = {
    GROCERIES: 'fa-cart-shopping',
    UTILITIES: 'fa-bolt',
    RENT: 'fa-house',
    ENTERTAINMENT: 'fa-film',
    TRANSFER: 'fa-right-left',
    OTHER: 'fa-ellipsis',
  };

  categoryColors: Record<string, string> = {
    GROCERIES: 'linear-gradient(135deg, #22c55e, #16a34a)',
    UTILITIES: 'linear-gradient(135deg, #f59e0b, #d97706)',
    RENT: 'linear-gradient(135deg, #6366f1, #4f46e5)',
    ENTERTAINMENT: 'linear-gradient(135deg, #ec4899, #db2777)',
    TRANSFER: 'linear-gradient(135deg, #3b82f6, #2563eb)',
    OTHER: 'linear-gradient(135deg, #8b5cf6, #7c3aed)',
  };

  formatMonth(val: string): string {
    if (!val) return '—';
    const [year, month] = val.split('-');
    const date = new Date(+year, +month - 1);
    return date.toLocaleDateString('en-IN', { month: 'long', year: 'numeric' });
  }

  onSubmit(): void {
    if (this.budgetForm.invalid) {
      this.budgetForm.markAllAsTouched();
      return;
    }
    this.isSubmitting.set(true);
    this.budgetService.createBudget(this.budgetForm.value).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.toastService.success('Budget limit set successfully!');
        this.router.navigate(['/budget']);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        const msg = err.error?.message || 'Failed to set budget. Check if it already exists for this category/month.';
        this.toastService.error(msg);
      }
    });
  }
}
