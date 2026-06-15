import { Routes } from '@angular/router';

export const LOANS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./loans-dashboard/loans-dashboard').then(m => m.LoansDashboard)
  },
  {
    path: 'apply/:productId',
    loadComponent: () => import('./apply-loan/apply-loan').then(m => m.ApplyLoan)
  },
  {
    path: ':id',
    loadComponent: () => import('./loan-detail/loan-detail').then(m => m.LoanDetail)
  }
];
