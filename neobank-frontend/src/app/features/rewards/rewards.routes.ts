import { Routes } from '@angular/router';

export const REWARDS_ROUTES: Routes = [
  { path: '', redirectTo: 'dashboard', pathMatch: 'full' },
  { path: 'dashboard', loadComponent: () => import('./dashboard/dashboard').then(m => m.Dashboard) },
  { path: 'gallery', loadComponent: () => import('./gallery/gallery').then(m => m.Gallery) },
  { path: 'success', loadComponent: () => import('./success/success').then(m => m.Success) },
];
