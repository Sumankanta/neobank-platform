export interface User {
  id: number;
  email: string;
  fullName: string;
  role: 'CUSTOMER' | 'ADMIN';
  isActive: boolean;
  createdAt: string;
}
