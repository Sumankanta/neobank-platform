import { Component, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { Router, RouterLink } from '@angular/router';
import { AccountService } from '../../../core/services/account';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ToastService } from '../../../core/services/toast';

@Component({
  selector: 'app-create-account',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './create-account.html',
  styleUrl: './create-account.css',
})
export class CreateAccount {
  private accountService = inject(AccountService);
  private router = inject(Router);
  private toastService = inject(ToastService);

  isSubmitting = signal(false);

  createAccount(type: 'SAVINGS' | 'CURRENT'): void {
    this.isSubmitting.set(true);
    this.accountService.createAccount(type).subscribe({
      next: () => {
        this.isSubmitting.set(false);
        this.toastService.success(`${type} account opened successfully!`);
        this.router.navigate(['/dashboard']);
      },
      error: () => {
        this.isSubmitting.set(false);
        this.toastService.error('Failed to open account. Please try again.');
      }
    });
  }
}
