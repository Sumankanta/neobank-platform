import { Routes } from '@angular/router';

export const ADMIN_ROUTES: Routes = [
  {
    path: '',
    redirectTo: 'dashboard',
    pathMatch: 'full',
  },
  {
    path: 'dashboard',
    loadComponent: () =>
      import('./dashboard/admin-dashboard').then((m) => m.AdminDashboard),
  },
  {
    path: 'accounts',
    loadComponent: () =>
      import('./accounts/admin-accounts').then((m) => m.AdminAccounts),
  },
  {
    path: 'transactions',
    loadComponent: () =>
      import('./transactions/admin-transactions').then((m) => m.AdminTransactions),
  },
  {
    path: 'insights',
    loadComponent: () =>
      import('./insights/admin-insights').then((m) => m.AdminInsights),
  },
  {
    path: 'loans',
    loadComponent: () =>
      import('./loans/admin-loans').then((m) => m.AdminLoans),
  },
];
