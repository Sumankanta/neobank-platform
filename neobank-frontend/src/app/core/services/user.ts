import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiEndpoints } from '../utils/api-endpoints';
import { User } from '../models/user';

@Injectable({
  providedIn: 'root'
})
export class UserService {
  private http = inject(HttpClient);

  getAdminUsers(): Observable<User[]> {
    return this.http.get<User[]>(ApiEndpoints.admin.users());
  }

  toggleUserStatus(id: number | string): Observable<User> {
    return this.http.patch<User>(ApiEndpoints.admin.toggleStatus(id), {});
  }
}
