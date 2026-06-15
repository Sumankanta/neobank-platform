import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { StorageUtil } from '../utils/storage.util';
import { ToastService } from '../services/toast';

export const errorInterceptor: HttpInterceptorFn = (req, next) => {
  const router = inject(Router);
  const toastService = inject(ToastService);

  return next(req).pipe(
    catchError((error) => {
      let errorMessage = 'An unexpected error occurred. Please try again.';

      if (error.error && error.error.message) {
        errorMessage = error.error.message;
      } else if (error.error && typeof error.error === 'string') {
        errorMessage = error.error;
      } else if (error.statusText) {
        errorMessage = error.statusText;
      }

      if (error.status === 401) {
        StorageUtil.clearSession();
        toastService.error('Session expired. Please log in again.');
        router.navigate(['/auth/login']);
      } else if (error.status === 403) {
        toastService.error(errorMessage || 'Access Denied: You do not have permission.');
      } else if (error.status === 409) {
        // Handled by specific forms but toast as a fallback
        toastService.warning(errorMessage || 'Conflict occurred.');
      } else if (error.status === 422) {
        toastService.error(errorMessage || 'Transaction rejected (e.g. overdraft prevention).');
      } else if (error.status === 400) {
        toastService.error(errorMessage || 'Bad request. Please verify inputs.');
      } else if (error.status === 500) {
        toastService.error('Internal server error. Please try again later.');
      } else {
        toastService.error(errorMessage);
      }

      return throwError(() => error);
    })
  );
};
