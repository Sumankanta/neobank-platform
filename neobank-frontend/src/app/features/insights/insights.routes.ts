import { Routes } from '@angular/router';

export const INSIGHTS_ROUTES: Routes = [
  {
    path: '',
    loadComponent: () => import('./insights-dashboard/insights-dashboard').then(m => m.InsightsDashboard)
  }
];
