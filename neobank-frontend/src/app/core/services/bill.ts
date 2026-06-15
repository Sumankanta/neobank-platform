import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiEndpoints } from '../utils/api-endpoints';
import { Bill } from '../models/bill';

@Injectable({
  providedIn: 'root'
})
export class BillService {
  private http = inject(HttpClient);

  getBills(): Observable<Bill[]> {
    return this.http.get<Bill[]>(ApiEndpoints.bills.list());
  }

  getBillById(id: number | string): Observable<Bill> {
    return this.http.get<Bill>(ApiEndpoints.bills.detail(id));
  }

  createBill(bill: { billerName: string; amount: number; dueDate: string }): Observable<Bill> {
    return this.http.post<Bill>(ApiEndpoints.bills.create(), bill);
  }

  updateBillStatus(id: number | string, status: 'PENDING' | 'PAID' | 'OVERDUE'): Observable<Bill> {
    return this.http.patch<Bill>(ApiEndpoints.bills.status(id), { status });
  }
}
