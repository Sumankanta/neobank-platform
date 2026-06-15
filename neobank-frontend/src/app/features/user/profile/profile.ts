import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth';
import { User } from '../../../core/models/user';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ToastService } from '../../../core/services/toast';

@Component({
  selector: 'app-profile',
  standalone: true,
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './profile.html',
  styleUrl: './profile.css',
})
export class Profile implements OnInit {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);

  profileForm: FormGroup = this.fb.group({
    fullName: ['', [Validators.required, Validators.minLength(3)]],
    email: ['', [Validators.required, Validators.email]],
    phone: ['+91 98765 43210'] // Mock data for UI completeness
  });

  isLoading = signal(false);
  isUpdating = signal(false);
  user = signal<User | null>(null);

  ngOnInit(): void {
    this.loadProfile();
  }

  loadProfile(): void {
    this.isLoading.set(true);
    this.authService.getProfile().subscribe({
      next: (user) => {
        this.user.set(user);
        this.profileForm.patchValue({
          fullName: user.fullName,
          email: user.email
        });
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  onSubmit(): void {
    if (this.profileForm.invalid) {
      this.profileForm.markAllAsTouched();
      return;
    }

    this.isUpdating.set(true);
    // Send only what API expects
    const payload = {
      fullName: this.profileForm.value.fullName,
      email: this.profileForm.value.email
    };
    
    this.authService.updateProfile(payload).subscribe({
      next: (user) => {
        this.user.set(user);
        this.isUpdating.set(false);
        this.toastService.success('Profile updated successfully!');
      },
      error: () => {
        this.isUpdating.set(false);
        this.toastService.error('Failed to update profile.');
      }
    });
  }

  getInitials(): string {
    const name = this.user()?.fullName || 'User';
    return name.split(' ').map(n => n[0]).join('').substring(0, 2).toUpperCase();
  }
}
