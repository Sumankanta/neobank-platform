import { AuthResponse } from '../models/auth';

const TOKEN_KEY = 'neobank_token';
const USER_KEY = 'neobank_user';

export class StorageUtil {
  static setSession(authData: AuthResponse): void {
    if (typeof window !== 'undefined') {
      sessionStorage.setItem(TOKEN_KEY, authData.token);
      sessionStorage.setItem(USER_KEY, JSON.stringify({
        userId: authData.userId,
        email: authData.email,
        fullName: authData.fullName,
        role: authData.role
      }));
    }
  }

  static getToken(): string | null {
    if (typeof window !== 'undefined') {
      return sessionStorage.getItem(TOKEN_KEY);
    }
    return null;
  }

  static getUser(): Omit<AuthResponse, 'token'> | null {
    if (typeof window !== 'undefined') {
      const userStr = sessionStorage.getItem(USER_KEY);
      if (userStr) {
        try {
          return JSON.parse(userStr);
        } catch (e) {
          return null;
        }
      }
    }
    return null;
  }

  static clearSession(): void {
    if (typeof window !== 'undefined') {
      sessionStorage.removeItem(TOKEN_KEY);
      sessionStorage.removeItem(USER_KEY);
    }
  }
}
