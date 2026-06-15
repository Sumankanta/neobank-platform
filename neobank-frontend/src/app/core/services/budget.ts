import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiEndpoints } from '../utils/api-endpoints';
import { BudgetSummary } from '../models/budget';

@Injectable({
  providedIn: 'root'
})
export class BudgetService {
  private http = inject(HttpClient);

  createBudget(budget: { category: string; budgetMonth: string; limitAmount: number }): Observable<BudgetSummary> {
    return this.http.post<BudgetSummary>(ApiEndpoints.budgets.create(), budget);
  }

  getBudgetSummary(userId: number | string, month: string): Observable<BudgetSummary[]> {
    return this.http.get<BudgetSummary[]>(ApiEndpoints.budgets.summary(userId, month));
  }

  getAllBudgets(): Observable<BudgetSummary[]> {
    return this.http.get<BudgetSummary[]>(ApiEndpoints.budgets.list());
  }

  deleteBudget(id: number | string): Observable<void> {
    return this.http.delete<void>(ApiEndpoints.budgets.delete(id));
  }
}
