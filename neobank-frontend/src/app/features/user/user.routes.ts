import { Routes } from '@angular/router';
import { Profile } from './profile/profile';
import { AdminUsers } from './admin-users/admin-users';

export const USER_ROUTES: Routes = [
  { path: '', component: Profile },
  { path: 'admin/users', component: AdminUsers },
];
