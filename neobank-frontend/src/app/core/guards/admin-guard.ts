import { CanActivateFn, Router } from '@angular/router';
import { inject } from '@angular/core';
import { StorageUtil } from '../utils/storage.util';

export const adminGuard: CanActivateFn = (route, state) => {
  const router = inject(Router);
  const user = StorageUtil.getUser();
  if (user && user.role === 'ADMIN') {
    return true;
  }
  router.navigate(['/dashboard']);
  return false;
};
