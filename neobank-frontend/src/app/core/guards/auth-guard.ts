import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { StorageUtil } from '../utils/storage.util';

export const authGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  if (StorageUtil.getToken()) {
    return true;
  }
  router.navigate(['/auth/login']);
  return false;
};
