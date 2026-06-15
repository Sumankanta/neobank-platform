import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiEndpoints } from '../utils/api-endpoints';
import { Account } from '../models/account';

@Injectable({
  providedIn: 'root'
})
export class AccountService {
  private http = inject(HttpClient);

  getAccounts(): Observable<Account[]> {
    return this.http.get<Account[]>(ApiEndpoints.accounts.list());
  }

  getAccountById(id: number | string): Observable<Account> {
    return this.http.get<Account>(ApiEndpoints.accounts.detail(id));
  }

  createAccount(accountType: 'SAVINGS' | 'CURRENT'): Observable<Account> {
    return this.http.post<Account>(ApiEndpoints.accounts.create(), { accountType });
  }
}
