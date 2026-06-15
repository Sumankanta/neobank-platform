import { HttpInterceptorFn } from '@angular/common/http';
import { StorageUtil } from '../utils/storage.util';

export const jwtInterceptor: HttpInterceptorFn = (req, next) => {
  const token = StorageUtil.getToken();
  if (token) {
    req = req.clone({
      setHeaders: {
        Authorization: `Bearer ${token}`
      }
    });
  }
  return next(req);
};
