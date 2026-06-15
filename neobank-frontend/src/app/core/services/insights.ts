import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiEndpoints } from '../utils/api-endpoints';
import { AuthService } from './auth';

export interface FinancialTrend {
  month: string;
  income: number;
  expense: number;
}

export interface SpendingCategory {
  category: string;
  amount: number;
  percentage: number;
}

export interface InsightsData {
  trends: FinancialTrend[];
  categories: SpendingCategory[];
  totalIncome: number;
  totalExpense: number;
  netSavings: number;
}

@Injectable({
  providedIn: 'root'
})
export class InsightsService {
  private http = inject(HttpClient);
  private auth = inject(AuthService);

  getInsights(): Observable<InsightsData> {
    const userId = this.auth.currentUser()?.userId || 0;
    return this.http.get<InsightsData>(ApiEndpoints.insights.summary(userId));
  }
}
