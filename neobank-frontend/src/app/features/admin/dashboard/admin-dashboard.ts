import { Component, OnInit, OnDestroy, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterModule } from '@angular/router';
import { AdminService, AdminDashboardMetrics, AdminSystemHealth } from '../../../core/services/admin';
import { UserService } from '../../../core/services/user';

@Component({
  selector: 'app-admin-dashboard',
  standalone: true,
  imports: [CommonModule, RouterModule],
  templateUrl: './admin-dashboard.html',
  styleUrl: './admin-dashboard.css',
})
export class AdminDashboard implements OnInit, OnDestroy {
  private adminService = inject(AdminService);

  metrics = signal<AdminDashboardMetrics | null>(null);
  health = signal<AdminSystemHealth | null>(null);
  loadingMetrics = signal(true);
  loadingHealth = signal(true);
  healthPollInterval: any;

  apiEndpoints = [
    { name: '/api/auth', status: 'UP', latency: '12ms' },
    { name: '/api/accounts', status: 'UP', latency: '24ms' },
    { name: '/api/transactions', status: 'UP', latency: '18ms' },
    { name: '/api/loans', status: 'UP', latency: '31ms' },
    { name: '/api/insights', status: 'UP', latency: '20ms' },
    { name: '/api/users', status: 'UP', latency: '15ms' },
  ];

  ngOnInit(): void {
    this.loadMetrics();
    this.loadHealth();
    this.healthPollInterval = setInterval(() => this.loadHealth(), 15000);
  }

  ngOnDestroy(): void {
    if (this.healthPollInterval) clearInterval(this.healthPollInterval);
  }

  loadMetrics(): void {
    this.loadingMetrics.set(true);
    this.adminService.getDashboardMetrics().subscribe({
      next: (m) => { this.metrics.set(m); this.loadingMetrics.set(false); },
      error: () => this.loadingMetrics.set(false),
    });
  }

  loadHealth(): void {
    this.adminService.getSystemHealth().subscribe({
      next: (h) => { this.health.set(h); this.loadingHealth.set(false); },
      error: () => this.loadingHealth.set(false),
    });
  }

  formatUptime(seconds: number): string {
    const h = Math.floor(seconds / 3600);
    const m = Math.floor((seconds % 3600) / 60);
    return `${h}h ${m}m`;
  }

  formatCurrency(value: number): string {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(value);
  }
}
