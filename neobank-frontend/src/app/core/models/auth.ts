export interface AuthResponse {
  token: string;
  userId: number;
  email: string;
  fullName: string;
  role: 'CUSTOMER' | 'ADMIN';
}
