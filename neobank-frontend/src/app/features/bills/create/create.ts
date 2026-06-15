import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { BillService } from '../../../core/services/bill';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatSlideToggleModule } from '@angular/material/slide-toggle';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ToastService } from '../../../core/services/toast';

@Component({
  selector: 'app-bill-create',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    RouterLink,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatSlideToggleModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './create.html',
  styleUrl: './create.css',
})
export class Create {
  private fb = inject(FormBuilder);
  private router = inject(Router);
  private billService = inject(BillService);
  private toastService = inject(ToastService);

  billForm: FormGroup = this.fb.group({
    billerName: ['', [Validators.required, Validators.minLength(3)]],
    amount: ['', [Validators.required, Validators.min(0.01)]],
    dueDate: ['', [Validators.required]],
    remindMe: [true]
  });

  isSubmitting = signal(false);

  onSubmit(): void {
    if (this.billForm.invalid) {
      this.billForm.markAllAsTouched();
      return;
    }

    this.isSubmitting.set(true);
    this.billService.createBill(this.billForm.value).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.toastService.success('Bill scheduled successfully!');
        this.router.navigate(['/bills']);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        const errorMsg = err.error?.message || 'Failed to schedule bill. Check for duplicates.';
        this.toastService.error(errorMsg);
      }
    });
  }
}
