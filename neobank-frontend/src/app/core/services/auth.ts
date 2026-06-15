import { Injectable, signal, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, tap } from 'rxjs';
import { ApiEndpoints } from '../utils/api-endpoints';
import { StorageUtil } from '../utils/storage.util';
import { AuthResponse } from '../models/auth';
import { User } from '../models/user';

@Injectable({
  providedIn: 'root'
})
export class AuthService {
  private http = inject(HttpClient);
  currentUser = signal<Omit<AuthResponse, 'token'> | null>(StorageUtil.getUser());

  login(credentials: { email: string; password: string }): Observable<AuthResponse> {
    return this.http.post<AuthResponse>(ApiEndpoints.auth.login(), credentials).pipe(
      tap((res) => {
        StorageUtil.setSession(res);
        this.currentUser.set({
          userId: res.userId,
          email: res.email,
          fullName: res.fullName,
          role: res.role
        });
      })
    );
  }

  register(userData: any): Observable<User> {
    return this.http.post<User>(ApiEndpoints.auth.register(), userData);
  }

  getProfile(): Observable<User> {
    return this.http.get<User>(ApiEndpoints.users.profile()).pipe(
      tap((user) => {
        const current = this.currentUser();
        if (current) {
          const updated = { ...current, fullName: user.fullName, email: user.email, role: user.role };
          StorageUtil.setSession({
            token: StorageUtil.getToken() || '',
            ...updated
          });
          this.currentUser.set(updated);
        }
      })
    );
  }

  updateProfile(userData: any): Observable<User> {
    return this.http.put<User>(ApiEndpoints.users.profile(), userData).pipe(
      tap((user) => {
        const current = this.currentUser();
        if (current) {
          const updated = { ...current, fullName: user.fullName, email: user.email, role: user.role };
          StorageUtil.setSession({
            token: StorageUtil.getToken() || '',
            ...updated
          });
          this.currentUser.set(updated);
        }
      })
    );
  }

  logout(): void {
    StorageUtil.clearSession();
    this.currentUser.set(null);
  }

  isAuthenticated(): boolean {
    return !!StorageUtil.getToken();
  }
}
