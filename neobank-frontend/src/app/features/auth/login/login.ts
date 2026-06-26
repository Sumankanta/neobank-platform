import { Component, inject, signal, computed } from '@angular/core';
import {
  FormBuilder, FormGroup, ReactiveFormsModule,
  Validators, AbstractControl, ValidationErrors
} from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { CommonModule } from '@angular/common';
import { AuthService } from '../../../core/services/auth';
import { ToastService } from '../../../core/services/toast';


// ── Custom validators ────────────────────────────────────────────
function emailOrPhoneValidator(control: AbstractControl): ValidationErrors | null {
  const v = control.value?.trim() ?? '';
  const emailOk = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v);
  const phoneOk = /^[0-9]{10}$/.test(v);
  return emailOk || phoneOk ? null : { invalidContact: true };
}

function phoneValidator(control: AbstractControl): ValidationErrors | null {
  const v = control.value?.trim() ?? '';
  return /^[0-9]{10}$/.test(v) ? null : { invalidPhone: true };
}

function emailValidator(control: AbstractControl): ValidationErrors | null {
  const v = control.value?.trim() ?? '';
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v) ? null : { email: true };
}

type LoginView = 'password' | 'forgot';

@Component({
  selector: 'app-login',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login {
  private fb      = inject(FormBuilder);
  private authSvc = inject(AuthService);
  private router  = inject(Router);
  private toastSvc = inject(ToastService);


  // ── View state ──────────────────────────────────────────────────
  view = signal<LoginView>('password');

  switchView(v: LoginView): void {
    this.view.set(v);
    this.errorMessage.set(null);
    this.captchaVerified.set(false);
    this.captchaLoading.set(false);
  }

  // ── CAPTCHA state ───────────────────────────────────────────────
  captchaVerified = signal(false);
  captchaLoading  = signal(false);

  verifyCaptcha(): void {
    if (this.captchaVerified() || this.captchaLoading()) return;
    this.captchaLoading.set(true);
    setTimeout(() => {
      this.captchaLoading.set(false);
      this.captchaVerified.set(true);
    }, 900);
  }

  // ── Password login form ─────────────────────────────────────────
  loginForm: FormGroup = this.fb.group({
    idType:     ['email'],
    identifier: ['', [Validators.required, emailValidator]],
    password:   ['', [Validators.required]],
    rememberMe: [false],
  });

  showPassword = false;
  isLoading    = signal(false);
  errorMessage = signal<string | null>(null);

  idTypeIcon = computed((): string => {
    switch (this.loginForm.get('idType')?.value) {
      case 'phone':    return 'fa-solid fa-mobile-screen inp-icon';
      case 'username': return 'fa-solid fa-at inp-icon';
      default:         return 'fa-solid fa-envelope inp-icon';
    }
  });

  idTypePlaceholder = computed((): string => {
    switch (this.loginForm.get('idType')?.value) {
      case 'phone':    return '10-digit mobile number';
      case 'username': return 'Your username';
      default:         return 'you@example.com';
    }
  });

  idTypeError = computed((): string => {
    switch (this.loginForm.get('idType')?.value) {
      case 'phone':    return 'Enter a valid 10-digit phone number.';
      case 'username': return 'Username is required.';
      default:         return 'Enter a valid email address.';
    }
  });

  onIdTypeChange(): void {
    const ctrl = this.loginForm.get('identifier')!;
    ctrl.reset();
    switch (this.loginForm.get('idType')?.value) {
      case 'phone':
        ctrl.setValidators([Validators.required, phoneValidator]); break;
      case 'username':
        ctrl.setValidators([Validators.required, Validators.minLength(3)]); break;
      default:
        ctrl.setValidators([Validators.required, emailValidator]);
    }
    ctrl.updateValueAndValidity();
  }

  onSubmit(): void {
    if (this.loginForm.invalid) { this.loginForm.markAllAsTouched(); return; }
    if (!this.captchaVerified()) return;

    this.isLoading.set(true);
    this.errorMessage.set(null);

    // FIX: map 'identifier' → 'email' to match AuthService.login() signature
    const { identifier, password } = this.loginForm.value;
    this.authSvc.login({ email: identifier, password }).subscribe({
      next: () => { this.isLoading.set(false); this.router.navigate(['/dashboard']); },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 401)      this.errorMessage.set('Incorrect email or password.');
        else if (err.status === 403) this.errorMessage.set('Your account has been deactivated. Contact support.');
        else                         this.errorMessage.set('Could not sign in. Please try again.');
      },
    });
  }

  // ── Forgot password form ────────────────────────────────────────
  forgotForm: FormGroup = this.fb.group({
    identifier: ['', [Validators.required, emailOrPhoneValidator]],
  });

  forgotSent      = signal(false);
  isForgotLoading = signal(false);

  onForgotSubmit(): void {
    if (this.forgotForm.invalid) { this.forgotForm.markAllAsTouched(); return; }
    this.isForgotLoading.set(true);
    setTimeout(() => {
      this.isForgotLoading.set(false);
      this.forgotSent.set(true);
    }, 1300);
  }

}
