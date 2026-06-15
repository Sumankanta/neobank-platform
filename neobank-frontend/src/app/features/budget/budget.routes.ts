import { Routes } from '@angular/router';
import { Dashboard } from './dashboard/dashboard';
import { Create } from './create/create';

export const BUDGET_ROUTES: Routes = [
  { path: '', component: Dashboard },
  { path: 'create', component: Create },
];
