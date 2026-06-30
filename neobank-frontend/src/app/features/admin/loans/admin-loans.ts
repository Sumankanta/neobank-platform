import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { AdminService } from '../../../core/services/admin';
import { ToastService } from '../../../core/services/toast';

@Component({
  selector: 'app-admin-loans',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './admin-loans.html',
  styleUrl: './admin-loans.css',
})
export class AdminLoans implements OnInit {
  private adminService = inject(AdminService);
  private toastService = inject(ToastService);

  applications = signal<any[]>([]);
  isLoading = signal(true);
  filterStatus = signal<string>('ALL');
  selectedApp = signal<any | null>(null);   // full app shown in modal
  remarks = signal<string>('');
  isProcessing = signal(false);

  filtered = computed(() => {
    const apps = this.applications();
    if (!Array.isArray(apps)) return [];
    const status = this.filterStatus();
    if (status === 'ALL') return apps;
    return apps.filter(a => a && a.status === status);
  });

  totalActive  = computed(() => {
    const apps = this.applications();
    if (!Array.isArray(apps)) return 0;
    return apps.filter(a => a && a.status === 'APPROVED').length;
  });

  totalPending = computed(() => {
    const apps = this.applications();
    if (!Array.isArray(apps)) return 0;
    return apps.filter(a => a && a.status === 'PENDING').length;
  });

  totalAmount  = computed(() => {
    const apps = this.applications();
    if (!Array.isArray(apps)) return 0;
    return apps.reduce((s, a) => s + (a ? (Number(a.requestedAmount) || 0) : 0), 0);
  });

  ngOnInit(): void { this.loadApplications(); }

  loadApplications(): void {
    this.isLoading.set(true);
    this.adminService.getAllLoanApplications().subscribe({
      next: (apps) => {
        this.applications.set(Array.isArray(apps) ? apps : []);
        this.isLoading.set(false);
      },
      error: (err) => {
        console.error('Failed to load loan applications:', err);
        this.toastService.error('Failed to load applications ledger');
        this.applications.set([]);
        this.isLoading.set(false);
      },
    });
  }

  openModal(app: any): void {
    this.selectedApp.set(app);
    this.remarks.set(app.adminRemarks || '');
  }

  closeModal(): void {
    this.selectedApp.set(null);
    this.remarks.set('');
  }

  processApplication(status: 'APPROVED' | 'REJECTED'): void {
    const app = this.selectedApp();
    if (!app) return;
    this.isProcessing.set(true);
    this.adminService.processLoanApplication(app.id, { status, reason: this.remarks() }).subscribe({
      next: (updated) => {
        this.applications.update(apps => {
          if (!Array.isArray(apps)) return [];
          return apps.map(a => a && a.id === updated.id ? updated : a);
        });
        this.toastService.success(`Application ${status === 'APPROVED' ? 'approved ✓' : 'rejected ✗'}`);
        this.closeModal();
        this.isProcessing.set(false);
      },
      error: (err) => {
        console.error('Failed to process loan application:', err);
        this.toastService.error('Failed to process application');
        this.isProcessing.set(false);
      },
    });
  }

  setFilter(status: string): void { this.filterStatus.set(status); }

  calculateEmi(amount: number, annualRate: number, tenureMonths: number): number {
    if (!amount || !tenureMonths) return 0;
    const r = annualRate / 12 / 100;
    if (r === 0) return amount / tenureMonths;
    const emi = (amount * r * Math.pow(1 + r, tenureMonths)) / (Math.pow(1 + r, tenureMonths) - 1);
    return Math.round(emi);
  }

  formatCurrency(v: number): string {
    if (isNaN(v) || v == null) return '₹0';
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(v);
  }

  getInitials(name: string): string {
    if (!name) return '?';
    return name.split(' ').slice(0, 2).map(w => w[0]).join('').toUpperCase();
  }
}
