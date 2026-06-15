import { Routes } from '@angular/router';
import { Dashboard } from './dashboard/dashboard';
import { AccountDetail } from './account-detail/account-detail';
import { CreateAccount } from './create-account/create-account';

export const ACCOUNTS_ROUTES: Routes = [
  { path: '', component: Dashboard },
  { path: 'create', component: CreateAccount },
  { path: ':id', component: AccountDetail },
];
