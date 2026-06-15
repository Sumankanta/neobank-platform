import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';

export class CustomValidators {
  /**
   * Enforces password complexity:
   * Minimum 8 characters, at least one uppercase letter, one lowercase letter, one number, and one special character.
   */
  static passwordStrength(): ValidatorFn {
    return (control: AbstractControl): ValidationErrors | null => {
      const value = control.value;
      if (!value) {
        return null;
      }

      const hasUpperCase = /[A-Z]/.test(value);
      const hasLowerCase = /[a-z]/.test(value);
      const hasNumeric = /\d/.test(value);
      const hasSpecial = /[@$!%*?&]/.test(value);
      const isValidLength = value.length >= 8;

      const passwordValid = hasUpperCase && hasLowerCase && hasNumeric && hasSpecial && isValidLength;

      return !passwordValid
        ? {
            passwordStrength: {
              hasUpperCase,
              hasLowerCase,
              hasNumeric,
              hasSpecial,
              isValidLength,
            },
          }
        : null;
    };
  }

  /**
   * Enforces that password and confirmPassword fields match.
   */
  static passwordMatch(passwordControlName: string, confirmControlName: string): ValidatorFn {
    return (group: AbstractControl): ValidationErrors | null => {
      const passwordControl = group.get(passwordControlName);
      const confirmControl = group.get(confirmControlName);

      if (!passwordControl || !confirmControl) {
        return null;
      }

      if (confirmControl.errors && !confirmControl.errors['passwordMismatch']) {
        return null;
      }

      if (passwordControl.value !== confirmControl.value) {
        confirmControl.setErrors({ passwordMismatch: true });
        return { passwordMismatch: true };
      } else {
        confirmControl.setErrors(null);
        return null;
      }
    };
  }
}
