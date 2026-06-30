import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, Router, RouterModule } from '@angular/router';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { LoanService } from '../../../core/services/loan';
import { LoanProduct } from '../../../core/models/loan';
import { ToastService } from '../../../core/services/toast';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format-pipe';

@Component({
  selector: 'app-apply-loan',
  standalone: true,
  imports: [CommonModule, RouterModule, ReactiveFormsModule, CurrencyFormatPipe],
  templateUrl: './apply-loan.html',
  styleUrl: './apply-loan.css'
})
export class ApplyLoan implements OnInit {
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private fb = inject(FormBuilder);
  private loanService = inject(LoanService);
  private toast = inject(ToastService);

  product = signal<LoanProduct | null>(null);
  loanForm: FormGroup;
  personalForm: FormGroup;
  isSubmitting = signal(false);
  
  // Wizard state
  currentStep = signal<number>(1);
  totalSteps = 3;

  constructor() {
    this.personalForm = this.fb.group({
      employmentType: ['SALARIED', Validators.required],
      monthlyIncome: ['', [Validators.required, Validators.min(1000)]],
      panNumber: ['', [Validators.required, Validators.pattern(/^[a-zA-Z]{5}[0-9]{4}[a-zA-Z]{1}$/)]]
    });

    this.loanForm = this.fb.group({
      amount: ['', [Validators.required, Validators.min(10000)]],
      tenureMonths: ['', [Validators.required, Validators.min(6)]],
      purpose: ['', [Validators.required, Validators.minLength(10)]]
    });
  }

  onPanInput(event: Event) {
    const input = event.target as HTMLInputElement;
    const start = input.selectionStart;
    const end = input.selectionEnd;
    const upperValue = input.value.toUpperCase();
    this.personalForm.get('panNumber')?.setValue(upperValue, { emitEvent: false });
    input.value = upperValue;
    if (start !== null && end !== null) {
      input.setSelectionRange(start, end);
    }
  }

  ngOnInit() {
    const productId = Number(this.route.snapshot.paramMap.get('productId'));
    this.loanService.getLoanProducts().subscribe(products => {
      const found = products.find(p => p.id === productId);
      if (found) {
        this.product.set(found);
        this.loanForm.get('amount')?.setValidators([
          Validators.required, 
          Validators.min(found.minAmount), 
          Validators.max(found.maxAmount)
        ]);
        this.loanForm.get('tenureMonths')?.setValidators([
          Validators.required, 
          Validators.min(found.minTenureMonths), 
          Validators.max(found.maxTenureMonths)
        ]);
      } else {
        this.toast.error('Product not found');
        this.router.navigate(['/loans']);
      }
    });
  }

  nextStep() {
    if (this.currentStep() === 1 && this.personalForm.invalid) {
      this.personalForm.markAllAsTouched();
      return;
    }
    if (this.currentStep() === 2 && this.loanForm.invalid) {
      this.loanForm.markAllAsTouched();
      return;
    }
    if (this.currentStep() < this.totalSteps) {
      this.currentStep.update(s => s + 1);
    }
  }

  prevStep() {
    if (this.currentStep() > 1) {
      this.currentStep.update(s => s - 1);
    }
  }

  onSubmit() {
    if (this.loanForm.valid && this.product()) {
      this.isSubmitting.set(true);
      const applicationData = {
        productId: this.product()!.id,
        amount: this.loanForm.value.amount,
        tenureMonths: this.loanForm.value.tenureMonths
      };

      this.loanService.applyForLoan(applicationData).subscribe({
        next: () => {
          this.toast.success('Loan application submitted successfully!');
          this.router.navigate(['/loans']);
        },
        error: (err) => {
          this.toast.error(err.error?.message || 'Failed to submit application');
          this.isSubmitting.set(false);
        }
      });
    }
  }

  calculateEMI(): number {
    const p = Number(this.loanForm.value.amount);
    const annualRate = this.product()?.annualInterestRate || this.product()?.interestRate || 0;
    const r = annualRate / 12 / 100;
    const n = Number(this.loanForm.value.tenureMonths);
    if (p > 0 && n > 0) {
      if (r === 0) return p / n;
      return (p * r * Math.pow(1 + r, n)) / (Math.pow(1 + r, n) - 1);
    }
    return 0;
  }
}
