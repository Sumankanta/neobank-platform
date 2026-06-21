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

type LoginView = 'password' | 'otp' | 'social' | 'forgot';

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
    this.otpStep.set(1);
  }

  // ── CAPTCHA state ───────────────────────────────────────────────
  captchaVerified = signal(false);
  captchaLoading  = signal(false);

  verifyCaptcha(): void {
    if (this.captchaVerified()) return;
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

  // ── OTP flow ────────────────────────────────────────────────────
  otpStep      = signal<1 | 2 | 3>(1);
  otpCountdown = signal(60);
  otpError     = signal<string | null>(null);
  private countdownTimer: any;

  otpRequestForm: FormGroup = this.fb.group({
    contactType: ['phone'],
    contact:     ['', [Validators.required, phoneValidator]],
  });

  onContactTypeChange(): void {
    const ctrl = this.otpRequestForm.get('contact')!;
    ctrl.reset();
    if (this.otpRequestForm.get('contactType')?.value === 'email') {
      ctrl.setValidators([Validators.required, emailValidator]);
    } else {
      ctrl.setValidators([Validators.required, phoneValidator]);
    }
    ctrl.updateValueAndValidity();
  }

  otpVerifyForm: FormGroup = this.fb.group({
    code: ['', [Validators.required, Validators.minLength(6), Validators.maxLength(6)]],
  });

  onOtpInput(event: Event, index: number): void {
    const input = event.target as HTMLInputElement;
    const val = input.value.replace(/\D/g, '');
    input.value = val.slice(-1);
    this.collectOtpValue();
    if (val && index < 5) {
      (document.getElementById(`otp-${index + 1}`) as HTMLInputElement)?.focus();
    }
    if (index === 5 && val) { this.autoSubmitOtp(); }
  }

  // FIX: typed as KeyboardEvent (matches $any($event) cast in template)
  onOtpBackspace(event: KeyboardEvent, index: number): void {
    const input = event.target as HTMLInputElement;
    if (!input.value && index > 0) {
      (document.getElementById(`otp-${index - 1}`) as HTMLInputElement)?.focus();
    }
  }

  onOtpPaste(event: ClipboardEvent): void {
    event.preventDefault();
    const digits = (event.clipboardData?.getData('text') ?? '').replace(/\D/g, '').slice(0, 6);
    digits.split('').forEach((d, i) => {
      const el = document.getElementById(`otp-${i}`) as HTMLInputElement;
      if (el) el.value = d;
    });
    this.collectOtpValue();
    if (digits.length === 6) this.autoSubmitOtp();
  }

  private collectOtpValue(): void {
    const code = [0,1,2,3,4,5]
      .map(i => (document.getElementById(`otp-${i}`) as HTMLInputElement)?.value ?? '')
      .join('');
    this.otpVerifyForm.get('code')!.setValue(code);
  }

  private autoSubmitOtp(): void {
    if (this.otpVerifyForm.valid) setTimeout(() => this.verifyOtp(), 120);
  }

  requestOtp(): void {
    if (this.otpRequestForm.invalid) { this.otpRequestForm.markAllAsTouched(); return; }
    if (!this.captchaVerified()) return;

    this.isLoading.set(true);
    this.errorMessage.set(null);
    const { contact, contactType } = this.otpRequestForm.value;

    this.authSvc.requestOtp(contact, contactType).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.otpStep.set(2);
        this.toastSvc.success('OTP sent successfully! Check your phone/email (or console logs).');
        this.otpCountdown.set(60);
        this.startCountdown();
        setTimeout(() => (document.getElementById('otp-0') as HTMLInputElement)?.focus(), 100);
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 404) {
          this.errorMessage.set('No registered account found with this contact information.');
        } else {
          this.errorMessage.set('Failed to send OTP. Please try again.');
        }
      }
    });
  }

  verifyOtp(): void {
    this.collectOtpValue();
    if (this.otpVerifyForm.invalid) return;

    this.isLoading.set(true);
    this.otpError.set(null);
    const contact = this.otpRequestForm.get('contact')?.value;
    const code = this.otpVerifyForm.get('code')?.value;

    this.authSvc.verifyOtp(contact, code).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.otpStep.set(3);
        this.toastSvc.success('OTP verified successfully!');
        this.router.navigate(['/dashboard']);
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 403) {
          this.otpError.set('Account inactive. Pending admin approval.');
        } else if (err.status === 401 || err.status === 400) {
          this.otpError.set('Invalid or expired OTP code.');
        } else {
          this.otpError.set('Verification failed. Please try again.');
        }
      }
    });
  }

  resendOtp(): void {
    const contact = this.otpRequestForm.get('contact')?.value;
    const contactType = this.otpRequestForm.get('contactType')?.value;
    if (!contact) return;

    this.authSvc.requestOtp(contact, contactType).subscribe({
      next: () => {
        this.otpCountdown.set(60);
        this.toastSvc.success('OTP resent successfully!');
        this.startCountdown();
      },
      error: () => {
        this.toastSvc.error('Failed to resend OTP. Please try again.');
      }
    });
  }

  private startCountdown(): void {
    clearInterval(this.countdownTimer);
    this.countdownTimer = setInterval(() => {
      if (this.otpCountdown() <= 1) {
        clearInterval(this.countdownTimer);
        this.otpCountdown.set(0);
      } else {
        this.otpCountdown.update(v => v - 1);
      }
    }, 1000);
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

  ngOnDestroy(): void {
    clearInterval(this.countdownTimer);
  }
}
