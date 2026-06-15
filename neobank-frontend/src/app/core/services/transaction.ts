import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiEndpoints } from '../utils/api-endpoints';
import { Transaction } from '../models/transaction';

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  size: number;
  number: number;
}

@Injectable({
  providedIn: 'root'
})
export class TransactionService {
  private http = inject(HttpClient);

  getTransactions(accountId: number | string, page = 0, size = 10): Observable<Page<Transaction>> {
    const params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    return this.http.get<Page<Transaction>>(ApiEndpoints.accounts.transactions(accountId), { params });
  }

  createTransaction(
    accountId: number | string,
    transaction: { type: 'DEBIT' | 'CREDIT'; amount: number; description: string }
  ): Observable<Transaction> {
    return this.http.post<Transaction>(ApiEndpoints.accounts.transactions(accountId), transaction);
  }

  getAllTransactions(page = 0, size = 10, startDate?: string, endDate?: string): Observable<Page<Transaction>> {
    let params = new HttpParams()
      .set('page', page.toString())
      .set('size', size.toString());
    
    if (startDate) params = params.set('startDate', startDate);
    if (endDate) params = params.set('endDate', endDate);
    
    return this.http.get<Page<Transaction>>(ApiEndpoints.transactions.history(), { params });
  }

  getSpendingSummary(): Observable<Record<string, number>> {
    return this.http.get<Record<string, number>>(ApiEndpoints.transactions.summary());
  }

  downloadStatement(): Observable<Blob> {
    return this.http.get(ApiEndpoints.statements.download(), { responseType: 'blob' });
  }
}
