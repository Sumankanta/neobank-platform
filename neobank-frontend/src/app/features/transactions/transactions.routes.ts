import { Routes } from '@angular/router';
import { TransactionList } from './transaction-list/transaction-list';
import { TransactionForm } from './transaction-form/transaction-form';

export const TRANSACTIONS_ROUTES: Routes = [
  { path: '', component: TransactionList },
  { path: 'new', component: TransactionForm },
];
