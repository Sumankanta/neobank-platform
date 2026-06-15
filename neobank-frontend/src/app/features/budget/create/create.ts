import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { BudgetService } from '../../../core/services/budget';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatSelectModule } from '@angular/material/select';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ToastService } from '../../../core/services/toast';

@Component({
  selector: 'app-budget-create',
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
  ],
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
        const errorMsg = err.error?.message || 'Failed to set budget. Check if already exists for this category/month.';
        this.toastService.error(errorMsg);
      }
    });
  }
}
