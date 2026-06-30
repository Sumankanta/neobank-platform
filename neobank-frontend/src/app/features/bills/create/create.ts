import { Component, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { BillService } from '../../../core/services/bill';
import { ToastService } from '../../../core/services/toast';

@Component({
  selector: 'app-bill-create',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
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
    remindMe: [true],
    billerType: ['UTILITIES'],
  });

  isSubmitting = signal(false);
  amountPresets = [199, 499, 999, 2000, 5000];

  billerTypes = [
    { value: 'UTILITIES', label: 'Utilities', icon: 'fa-bolt', color: 'linear-gradient(135deg, #f59e0b, #d97706)', placeholder: 'e.g., BESCOM, BWSSB' },
    { value: 'INTERNET', label: 'Internet', icon: 'fa-wifi', color: 'linear-gradient(135deg, #3b82f6, #2563eb)', placeholder: 'e.g., ACT Fibernet, Airtel' },
    { value: 'STREAMING', label: 'Streaming', icon: 'fa-film', color: 'linear-gradient(135deg, #ec4899, #db2777)', placeholder: 'e.g., Netflix, Disney+' },
    { value: 'INSURANCE', label: 'Insurance', icon: 'fa-shield-halved', color: 'linear-gradient(135deg, #22c55e, #16a34a)', placeholder: 'e.g., LIC, Star Health' },
    { value: 'RENT', label: 'Rent', icon: 'fa-house', color: 'linear-gradient(135deg, #6366f1, #4f46e5)', placeholder: 'e.g., Monthly House Rent' },
    { value: 'OTHER', label: 'Other', icon: 'fa-ellipsis', color: 'linear-gradient(135deg, #8b5cf6, #7c3aed)', placeholder: 'e.g., Gym, Subscription' },
  ];

  get currentType() {
    return this.billerTypes.find(t => t.value === this.billForm.get('billerType')?.value) ?? this.billerTypes[0];
  }
  get currentTypeIcon() { return this.currentType.icon; }
  get currentTypeLabel() { return this.currentType.label; }
  get currentTypeColor() { return this.currentType.color; }
  get currentTypePlaceholder() { return this.currentType.placeholder; }

  get daysUntilDue(): number | null {
    const val = this.billForm.get('dueDate')?.value;
    if (!val) return null;
    const due = new Date(val).getTime();
    const now = new Date().setHours(0, 0, 0, 0);
    return Math.ceil((due - now) / (1000 * 60 * 60 * 24));
  }

  selectBillerType(value: string) {
    this.billForm.get('billerType')?.setValue(value);
  }

  formatDate(val: string): string {
    if (!val) return '—';
    return new Date(val).toLocaleDateString('en-IN', { day: 'numeric', month: 'short', year: 'numeric' });
  }

  onSubmit(): void {
    if (this.billForm.invalid) {
      this.billForm.markAllAsTouched();
      return;
    }
    this.isSubmitting.set(true);
    const { billerType, ...payload } = this.billForm.value;
    this.billService.createBill(payload).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.toastService.success('Bill scheduled successfully!');
        this.router.navigate(['/bills']);
      },
      error: (err) => {
        this.isSubmitting.set(false);
        const msg = err.error?.message || 'Failed to schedule bill.';
        this.toastService.error(msg);
      }
    });
  }
}
