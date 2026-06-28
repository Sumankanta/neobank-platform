import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { ApiEndpoints } from '../utils/api-endpoints';

export interface AdminDashboardMetrics {
  totalVaultValue: number;
  activeLoanExposure: number;
  kycQueueCount: number;
  totalTransactions: number;
}

export interface AdminSystemHealth {
  cpuUsage: number;
  memoryUsage: number;
  uptime: number;
  apiHealth: string;
  activeRequests: number;
}

@Injectable({ providedIn: 'root' })
export class AdminService {
  private http = inject(HttpClient);

  getDashboardMetrics(): Observable<AdminDashboardMetrics> {
    return this.http.get<AdminDashboardMetrics>(ApiEndpoints.admin.dashboard());
  }

  getSystemHealth(): Observable<AdminSystemHealth> {
    return this.http.get<AdminSystemHealth>(ApiEndpoints.admin.systemHealth());
  }

  getPendingApprovals(): Observable<any[]> {
    return this.http.get<any[]>(ApiEndpoints.admin.pendingApprovals());
  }

  getAllUsers(): Observable<any[]> {
    return this.http.get<any[]>(ApiEndpoints.admin.users());
  }

  toggleUserStatus(id: number | string): Observable<any> {
    return this.http.patch(ApiEndpoints.admin.toggleStatus(id), {});
  }

  getAllAccounts(): Observable<any[]> {
    return this.http.get<any[]>(ApiEndpoints.admin.allAccounts());
  }

  getAllTransactions(): Observable<any[]> {
    return this.http.get<any[]>(ApiEndpoints.admin.allTransactions());
  }

  getAllLoanApplications(): Observable<any[]> {
    return this.http.get<any[]>(ApiEndpoints.loans.adminApplications());
  }

  processLoanApplication(id: number, decision: { status: 'APPROVED' | 'REJECTED'; reason?: string }): Observable<any> {
    return this.http.put(ApiEndpoints.loans.decision(id), decision);
  }
}
