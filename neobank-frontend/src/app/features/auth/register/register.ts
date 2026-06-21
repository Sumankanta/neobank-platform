import { Component, inject, signal } from '@angular/core';
import { FormBuilder, FormGroup, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthService } from '../../../core/services/auth';
import { ToastService } from '../../../core/services/toast';
import { CustomValidators } from '../../../core/utils/validators';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CommonModule } from '@angular/common';

class Mailjs {
  private baseUrl = 'https://api.mail.tm';

  async createOneAccount(): Promise<any> {
    // 1. Get available domains
    const domainsResponse = await fetch(`${this.baseUrl}/domains`);
    if (!domainsResponse.ok) {
      throw new Error('Failed to fetch mail domains');
    }
    const domainsData = await domainsResponse.json();
    const domains = domainsData['hydra:member'] || domainsData;
    if (!domains || domains.length === 0) {
      throw new Error('No temporary domains available');
    }
    const domain = domains[0].domain;

    // 2. Generate a random email user and password
    const uniqueId = Math.random().toString(36).substring(2, 10);
    const email = `neobank_${uniqueId}@${domain}`;
    const password = Math.random().toString(36).substring(2, 12);

    // 3. Register the account on mail.tm
    const createResponse = await fetch(`${this.baseUrl}/accounts`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({
        address: email,
        password: password
      })
    });

    if (!createResponse.ok) {
      const errorData = await createResponse.json();
      throw new Error(errorData.message || 'Failed to create temp email account');
    }

    const accountData = await createResponse.json();
    return {
      status: true,
      data: {
        id: accountData.id,
        address: accountData.address,
        createdAt: accountData.createdAt,
        password: password
      }
    };
  }
}


@Component({
  selector: 'app-register',
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatCardModule,
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatProgressSpinnerModule,
  ],
  templateUrl: './register.html',
  styleUrl: './register.css',
})
export class Register {
  private fb = inject(FormBuilder);
  private authService = inject(AuthService);
  private toastService = inject(ToastService);
  private router = inject(Router);
  private mailjs = new Mailjs();

  isGeneratingEmail = signal(false);
  currentStep = signal(1);
  totalSteps = 8;

  registerForm: FormGroup = this.fb.group({
    // Step 1: Personal Info
    personal: this.fb.group({
      fullName: ['', [Validators.required, Validators.minLength(2)]],
      dob: ['', [Validators.required]],
      gender: ['', [Validators.required]],
      nationality: ['', [Validators.required]]
    }),
    // Step 2: Contact Info
    contact: this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      phone: ['', [Validators.required, Validators.pattern('^[0-9]{10}$')]]
    }),
    // Step 3: Identity Verification
    identity: this.fb.group({
      idType: ['', [Validators.required]],
      idNumber: ['', [Validators.required]],
      isVerified: [false, [Validators.requiredTrue]]
    }),
    // Step 4: Address Information
    address: this.fb.group({
      street: ['', [Validators.required]],
      city: ['', [Validators.required]],
      state: ['', [Validators.required]],
      zipCode: ['', [Validators.required, Validators.pattern('^[0-9]{5,6}$')]]
    }),
    // Step 5: Nominee Info
    nominee: this.fb.group({
      nomineeName: ['', [Validators.required]],
      relationship: ['', [Validators.required]],
      nomineeDob: ['', [Validators.required]]
    }),
    // Step 6: Account Preferences
    preferences: this.fb.group({
      accountType: ['', [Validators.required]],
      currency: ['INR', [Validators.required]]
    }),
    // Step 7: Security Setup
    security: this.fb.group({
      password: ['', [Validators.required, CustomValidators.passwordStrength()]],
      confirmPassword: ['', [Validators.required]]
    }, { validators: [CustomValidators.passwordMatch('password', 'confirmPassword')] }),
    // Step 8: Final Review
    review: this.fb.group({
      termsAccepted: [false, [Validators.requiredTrue]]
    })
  });

  isLoading = signal(false);
  errorMessage = signal<string | null>(null);

  // Group helpers
  get personalGroup()     { return this.registerForm.get('personal')     as FormGroup; }
  get contactGroup()      { return this.registerForm.get('contact')      as FormGroup; }
  get identityGroup()     { return this.registerForm.get('identity')     as FormGroup; }
  get addressGroup()      { return this.registerForm.get('address')      as FormGroup; }
  get nomineeGroup()      { return this.registerForm.get('nominee')      as FormGroup; }
  get preferencesGroup()  { return this.registerForm.get('preferences')  as FormGroup; }
  get securityGroup()     { return this.registerForm.get('security')     as FormGroup; }
  get reviewGroup()       { return this.registerForm.get('review')       as FormGroup; }

  getCurrentFormGroup(): FormGroup | null {
    switch (this.currentStep()) {
      case 1: return this.personalGroup;
      case 2: return this.contactGroup;
      case 3: return this.identityGroup;
      case 4: return this.addressGroup;
      case 5: return this.nomineeGroup;
      case 6: return this.preferencesGroup;
      case 7: return this.securityGroup;
      case 8: return this.reviewGroup;
      default: return null;
    }
  }

  nextStep(): void {
    const activeGroup = this.getCurrentFormGroup();
    if (activeGroup && activeGroup.invalid) {
      activeGroup.markAllAsTouched();
      return;
    }
    if (this.currentStep() < this.totalSteps) {
      this.currentStep.update(v => v + 1);
    }
  }

  prevStep(): void {
    if (this.currentStep() > 1) {
      this.currentStep.update(v => v - 1);
    }
  }

  isVerifying = signal(false);

  simulateIdentityVerification(): void {
    const idGroup = this.identityGroup;
    const idType = idGroup.get('idType')?.value;
    const idNumber = idGroup.get('idNumber')?.value;
    
    console.log('simulateIdentityVerification started. Type:', idType, 'Number:', idNumber);
    console.log('idType valid:', idGroup.get('idType')?.valid, 'idNumber valid:', idGroup.get('idNumber')?.valid);

    if (idGroup.get('idType')?.valid && idGroup.get('idNumber')?.valid && idNumber) {
      this.isVerifying.set(true);
      console.log('Calling authService.verifyDocument for:', idNumber);
      
      this.authService.verifyDocument(idNumber).subscribe({
        next: (res) => {
          console.log('Received response from verifyDocument:', res);
          this.isVerifying.set(false);
          if (res && res.verified) {
            idGroup.get('isVerified')?.setValue(true);
            this.toastService.success('Identity Verified Successfully');
          } else {
            idGroup.get('isVerified')?.setValue(false);
            this.toastService.error('Verification failed. This document number is already registered.');
          }
        },
        error: (err) => {
          console.error('Error during verifyDocument API call:', err);
          this.isVerifying.set(false);
          idGroup.get('isVerified')?.setValue(false);
          this.toastService.error('Verification failed. Unable to connect to verification service.');
        }
      });
    } else {
      console.warn('Form group invalid. Marking all as touched.');
      idGroup.markAllAsTouched();
    }
  }

  // ── Password Strength Helpers ─────────────────────────────────────
  private getPasswordStrength(password: string): number {
    if (!password) return 0;
    let score = 0;
    if (password.length >= 8)  score++;
    if (password.length >= 12) score++;
    if (/[A-Z]/.test(password) && /[a-z]/.test(password)) score++;
    if (/[0-9]/.test(password)) score++;
    if (/[^A-Za-z0-9]/.test(password)) score++;
    return Math.min(4, Math.max(1, Math.ceil(score * 4 / 5)));
  }

  getStrengthClass(barIndex: number, password: string): string {
    const strength = this.getPasswordStrength(password);
    if (barIndex > strength) return 'strength-bar';
    return `strength-bar active-${strength}`;
  }

  getStrengthLabelClass(password: string): string {
    const strength = this.getPasswordStrength(password);
    return `strength-label s${strength}`;
  }

  getStrengthLabel(password: string): string {
    const strength = this.getPasswordStrength(password);
    const labels: Record<number, string> = {
      1: 'Weak — add length and symbols',
      2: 'Fair — try mixing cases and numbers',
      3: 'Good — almost there',
      4: 'Strong password ✓',
    };
    return labels[strength] ?? '';
  }

  generateTempEmail(): void {
    this.isGeneratingEmail.set(true);
    this.mailjs.createOneAccount().then(
      (account: any) => {
        this.isGeneratingEmail.set(false);
        if (account && account.data && account.data.address) {
          const email = account.data.address;
          this.contactGroup.get('email')?.setValue(email);
          this.toastService.success(`Temp email generated: ${email}`);
        } else {
          this.toastService.error('Failed to generate temp email.');
        }
      },
      (err: any) => {
        this.isGeneratingEmail.set(false);
        this.toastService.error('Temp email service unavailable.');
      }
    );
  }

  onSubmit(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }

    this.isLoading.set(true);
    this.errorMessage.set(null);

    const f = this.registerForm.value;

    const payload = {
      // Step 1: Personal Info
      fullName:             f.personal.fullName,
      dob:                  f.personal.dob,
      gender:               f.personal.gender,
      nationality:          f.personal.nationality,

      // Step 2: Contact Info
      email:                f.contact.email,
      phone:                f.contact.phone,

      // Step 3: Identity Verification
      idType:               f.identity.idType,
      idNumber:             f.identity.idNumber,

      // Step 4: Address
      street:               f.address.street,
      city:                 f.address.city,
      state:                f.address.state,
      zipCode:              f.address.zipCode,

      // Step 5: Nominee
      nomineeName:          f.nominee.nomineeName,
      nomineeRelationship:  f.nominee.relationship,
      nomineeDob:           f.nominee.nomineeDob,

      // Step 6: Account Preferences
      accountType:          f.preferences.accountType,
      currency:             f.preferences.currency,

      // Step 7: Security
      password:             f.security.password,
    };

    this.authService.register(payload).subscribe({
      next: () => {
        this.isLoading.set(false);
        this.toastService.success('Onboarding complete! Account created.');
        this.router.navigate(['/auth/login']);
      },
      error: (err) => {
        this.isLoading.set(false);
        if (err.status === 409) {
          this.errorMessage.set('Email address is already in use.');
        } else {
          this.errorMessage.set('Failed to register. Please try again.');
        }
      }
    });
  }
}
