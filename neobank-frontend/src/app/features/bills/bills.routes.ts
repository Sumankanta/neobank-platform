import { Routes } from '@angular/router';
import { List } from './list/list';
import { Create } from './create/create';
import { Detail } from './detail/detail';

export const BILLS_ROUTES: Routes = [
  { path: '', component: List },
  { path: 'create', component: Create },
  { path: ':id', component: Detail },
];
