import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth-guard';
import { adminGuard } from './core/guards/admin-guard';
import { Layout } from './shared/components/layout/layout';

export const routes: Routes = [
  // Public landing page
  {
    path: '',
    loadComponent: () =>
      import('./features/landing/landing').then((m) => m.Landing),
  },

  // Auth routes (public) — wrapped in layout shell (AuthLayout)
  {
    path: 'auth',
    loadComponent: () =>
      import('./features/auth/auth-layout').then((m) => m.AuthLayout),
    loadChildren: () =>
      import('./features/auth/auth.routes').then((m) => m.AUTH_ROUTES),
  },

  // Protected app routes — wrapped in app-wide Layout (Sidebar + Header)
  {
    path: '',
    component: Layout,
    canActivate: [authGuard],
    children: [
      {
        path: 'dashboard',
        loadComponent: () =>
          import('./features/dashboard/home/home').then((m) => m.Home),
      },
      {
        path: 'accounts',
        loadChildren: () =>
          import('./features/accounts/accounts.routes').then((m) => m.ACCOUNTS_ROUTES),
      },
      {
        path: 'bills',
        loadChildren: () =>
          import('./features/bills/bills.routes').then((m) => m.BILLS_ROUTES),
      },
      {
        path: 'budget',
        loadChildren: () =>
          import('./features/budget/budget.routes').then((m) => m.BUDGET_ROUTES),
      },
      {
        path: 'rewards',
        loadChildren: () =>
          import('./features/rewards/rewards.routes').then((m) => m.REWARDS_ROUTES),
      },
      {
        path: 'transactions',
        loadChildren: () =>
          import('./features/transactions/transactions.routes').then((m) => m.TRANSACTIONS_ROUTES),
      },
      {
        path: 'loans',
        loadChildren: () =>
          import('./features/loans/loans.routes').then((m) => m.LOANS_ROUTES),
      },
      {
        path: 'insights',
        loadChildren: () =>
          import('./features/insights/insights.routes').then((m) => m.INSIGHTS_ROUTES),
      },
      {
        path: 'profile',
        loadComponent: () =>
          import('./features/user/profile/profile').then((m) => m.Profile),
      },
      {
        path: 'admin/users',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/user/admin-users/admin-users').then((m) => m.AdminUsers),
      },
    ],
  },

  // Wildcard fallback
  { path: '**', redirectTo: '/dashboard' },
];
