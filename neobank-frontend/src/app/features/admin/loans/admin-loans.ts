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
  remarksModal = signal<{ id: number; visible: boolean; remarks: string } | null>(null);

  filtered = computed(() => {
    const status = this.filterStatus();
    if (status === 'ALL') return this.applications();
    return this.applications().filter(a => a.status === status);
  });

  totalActive = computed(() => this.applications().filter(a => a.status === 'APPROVED').length);
  totalPending = computed(() => this.applications().filter(a => a.status === 'PENDING').length);
  totalAmount = computed(() => this.applications().reduce((s, a) => s + (a.amount || 0), 0));

  ngOnInit(): void { this.loadApplications(); }

  loadApplications(): void {
    this.isLoading.set(true);
    this.adminService.getAllLoanApplications().subscribe({
      next: (apps) => { this.applications.set(apps); this.isLoading.set(false); },
      error: () => this.isLoading.set(false),
    });
  }

  openRemarksModal(id: number): void {
    this.remarksModal.set({ id, visible: true, remarks: '' });
  }

  closeModal(): void { this.remarksModal.set(null); }

  processApplication(status: 'APPROVED' | 'REJECTED'): void {
    const m = this.remarksModal();
    if (!m) return;
    this.adminService.processLoanApplication(m.id, { status, reason: m.remarks }).subscribe({
      next: (updated) => {
        this.applications.update(apps => apps.map(a => a.id === updated.id ? updated : a));
        this.toastService.success(`Application ${status === 'APPROVED' ? 'approved' : 'rejected'}`);
        this.closeModal();
      },
      error: () => this.toastService.error('Failed to process application'),
    });
  }

  setFilter(status: string): void { this.filterStatus.set(status); }

  formatCurrency(v: number): string {
    return new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(v);
  }
}
