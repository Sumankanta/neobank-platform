import { Component, OnInit, inject, signal, computed } from '@angular/core';
import { CommonModule } from '@angular/common';
import { RouterLink } from '@angular/router';
import { BillService } from '../../../core/services/bill';
import { Bill } from '../../../core/models/bill';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format-pipe';
import { ToastService } from '../../../core/services/toast';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-bill-list',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatProgressSpinnerModule,
    CurrencyFormatPipe,
    FormsModule
  ],
  templateUrl: './list.html',
  styleUrl: './list.css',
})
export class List implements OnInit {
  private billService = inject(BillService);
  private toastService = inject(ToastService);

  bills = signal<Bill[]>([]);
  isLoading = signal(true);

  // Computed stats
  totalUpcoming = computed(() => 
    this.bills()
      .filter(b => b.status === 'PENDING')
      .reduce((sum, b) => sum + b.amount, 0)
  );

  totalOverdue = computed(() => 
    this.bills()
      .filter(b => b.status === 'OVERDUE')
      .reduce((sum, b) => sum + b.amount, 0)
  );

  remindersCount = computed(() => 
    this.bills().filter(b => b.remindMe).length
  );

  ngOnInit(): void {
    this.loadBills();
  }

  loadBills(): void {
    this.isLoading.set(true);
    this.billService.getBills().subscribe({
      next: (bills) => {
        // Sort bills: overdue first, then pending by due date, then paid
        bills.sort((a, b) => {
          if (a.status === 'OVERDUE' && b.status !== 'OVERDUE') return -1;
          if (b.status === 'OVERDUE' && a.status !== 'OVERDUE') return 1;
          if (a.status === 'PENDING' && b.status === 'PAID') return -1;
          if (b.status === 'PENDING' && a.status === 'PAID') return 1;
          return new Date(a.dueDate).getTime() - new Date(b.dueDate).getTime();
        });
        this.bills.set(bills);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  payBill(billId: number): void {
    this.billService.updateBillStatus(billId, 'PAID').subscribe({
      next: () => {
        this.toastService.success('Bill paid successfully!');
        this.loadBills();
      },
      error: () => this.toastService.error('Failed to process payment.')
    });
  }

  toggleReminder(bill: Bill, event: any): void {
    // Mock the toggle action since there's no update endpoint for remindMe
    const isChecked = event.target.checked;
    this.toastService.success(`Reminder ${isChecked ? 'enabled' : 'disabled'} for ${bill.billerName}`);
    // In a real app we'd call an API here.
  }

  getBillerIcon(name: string): string {
    const n = name.toLowerCase();
    if (n.includes('electric') || n.includes('power')) return 'fa-solid fa-bolt text-warning';
    if (n.includes('water') || n.includes('sewer')) return 'fa-solid fa-faucet-drip text-blue-400';
    if (n.includes('internet') || n.includes('wifi') || n.includes('broadband')) return 'fa-solid fa-wifi text-primary';
    if (n.includes('mobile') || n.includes('phone') || n.includes('tele')) return 'fa-solid fa-mobile-screen text-indigo-400';
    if (n.includes('gas')) return 'fa-solid fa-fire-flame-simple text-orange-400';
    if (n.includes('trash') || n.includes('waste')) return 'fa-solid fa-trash-can text-gray-400';
    if (n.includes('insurance')) return 'fa-solid fa-shield-heart text-emerald-400';
    return 'fa-solid fa-file-invoice text-secondary';
  }

  getBillerBg(name: string): string {
    const n = name.toLowerCase();
    if (n.includes('electric') || n.includes('power')) return 'bg-warning-light';
    if (n.includes('water') || n.includes('sewer')) return 'bg-blue-light';
    if (n.includes('internet') || n.includes('wifi') || n.includes('broadband')) return 'bg-primary-light';
    if (n.includes('mobile') || n.includes('phone') || n.includes('tele')) return 'bg-indigo-light';
    if (n.includes('gas')) return 'bg-orange-light';
    if (n.includes('trash') || n.includes('waste')) return 'bg-gray-light';
    if (n.includes('insurance')) return 'bg-emerald-light';
    return 'bg-secondary-light';
  }
}
