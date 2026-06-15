import { Component, OnInit, inject, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { BillService } from '../../../core/services/bill';
import { Bill } from '../../../core/models/bill';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatCardModule } from '@angular/material/card';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { CurrencyFormatPipe } from '../../../shared/pipes/currency-format-pipe';
import { ToastService } from '../../../core/services/toast';

@Component({
  selector: 'app-bill-detail',
  standalone: true,
  imports: [
    CommonModule,
    RouterLink,
    MatProgressSpinnerModule,
    MatCardModule,
    MatButtonModule,
    MatIconModule,
    CurrencyFormatPipe,
  ],
  templateUrl: './detail.html',
  styleUrl: './detail.css',
})
export class Detail implements OnInit {
  private route = inject(ActivatedRoute);
  private billService = inject(BillService);
  private toastService = inject(ToastService);

  bill = signal<Bill | null>(null);
  isLoading = signal(true);

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.loadBill(id);
    }
  }

  loadBill(id: string): void {
    this.isLoading.set(true);
    this.billService.getBillById(id).subscribe({
      next: (bill) => {
        this.bill.set(bill);
        this.isLoading.set(false);
      },
      error: () => this.isLoading.set(false)
    });
  }

  payBill(): void {
    if (!this.bill()) return;
    
    this.billService.updateBillStatus(this.bill()!.id, 'PAID').subscribe({
      next: () => {
        this.toastService.success('Bill paid successfully!');
        this.loadBill(this.bill()!.id.toString());
      },
      error: () => this.toastService.error('Failed to update bill status.')
    });
  }
}
